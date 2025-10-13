package com.nullinnix.clippr

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
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
import com.nullinnix.clippr.misc.formatText
import com.nullinnix.clippr.misc.isInLoginItemsChecker
import com.nullinnix.clippr.misc.listenForCopy
import com.nullinnix.clippr.misc.manageKeyEvent
import com.nullinnix.clippr.misc.pasteWithRobot
import com.nullinnix.clippr.misc.registerKeyStroke
import com.nullinnix.clippr.misc.showMacConfirmDialog
import com.nullinnix.clippr.misc.toggleFullscreen
import com.nullinnix.clippr.theme.Theme
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import com.nullinnix.clippr.viewmodels.MiscViewModel
import com.nullinnix.clippr.viewmodels.NotificationsViewModel
import com.nullinnix.clippr.viewmodels.SettingsViewModel
import com.nullinnix.clippr.views.App
import com.nullinnix.clippr.views.WindowBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit
import java.awt.Window

fun main() {
    val clipsDatabase = ClipsDatabaseFactory().create()
    val settingsDatabase = SettingsDatabaseFactory().create()
    val settingsViewModel = SettingsViewModel(settingsDatabase.settingsDao())
    val miscViewModel = MiscViewModel()
    val notificationsViewModel = NotificationsViewModel()
    val clipsViewModel = ClipsViewModel(clipsDatabase.clipsDao(), settingsViewModel, miscViewModel, notificationsViewModel)

    val composeWindowStateRaw = MutableStateFlow<Window?>(null)
    var coercedWindowPositionAndSize = false

    registerKeyStroke {
        if (settingsViewModel.state.value.enableMetaShiftVPopup) {
            if (composeWindowStateRaw.value != null && composeWindowStateRaw.value?.isVisible ?: false) {
                clipsViewModel.setShowMainApp(false)
            } else {
                clipsViewModel.forceShowMainApp()
            }
        }
    }

    listenForCopy (
        settingsViewModel = settingsViewModel
    ){
        clipsViewModel.onAction(ClipAction.OnAddClip(it))
    }

    isInLoginItemsChecker {
        if (it != settingsViewModel.state.value.startAtLogin) {
            settingsViewModel.onAction(SettingsAction.SetStartAtLogin(it))
        }
    }

    application {
        val windowState = rememberWindowState()

        if (showMain.collectAsState().value) {
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
                    if (!coercedWindowPositionAndSize) {
                        val gd = window.graphicsConfiguration
                        val insets = Toolkit.getDefaultToolkit().getScreenInsets(gd)
                        val maxSize = Dimension(window.graphicsConfiguration.bounds.size.width, window.graphicsConfiguration.bounds.size.height - (insets.top + insets.bottom))
                        val windowSize = window.size

                        window.size = Dimension(window.width, maxSize.height)
                        window.location = Point((maxSize.width / 2) - (windowSize.width / 2), insets.top)

                        coercedWindowPositionAndSize = true
                    }
                }

                var isFocused by remember { mutableStateOf(true) }
                val composeWindowState = composeWindowStateRaw.collectAsState().value
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(composeWindowState) {
                    while (true) {
                        if (!clipsViewModel.clipsState.value.isSearching && clipsViewModel.clipsState.value.currentTab != Tab.SettingsTab) {
                            focusRequester.requestFocus()
                        }

                        if (composeWindowState != null) {
                            isFocused = composeWindowState.isFocused
                        }

                        delay(100)
                    }
                }

                LaunchedEffect(Unit) {
                    this@Window.window.minimumSize = Dimension(300, 300)

                    composeWindowStateRaw.update {
                        this@Window.window
                    }
                }

                LaunchedEffect(miscViewModel.state.collectAsState().value) {
                    if (clipsViewModel.clipsState.value.protoFilters.sources.isEmpty()) {
                        clipsViewModel.setFilters(clipsViewModel.clipsState.value.protoFilters.copy(sources = miscViewModel.state.value.allApps.keys.toSet()))
                        clipsViewModel.setSearchFilters(clipsViewModel.clipsState.value.protoFilters.copy(sources = miscViewModel.state.value.allApps.keys.toSet()))
                    }
                }

                Box (
                    modifier = Modifier
                        .clip(corners(10.dp))
                        .border(1.dp, color = Color.Black.copy(0.25f), shape = corners(10.dp))
                        .background(Color.White)
                        .focusRequester(focusRequester)
                        .focusable()
                        .onPreviewKeyEvent { event ->
                            manageKeyEvent(event, clipsViewModel, settingsViewModel, miscViewModel)
                        }
                ){
                    Theme {
                        Box(
                            modifier = Modifier
                                .padding(top = 60.dp)
                                .fillMaxSize()
                        ) {
                            App(
                                window = window,
                                isFocused = isFocused,
                                clipsViewModel = clipsViewModel,
                                settingsViewModel = settingsViewModel,
                                miscViewModel = miscViewModel,
                                notificationsViewModel = notificationsViewModel,
                                onInterceptEvent = { event ->
                                    manageKeyEvent(event, clipsViewModel, settingsViewModel, miscViewModel)
                                }
                            )
                        }
                    }

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
                }
            }
        }

        val trayState = rememberTrayState()
        val clipsState = clipsViewModel.clipsState.collectAsState().value
        val settingsState = settingsViewModel.state.collectAsState().value

        Tray (
            state = trayState,
            icon = painterResource(Res.drawable.clippr_status_icon),
            menu = {
                val pinned = if (clipsState.pinnedClips.size > 5) clipsState.pinnedClips.subList(0, 5) else clipsState.pinnedClips
                val others = if (clipsState.otherClips.size > 25) clipsState.otherClips.subList(0, 25) else clipsState.otherClips

                Item(text = "Select a clip to paste in a focused window", enabled = false) {}

                Separator()

                Item(text = "\uD83D\uDCC2 Open Clippr") {
                    clipsViewModel.forceShowMainApp()
                }

                Separator()

                Item(text = "\uD83D\uDCCE Pinned clips", enabled = false) {}

                for (clip in pinned) {
                    Item(formatText(clip.content.trimIndent().trimMargin().coerce(50))) {
                        pasteWithRobot(clip, !settingsState.pasteFilesAsText)
                    }
                }

                Separator()

                Item(text = "\uD83D\uDDC2 Other clips", enabled = false) {}

                for (clip in others) {
                    Item(formatText(clip.content.trimIndent().trimMargin().coerce(50))) {
                        pasteWithRobot(clip, !settingsState.pasteFilesAsText)
                    }
                }

                Separator()

                Item(text = "Options", enabled = false) {}

                Item(text = "${if(settingsState.pasteFilesAsText) "✅" else "❌"} Toggle paste files as text") {
                    settingsViewModel.onAction(SettingsAction.SetPasteFilesAsText(!settingsState.pasteFilesAsText))
                }

                Item(text = "\uD83D\uDDD1\uFE0F Clear unpinned") {
                    if (showMacConfirmDialog("Delete all unpinned clips?", "Delete all your unpinned clips")) {
                        clipsViewModel.deleteAllUnpinned()
                    }
                }

                Item(text = "⚙\uFE0F Settings") {
                    clipsViewModel.switchTab(Tab.SettingsTab)
                    clipsViewModel.forceShowMainApp()
                }

                Separator()

                Item(text = "❌ Quit") {
                    exitApplication()
                }
            },
            onAction = {
                println("action")
            },
            tooltip = "Clippr"
        )
    }
}

val showMain = MutableStateFlow(false)