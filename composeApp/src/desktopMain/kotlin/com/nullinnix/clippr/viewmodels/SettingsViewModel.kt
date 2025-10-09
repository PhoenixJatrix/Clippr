package com.nullinnix.clippr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullinnix.clippr.database.settings.SettingsDao
import com.nullinnix.clippr.misc.SettingsAction
import com.nullinnix.clippr.misc.SettingsClass
import com.nullinnix.clippr.misc.SettingsState
import com.nullinnix.clippr.misc.addToLoginItems
import com.nullinnix.clippr.misc.isInLoginItems
import com.nullinnix.clippr.misc.lastCopiedItemHash
import com.nullinnix.clippr.misc.removeFromLoginItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel (
    private val settingsDao: SettingsDao
): ViewModel() {
    private val _settings = MutableStateFlow(SettingsState())
    val settings = _settings.asStateFlow()

    init {
        settingsDao
            .getSettings()
            .onEach { savedSettings ->
                _settings.update {
                    savedSettings.firstOrNull()?.settingsState ?: SettingsState()
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

            SettingsAction.ToggleEnableMetaShiftV -> {
                _settings.update {
                    it.copy(enableMetaShiftVPopup = !it.enableMetaShiftVPopup)
                }
            }

            SettingsAction.ToggleEnableClipping -> {
                _settings.update {
                    it.copy(recordingEnabled = !it.recordingEnabled)
                }

                lastCopiedItemHash = ""
            }

            SettingsAction.ToggleDeleteUnpinnedAfter30 -> {
                _settings.update {
                    it.copy(deleteUnpinnedClipsAfter30Days = !it.deleteUnpinnedClipsAfter30Days)
                }
            }

            SettingsAction.ToggleStartAtLogin -> {
                viewModelScope.launch {
                    if (!isInLoginItems()) {
                        addToLoginItems()
                    } else {
                        removeFromLoginItems()
                    }

                    _settings.update {
                        it.copy(startAtLogin = isInLoginItems())
                    }
                }
            }

            is SettingsAction.SetStartAtLogin -> {
                _settings.update {
                    it.copy(startAtLogin = action.value)
                }
            }

            is SettingsAction.SetClipTypes -> {
                _settings.update {
                    it.copy(clipTypesExceptions = action.value)
                }
            }

            is SettingsAction.SetSourceExceptions -> {
                _settings.update {
                    it.copy(sourcesExceptions = action.value)
                }
            }
        }

        save()
    }

    fun save () {
        viewModelScope.launch {
            settingsDao.save(SettingsClass(settingsState = settings.value))
        }
    }
}