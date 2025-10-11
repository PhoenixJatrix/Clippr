package com.nullinnix.clippr.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nullinnix.clippr.misc.SearchAction
import com.nullinnix.clippr.misc.Tab
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
        val showFilters = clipState.showFilters

        val pagerState = rememberPagerState { 3 }
        val coroutineScope = rememberCoroutineScope()

        var onActualTabChanged by remember { mutableStateOf(false) }

        val miscViewModelState = miscViewModel.state.collectAsState().value

        val clipLazyColumnScrollState = rememberLazyListState()
        val clipSearchScrollState = rememberLazyListState()

        val secondsBeforePaste = settingsViewModel.settings.value.secondsBeforePaste

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

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
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
                        onAction = { action ->
                            when (action) {
                                SearchAction.Filter -> {
                                    clipsViewModel.setShowFilters(true)
                                }
                                SearchAction.OnExit -> {
                                    clipsViewModel.setIsSearching(false)
                                }
                                SearchAction.OnSearchStart -> {
                                    clipsViewModel.searchAndFilter(true)
                                }
                                is SearchAction.SearchParamsChanged -> {
                                    clipsViewModel.setSearchParams(action.params)
                                }
                            }
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
                                miscViewModel = miscViewModel,
                                scrollStates = Pair(clipLazyColumnScrollState, clipSearchScrollState),
                                secondsBeforePaste = secondsBeforePaste
                            )
                        }

                        Tab.SettingsTab -> {
                            Settings(
                                settingsViewModel = settingsViewModel,
                                miscViewModelState = miscViewModelState,
                                restartClipsMonitor = {
                                    clipsViewModel.monitorOldClips()
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(100.dp))
            }

            if (showFilters) {
                FilterView(
                    clipsViewModel = clipsViewModel,
                    allApps = miscViewModel.state.value.allApps,
                    loadedIcns = miscViewModelState.loadedIcns
                )
            }
        }
    }
}