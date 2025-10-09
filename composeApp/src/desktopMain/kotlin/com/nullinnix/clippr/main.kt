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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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
import com.nullinnix.clippr.misc.EscPriorityConsumers
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
import com.nullinnix.clippr.viewmodels.MiscViewModel
import com.nullinnix.clippr.viewmodels.SettingsViewModel
import com.nullinnix.clippr.views.App
import com.nullinnix.clippr.views.WindowBar
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
    val miscViewModel = MiscViewModel()
    val clipsViewModel = ClipsViewModel(clipsDatabase.clipsDao(), settingsViewModel, miscViewModel)

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
        if (showMain.collectAsState().value) {
            val windowState = rememberWindowState()

            Window (
                transparent = true,
                undecorated = true,
                state = windowState,
                onCloseRequest = {
                    clipsViewModel.setShowMainApp(false)
                },
                title = "Clippr",
            ) {
                var isFocused by remember { mutableStateOf(true) }
                val composeWindowState = composeWindowStateRaw.collectAsState().value
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(composeWindowState) {
                    while (true) {
                        if (!clipsViewModel.clipsState.value.isSearching) {
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
                        .onPreviewKeyEvent {event ->
                            println("event = ${event.key}")

                            if (event.type == KeyEventType.KeyDown) {
                                when (event.key) {
                                    Key.MetaLeft, Key.MetaRight -> {

                                    }

                                    Key.Escape -> {
                                        for (consumer in EscPriorityConsumers.entries) {
                                            when (consumer) {
                                                EscPriorityConsumers.FilterEsc -> {
                                                    if (clipsViewModel.clipsState.value.showFilters) {
                                                        clipsViewModel.setShowFilters(false)
                                                        break
                                                    }
                                                }

                                                EscPriorityConsumers.SearchEsc -> {
                                                    if (clipsViewModel.clipsState.value.isSearching) {
                                                        clipsViewModel.setIsSearching(false)
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Key.Enter -> {
                                        clipsViewModel.searchAndFilter(true)
                                    }
                                }
                            } else if (event.type == KeyEventType.KeyUp) {
                                when (event.key) {
                                    Key.MetaLeft, Key.MetaRight -> {

                                    }
                                }
                            }

                            !clipsViewModel.clipsState.value.isSearching && !clipsViewModel.clipsState.value.showFilters
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
                                miscViewModel = miscViewModel
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
                        pasteWithRobot(clip)
                    }
                }

                Separator()

                for (clip in others) {
                    Item(clip.content.trimIndent().trimMargin().coerce(50)) {
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
    }
}

val showMain = MutableStateFlow(false)