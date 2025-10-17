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
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.nullinnix.clippr.database.clips.ClipsDatabaseFactory
import com.nullinnix.clippr.database.settings.SettingsDatabaseFactory
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.SettingsAction
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.hasAccessibilityAccessChecker
import com.nullinnix.clippr.misc.isInLoginItemsChecker
import com.nullinnix.clippr.misc.listenForCopy
import com.nullinnix.clippr.misc.log
import com.nullinnix.clippr.misc.manageKeyEvent
import com.nullinnix.clippr.misc.registerKeyStroke
import com.nullinnix.clippr.misc.toggleFullscreen
import com.nullinnix.clippr.theme.Theme
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import com.nullinnix.clippr.viewmodels.MiscViewModel
import com.nullinnix.clippr.viewmodels.NotificationsViewModel
import com.nullinnix.clippr.viewmodels.SettingsViewModel
import com.nullinnix.clippr.views.App
import com.nullinnix.clippr.views.ClipprTray
import com.nullinnix.clippr.views.WindowBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit
import java.awt.Window

fun main() {
    println("app start ${System.getenv().toList()}")
    log("app start ${System.getenv().toList()}", "main")

    val clipsDatabase = ClipsDatabaseFactory().create()
    val settingsDatabase = SettingsDatabaseFactory().create()
    val settingsViewModel = SettingsViewModel(settingsDatabase.settingsDao())
    val miscViewModel = MiscViewModel()
    val notificationsViewModel = NotificationsViewModel()
    val clipsViewModel = ClipsViewModel(clipsDatabase.clipsDao(), settingsViewModel, miscViewModel, notificationsViewModel)

    val composeWindowStateRaw = MutableStateFlow<Window?>(null)
    var coercedWindowPositionAndSize = false

    log("app started", "main")

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

    hasAccessibilityAccessChecker {
        miscViewModel.setHasAccessibilityAccess(it)
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
                        if (!clipsViewModel.clipsState.value.isSearching && clipsViewModel.clipsState.value.currentTab != Tab.SettingsTab && !clipsViewModel.clipsState.value.showClipPreview) {
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
                        .border(1.dp, color = Color.White.copy(0.25f), shape = corners(10.dp))
                        .background(Color.Black)
                        .focusRequester(focusRequester)
                        .focusable()
                        .onPreviewKeyEvent { event ->
                            manageKeyEvent(event, clipsViewModel, miscViewModel)
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
                                    if (event.key != Key.Escape)
                                        manageKeyEvent(event, clipsViewModel, miscViewModel)
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

        ClipprTray(
            scope = this,
            clipsViewModel = clipsViewModel,
            settingsViewModel = settingsViewModel,
            miscViewModel = miscViewModel
        )
    }
}


//set to false
val showMain = MutableStateFlow(true)