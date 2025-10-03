package com.nullinnix.clippr

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
import androidx.compose.ui.unit.dp
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.theme.White
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import com.nullinnix.clippr.viewmodels.MiscViewModel
import com.nullinnix.clippr.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App (
    isFocused: Boolean,
    clipsViewModel: ClipsViewModel,
    settingsViewModel: SettingsViewModel,
    miscViewModel: MiscViewModel
) {
    MaterialTheme {
        val clipState = clipsViewModel.clipsState.collectAsState().value
        val currentTab =  clipState.currentTab

        val pagerState = rememberPagerState { 2 }
        val coroutineScope = rememberCoroutineScope()

        var onActualTabChanged by remember { mutableStateOf(false) }

        LaunchedEffect(currentTab) {
            pagerState.animateScrollToPage(
                when (currentTab) {
                    Tab.ClipsTab -> 0
                    Tab.SettingsTab -> 1
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

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(15.dp))

            Tabs (
                isFocused = isFocused,
                currentTab = currentTab
            ) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = when (it) {
                            Tab.ClipsTab -> 0
                            Tab.SettingsTab -> 1
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            HorizontalPager (
                state = pagerState
            ) {
                when (currentTab) {
                    Tab.ClipsTab -> {
                        Clips (
                            clipsViewModel = clipsViewModel,
                            miscViewModel = miscViewModel
                        )
                    } else -> {
                        Settings (
                            settingsViewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }
}