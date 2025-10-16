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
import com.nullinnix.clippr.misc.log
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
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        settingsDao
            .getSettings()
            .onEach { savedSettings ->
                _state.update {
                    savedSettings.firstOrNull()?.settingsState ?: SettingsState()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction (action: SettingsAction) {
        when (action) {
            SettingsAction.ToggleClearAllUnpinnedDevicesOnStart -> {
                _state.update {
                    it.copy(clearAllUnpinnedClipsOnDeviceStart = !it.clearAllUnpinnedClipsOnDeviceStart)
                }
            }

            SettingsAction.ToggleEnableMetaShiftV -> {
                _state.update {
                    it.copy(enableMetaShiftVPopup = !it.enableMetaShiftVPopup)
                }
            }

            SettingsAction.ToggleEnableClipping -> {
                _state.update {
                    it.copy(recordingEnabled = !it.recordingEnabled)
                }

                lastCopiedItemHash = ""
            }

            SettingsAction.ToggleStartAtLogin -> {
                viewModelScope.launch {
                    if (!isInLoginItems()) {
                        addToLoginItems()
                    } else {
                        removeFromLoginItems()
                    }

                    _state.update {
                        it.copy(startAtLogin = isInLoginItems())
                    }
                }
            }

            is SettingsAction.SetStartAtLogin -> {
                _state.update {
                    it.copy(startAtLogin = action.value)
                }
            }

            is SettingsAction.SetClipTypes -> {
                _state.update {
                    it.copy(clipTypesExceptions = action.value)
                }
            }

            is SettingsAction.SetSourceExceptions -> {
                _state.update {
                    it.copy(sourcesExceptions = action.value)
                }
            }

            is SettingsAction.SetClipDeleteTime -> {
                _state.update {
                    it.copy(clipDeleteTime = action.value)
                }
            }

            is SettingsAction.SetMaximumRememberableUnpinnedClips -> {
                _state.update {
                    it.copy(maximumRememberableUnpinnedClips = action.value)
                }
            }

            is SettingsAction.SetSecondsBeforePaste -> {
                _state.update {
                    it.copy(secondsBeforePaste = action.value)
                }
            }

            is SettingsAction.SetPasteFilesAsText -> {
                _state.update {
                    it.copy(pasteFilesAsText = action.value)
                }
            }

            is SettingsAction.SetAutoPaste -> {
                _state.update {
                    it.copy(autoPaste = action.value)
                }
            }
        }

        save()
    }

    fun save () {
        viewModelScope.launch {
            if (state.value.clipDeleteTime.unit > 0) {
                log("saving settings", "save")
                settingsDao.save(SettingsClass(settingsState = state.value))
            }
        }
    }
}