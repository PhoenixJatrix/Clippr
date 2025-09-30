package com.nullinnix.clippr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullinnix.clippr.database.clips.ClipsDao
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.ClipsState
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.focusWindow
import com.nullinnix.clippr.misc.log
import com.nullinnix.clippr.misc.monitorOldClips
import com.nullinnix.clippr.misc.onCopyToClipboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

const val FETCH_OFFSET = 100

class ClipsViewModel(
    private val clipsDao: ClipsDao,
    private val settingsViewModel: SettingsViewModel
): ViewModel() {
    private val _clipsState = MutableStateFlow(ClipsState())
    val clipsState = _clipsState.asStateFlow()

    init {
        deleteUnpinnedOlderThan30()
        monitorOldClips(clipsViewModel = this@ClipsViewModel)

        clipsDao
            .getOtherClips(clipsState.value.currentOtherClipsFetchOffset + FETCH_OFFSET)
            .onEach {
                _clipsState.update { state ->
                    state.copy(currentOtherClipsFetchOffset = state.currentOtherClipsFetchOffset + FETCH_OFFSET, otherClips = it)
                }
            }
            .launchIn(viewModelScope)

        clipsDao
            .getPinnedClips(clipsState.value.currentPinnedClipsFetchOffset + FETCH_OFFSET)
            .onEach {
                _clipsState.update { state ->
                    state.copy(currentPinnedClipsFetchOffset = state.currentPinnedClipsFetchOffset + FETCH_OFFSET, pinnedClips = it)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction (action: ClipAction) {
        when (action) {
            is ClipAction.OnCopyToClipboard -> {
                onCopyToClipboard(clip = action.clip)
            }

            is ClipAction.OnDelete -> {
                deleteClip(action.clip)
            }

            is ClipAction.OnTogglePin -> {
                togglePinnedClip(action.clip)
            }

            is ClipAction.OnAddClip -> {
                if (settingsViewModel.settings.value.recordingEnabled) {
                    addClip(action.clip)
                }
            }
        }
    }

    fun addClip(clip: Clip) {
        viewModelScope.launch {
            clipsDao.upsert(clip)
        }
    }

    fun deleteClip(clip: Clip) {
        viewModelScope.launch {
            clipsDao.delete(clip)
        }
    }

    fun togglePinnedClip(clip: Clip) {
        addClip(clip.copy(isPinned = !clip.isPinned, pinnedAt = if (!clip.isPinned) LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) else 0L))
    }

    fun setShowMainApp(value: Boolean) {
        _clipsState.update {
            it.copy(showMainApp = value)
        }
    }

    fun forceShowMainApp() {
        viewModelScope.launch {
            log("force show app", "forceShowMainApp")
            _clipsState.update {
                it.copy(showMainApp = false)
            }

            delay(100)

            _clipsState.update {
                it.copy(showMainApp = true)
            }

            delay(100)

            focusWindow()
        }
    }

    fun switchTab(value: Tab) {
        _clipsState.update {
            it.copy(currentTab = value)
        }
    }

    fun deleteAllUnpinned() {
        viewModelScope.launch {
            log("delete all unpinned clips", "deleteAllUnpinned")
            clipsDao.deleteAllUnpinned()
        }
    }

    fun deleteUnpinnedOlderThan30() {
        viewModelScope.launch {
            log("delete all unpinned clips older than 30 days", "deleteUnpinnedOlderThan30")

            clipsDao.deleteUnpinnedOlderThan30(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
        }
    }
}