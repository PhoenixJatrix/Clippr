package com.nullinnix.clippr

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.pin
import com.nullinnix.clippr.database.ClipsDatabaseFactory
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.coerce
import com.nullinnix.clippr.misc.getClipboard
import com.nullinnix.clippr.misc.pasteWithRobot
import com.nullinnix.clippr.misc.showMacConfirmDialog
import com.nullinnix.clippr.model.ClipsViewModel
import com.nullinnix.clippr.theme.Theme
import com.tulskiy.keymaster.common.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import javax.swing.KeyStroke

fun main() {
    val database = ClipsDatabaseFactory().create()
    val clipsViewModel = ClipsViewModel(database.clipsDao())
    val provider = Provider.getCurrentProvider(true)

    provider.register(
        KeyStroke.getKeyStroke("meta shift V")
    ) {
        clipsViewModel.setShowMainApp(!clipsViewModel.clipsState.value.showMainApp)
    }

    application {
        val trayState = rememberTrayState()
        val clipsState = clipsViewModel.clipsState.collectAsState().value

        val showMain = clipsState.showMainApp
        val windowState = rememberWindowState()

//        LaunchedEffect(windowState) {
//            if (windowState.isMinimized) {
//                clipsViewModel.setShowMainApp(false)
//            }
//        }

        Tray (
            state = trayState,
            icon = painterResource(Res.drawable.pin), // place under resources
            menu = {
                val pinned = if (clipsState.pinnedClips.size > 5) clipsState.pinnedClips.subList(0, 5) else clipsState.pinnedClips
                val others = if (clipsState.otherClips.size > 25) clipsState.otherClips.subList(0, 25) else clipsState.otherClips

                Item(text = "Open app") {
                    clipsViewModel.setShowMainApp(true)
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
                    clipsViewModel.setShowMainApp(true)
                }

                Separator()

                Item(text = "Quit") {
                    exitApplication()
                }
            }
        )

        if (showMain) {
            Window(
                undecorated = true,
                state = windowState,
                onCloseRequest = {
                    clipsViewModel.setShowMainApp(false)
                },
                title = "Clippr",
            ) {
                LaunchedEffect(Unit) {
//                    val nsWindow = getNs(window)
//                    disableHide(nsWindow)
                }

                Theme {
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

                    App (
                        clipsViewModel = clipsViewModel
                    )
                }
            }
        }
    }
}