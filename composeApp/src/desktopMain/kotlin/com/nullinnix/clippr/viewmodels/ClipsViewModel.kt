package com.nullinnix.clippr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullinnix.clippr.database.clips.ClipsDao
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipEntity
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.ClipsState
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.focusWindow
import com.nullinnix.clippr.misc.log
import com.nullinnix.clippr.misc.monitorOldClips
import com.nullinnix.clippr.misc.onCopyToClipboard
import com.nullinnix.clippr.misc.search
import com.nullinnix.clippr.misc.toClip
import com.nullinnix.clippr.misc.toClipEntity
import com.nullinnix.clippr.showMain
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
                    state.copy(currentOtherClipsFetchOffset = state.currentOtherClipsFetchOffset + FETCH_OFFSET, otherClips = it.toClip())
                }
            }
            .launchIn(viewModelScope)

        clipsDao
            .getPinnedClips(clipsState.value.currentPinnedClipsFetchOffset + FETCH_OFFSET)
            .onEach {
                _clipsState.update { state ->
                    state.copy(currentPinnedClipsFetchOffset = state.currentPinnedClipsFetchOffset + FETCH_OFFSET, pinnedClips = it.toClip())
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

            is ClipAction.ToggleSelectClip -> {
                toggleSelectClip(action.clip)
            }

            is ClipAction.Search -> {
                searchAndFilter(action.searchParams)
            }
        }
    }

    fun addClip(clip: Clip) {
        viewModelScope.launch {
            clipsDao.upsert(clip.toClipEntity())
        }
    }

    fun deleteClip(clip: Clip) {
        viewModelScope.launch {
            clipsDao.delete(clip.toClipEntity())
        }
    }

    fun togglePinnedClip(clip: Clip) {
        addClip(clip.copy(isPinned = !clip.isPinned, pinnedAt = if (!clip.isPinned) LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) else 0L))
    }

    fun setShowMainApp(value: Boolean) {
        showMain.update {
            value
        }
    }

    fun forceShowMainApp() {
        viewModelScope.launch {
            log("force show app", "forceShowMainApp")
            setShowMainApp(false)

            delay(100)

            setShowMainApp(true)

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

    fun setIsSearching (value: Boolean) {
        _clipsState.update {
            it.copy(isSearching = value)
        }
    }

    fun setIsMultiSelecting (value: Boolean) {
        _clipsState.update {
            it.copy(isMultiSelecting = value)
        }
    }

    fun setSearchParams (value: String) {
        _clipsState.update {
            it.copy(searchParams = value)
        }
    }
    
    fun toggleSelectClip(clip: Clip) {
        println("red")
        if (clip.isPinned) {
            _clipsState.update {
                if (clip in it.selectedPinnedClips) {
                    it.copy(selectedPinnedClips = it.selectedPinnedClips - clip)
                } else {
                    it.copy(selectedPinnedClips = it.selectedPinnedClips + clip)
                }
            }
        } else {
            _clipsState.update {
                if (clip in it.selectedOtherClips) {
                    it.copy(selectedOtherClips = it.selectedOtherClips - clip)
                } else {
                    it.copy(selectedOtherClips = it.selectedOtherClips + clip)
                }
            }
        }
    }

    fun setSelectedPinnedClips (value: Set<Clip>) {
        _clipsState.update {
            it.copy(selectedPinnedClips = value)
        }
    }

    fun setSelectedOtherClips (value: Set<Clip>) {
        _clipsState.update {
            it.copy(selectedOtherClips = value)
        }
    }

    fun searchAndFilter (searchParams: String?) {
        viewModelScope.launch {
            if (searchParams != null) {
                if (searchParams.count { it == ' ' } != searchParams.length) {
                    val searchResults = search(searchParams = searchParams, filters = clipsState.value.filters, pinnedClips = clipsState.value.pinnedClips, otherClips = clipsState.value.otherClips)

                    _clipsState.update {
                        it.copy(searchResults = searchResults)
                    }
                }
            } else {
                val searchResults = search(searchParams = "", filters = clipsState.value.filters, pinnedClips = clipsState.value.pinnedClips, otherClips = clipsState.value.otherClips)

                _clipsState.update {
                    it.copy(searchResults = searchResults)
                }
            }
        }
    }
}