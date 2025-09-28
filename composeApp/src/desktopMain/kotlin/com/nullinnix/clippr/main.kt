package com.nullinnix.clippr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.clippr_status_icon
import clippr.composeapp.generated.resources.pin
import com.nullinnix.clippr.database.ClipsDatabaseFactory
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.coerce
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.getClipboard
import com.nullinnix.clippr.misc.pasteWithRobot
import com.nullinnix.clippr.misc.showMacConfirmDialog
import com.nullinnix.clippr.model.ClipsViewModel
import com.nullinnix.clippr.theme.Theme
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.tulskiy.keymaster.common.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import java.awt.Frame
import java.awt.Point
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import java.lang.Exception
import javax.swing.KeyStroke
import javax.swing.SwingUtilities

fun main() {
    val database = ClipsDatabaseFactory().create()
    val clipsViewModel = ClipsViewModel(database.clipsDao())
    val provider = Provider.getCurrentProvider(true)
    val composeWindowStateRaw = MutableStateFlow<Window?>(null)

    provider.register(
        KeyStroke.getKeyStroke("meta shift V")
    ) {
        clipsViewModel.setShowMainApp(!clipsViewModel.clipsState.value.showMainApp)
    }

    CoroutineScope(Dispatchers.IO).launch {
        while(true) {
            getClipboard(
                onCopy = {
                    clipsViewModel.onAction(ClipAction.OnAddClip(it))
                }
            )

            delay(1000)
        }
    }

    application {
        val trayState = rememberTrayState()
        val clipsState = clipsViewModel.clipsState.collectAsState().value

        val showMain = clipsState.showMainApp
        val windowState = rememberWindowState()

        val composeWindowState = composeWindowStateRaw.collectAsState().value

        Tray (
            state = trayState,
            icon = painterResource(Res.drawable.clippr_status_icon),
            menu = {
                val pinned = if (clipsState.pinnedClips.size > 5) clipsState.pinnedClips.subList(0, 5) else clipsState.pinnedClips
                val others = if (clipsState.otherClips.size > 25) clipsState.otherClips.subList(0, 25) else clipsState.otherClips

                Item(text = "Open app") {
                    clipsViewModel.forceShowMainApp()
                }

                Separator()

                for (clip in pinned) {
                    Item(clip.content.trimIndent().coerce(50)) {
                        println("pasted new...")
                        pasteWithRobot(clip)
                    }
                }

                Separator()

                for (clip in others) {
                    Item(clip.content.trimIndent().coerce(50)) {
                        pasteWithRobot(clip)
                    }
                }

                Separator()

                Item(text = "Clear unpinned") {
                    if (showMacConfirmDialog("Delete all unpinned clips?", "Delete all your unpinned clips")) {

                    }
                }

                Separator()

                Item(text = "Settings") {
                    clipsViewModel.switchTab(Tab.SettingsTab)
                    clipsViewModel.forceShowMainApp()
                }

                Separator()

                Item(text = "Quit") {
                    exitApplication()
                }
            }
        )

        if (showMain) {
            Window (
                transparent = true,
                undecorated = true,
                state = windowState,
                onCloseRequest = {
                    clipsViewModel.setShowMainApp(false)
                },
                title = "Clippr",
            ) {
                LaunchedEffect(Unit) {
                    composeWindowStateRaw.update {
                        this@Window.window
                    }
                }

                Column (
                    modifier = Modifier
                        .clip(corners(10.dp))
                        .background(Color.White)
                ){
                    WindowBar (
                        window = window,
                        onToggleFullScreen = {
                            toggleFullscreen(window)
                        },
                        onHideMainApp = {
                            clipsViewModel.setShowMainApp(false)
                        }
                    )

                    Theme {
                        App (
                            window = window,
                            clipsViewModel = clipsViewModel
                        )
                    }
                }
            }
        }
    }
}

var size = Dimension(300, 200)
var location = Point(0, 0)
var isFullScreen = false

fun toggleFullscreen(window: Window?) {
    try {
        if (window != null) {
            val gd = window.graphicsConfiguration
            val insets = Toolkit.getDefaultToolkit().getScreenInsets(gd)
            val maxSize = Dimension(window.graphicsConfiguration.bounds.size.width, window.graphicsConfiguration.bounds.size.height - (insets.top + insets.bottom))
            val initialFullScreenPosition = Point(0, insets.top)

            if (isFullScreen && window.size == maxSize && window.location == initialFullScreenPosition) {
                window.size = size
                window.location = location

                isFullScreen = false
            } else {
                size = window.size
                location = window.location

                window.location = Point(0, insets.top)
                window.size = Dimension(window.graphicsConfiguration.bounds.size.width, window.graphicsConfiguration.bounds.size.height - (insets.top + insets.bottom))

                isFullScreen = true
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun focusWindow() {
    val nsApp = Cocoa.INSTANCE.objc_getClass("NSApplication")
    val sharedApp = Cocoa.INSTANCE.objc_msgSend(nsApp, Cocoa.INSTANCE.sel_registerName("sharedApplication"))
    Cocoa.INSTANCE.objc_msgSend(sharedApp, Cocoa.INSTANCE.sel_registerName("activateIgnoringOtherApps:"))
}

interface Cocoa : Library {
    fun objc_getClass(name: String): Pointer
    fun sel_registerName(name: String): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer): Pointer

    companion object {
        val INSTANCE: Cocoa = Native.load("objc", Cocoa::class.java)
    }
}