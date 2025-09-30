package com.nullinnix.clippr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullinnix.clippr.database.settings.SettingsDao
import com.nullinnix.clippr.misc.SettingsAction
import com.nullinnix.clippr.misc.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class SettingsViewModel (
    settingsDao: SettingsDao
): ViewModel() {
    private val _settings = MutableStateFlow(SettingsState())
    val settings = _settings.asStateFlow()

    init {
        settingsDao
            .getSettings()
            .onEach { savedSettings ->
                _settings.update {
                    savedSettings.firstOrNull() ?: SettingsState()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction (action: SettingsAction) {
        when (action) {
            SettingsAction.ToggleClearAllUnpinnedDevicesOnStart -> {
                _settings.update {
                    it.copy(clearAllUnpinnedClipsOnDeviceStart = !it.clearAllUnpinnedClipsOnDeviceStart)
                }
            }
        }
    }
}