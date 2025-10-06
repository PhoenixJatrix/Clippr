package com.nullinnix.clippr.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.theme.White
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import com.nullinnix.clippr.viewmodels.MiscViewModel
import com.nullinnix.clippr.viewmodels.SettingsViewModel
import com.nullinnix.clippr.views.tabs.Clips
import com.nullinnix.clippr.views.tabs.Settings
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Window

@Composable
@Preview
fun App (
    window: Window,
    isFocused: Boolean,
    clipsViewModel: ClipsViewModel,
    settingsViewModel: SettingsViewModel,
    miscViewModel: MiscViewModel
) {
    MaterialTheme {
        val clipState = clipsViewModel.clipsState.collectAsState().value
        val currentTab =  clipState.currentTab
        val isSearching = clipState.isSearching

        val pagerState = rememberPagerState { 3 }
        val coroutineScope = rememberCoroutineScope()

        var onActualTabChanged by remember { mutableStateOf(false) }

        val focusRequester by remember {
            mutableStateOf(FocusRequester())
        }

        LaunchedEffect(currentTab) {
            pagerState.animateScrollToPage(
                when (currentTab) {
                    Tab.ClipsTab -> 0
                    Tab.SettingsTab -> 2
                }
            )
        }

        LaunchedEffect(pagerState.currentPage) {
            if (onActualTabChanged) {
                clipsViewModel.switchTab(when (pagerState.currentPage) {
                    0 -> Tab.ClipsTab
                    else -> Tab.SettingsTab
                })
            }

            onActualTabChanged = true
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        println("event = ${event.key}")
                        when {
                            event.key == Key.MetaLeft || event.key == Key.MetaRight -> {
                                clipsViewModel.setIsMultiSelecting(true)
                            }
                        }
                    } else if (event.type == KeyEventType.KeyUp) {
                        when {
                            event.key == Key.MetaLeft || event.key == Key.MetaRight -> {
                                clipsViewModel.setIsMultiSelecting(false)
                            }
                        }
                    }

                    true
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(15.dp))

            AnimatedVisibility(!isSearching, enter = fadeIn(tween(200)) + expandHorizontally(tween(200))) {
                Tabs (
                    isFocused = isFocused,
                    currentTab = currentTab
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            page = when (it) {
                                Tab.ClipsTab -> 0
                                Tab.SettingsTab -> 2
                            }
                        )
                    }
                }
            }

            if (!isSearching) {
                Spacer(Modifier.height(15.dp))
            }

            AnimatedVisibility(currentTab == Tab.ClipsTab) {
                SearchBar (
                    window = window,
                    isSearching = isSearching,
                    clipState = clipState,
                    onSearchStart = {
                        clipsViewModel.setIsSearching(true)
                    },
                    onSearchParamsChanged = {
                        clipsViewModel.setSearchParams(it)
                    },
                    onExitSearch = {
                        clipsViewModel.setIsSearching(false)
                    }
                )

                Spacer(Modifier.height(10.dp))
            }

            HorizontalPager (
                state = pagerState
            ) {
                when (currentTab) {
                    Tab.ClipsTab -> {
                        Clips (
                            clipsViewModel = clipsViewModel,
                            miscViewModel = miscViewModel
                        )
                    }

                    Tab.SettingsTab -> {
                        Settings(
                            settingsViewModel = settingsViewModel
                        )
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}