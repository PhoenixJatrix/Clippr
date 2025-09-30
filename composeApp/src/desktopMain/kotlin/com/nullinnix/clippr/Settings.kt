package com.nullinnix.clippr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import com.nullinnix.clippr.viewmodels.SettingsViewModel

@Composable
fun Settings (
    settingsViewModel: SettingsViewModel
) {
    val settingsState = settingsViewModel.settings.collectAsState().value
}