package com.nullinnix.clippr

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.nullinnix.clippr.database.clips.ClipsDatabaseFactory
import com.nullinnix.clippr.database.settings.SettingsDatabaseFactory
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.SettingsAction
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.coerce
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.isInLoginItemsChecker
import com.nullinnix.clippr.misc.listenForCopy
import com.nullinnix.clippr.misc.pasteWithRobot
import com.nullinnix.clippr.misc.registerKeyStroke
import com.nullinnix.clippr.misc.showMacConfirmDialog
import com.nullinnix.clippr.misc.toggleFullscreen
import com.nullinnix.clippr.theme.Theme
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import com.nullinnix.clippr.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import java.awt.Window

fun main() {
    val clipsDatabase = ClipsDatabaseFactory().create()
    val settingsDatabase = SettingsDatabaseFactory().create()
    val settingsViewModel = SettingsViewModel(settingsDatabase.settingsDao())
    val clipsViewModel = ClipsViewModel(clipsDatabase.clipsDao(), settingsViewModel)

    val composeWindowStateRaw = MutableStateFlow<Window?>(null)

    registerKeyStroke {
        if (settingsViewModel.settings.value.enableMetaShiftVPopup) {
            if (composeWindowStateRaw.value != null && composeWindowStateRaw.value?.isVisible ?: false) {
                clipsViewModel.setShowMainApp(false)
            } else {
                clipsViewModel.forceShowMainApp()
            }
        }
    }

    listenForCopy {
        clipsViewModel.onAction(ClipAction.OnAddClip(it))
    }

    isInLoginItemsChecker {
        if (it != settingsViewModel.settings.value.startAtLogin) {
            settingsViewModel.onAction(SettingsAction.SetStartAtLogin(it))
        }
    }

    application {
        val trayState = rememberTrayState()
        val clipsState = clipsViewModel.clipsState.collectAsState().value

        val showMain = clipsState.showMainApp
        val windowState = rememberWindowState()

        val composeWindowState = composeWindowStateRaw.collectAsState().value

        var isFocused by remember { mutableStateOf(true) }

        LaunchedEffect(composeWindowState) {
            while (true) {
                if (composeWindowState != null) {
                    isFocused = composeWindowState.isFocused
                }

                delay(100)
            }
        }

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
                    Item(clip.content.trimIndent().trimMargin().coerce(50)) {
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
                        clipsViewModel.deleteAllUnpinned()
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
            },
            onAction = {
                println("action")
            },
            tooltip = "this is tool tip"
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
                    this@Window.window.minimumSize = Dimension(300, 300)

                    composeWindowStateRaw.update {
                        this@Window.window
                    }
                }

                Column (
                    modifier = Modifier
                        .clip(corners(10.dp))
                        .border(1.dp, color = Color.Black.copy(0.25f), shape = corners(10.dp))
                        .background(Color.White)
                ){
                    WindowBar (
                        window = window,
                        isFocused = isFocused,
                        onToggleFullScreen = {
                            toggleFullscreen(window)
                        },
                        onHideMainApp = {
                            clipsViewModel.setShowMainApp(false)
                        }
                    )

                    Theme {
                        App (
                            isFocused = isFocused,
                            clipsViewModel = clipsViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }
}