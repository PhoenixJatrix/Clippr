package com.nullinnix.clippr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullinnix.clippr.database.clips.ClipsDao
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.ClipType
import com.nullinnix.clippr.misc.ClipsState
import com.nullinnix.clippr.misc.Filters
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
    private val settingsViewModel: SettingsViewModel,
    private val miscViewModel: MiscViewModel
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
                searchAndFilter()
            }

            is ClipAction.FilterBySource -> {
                _clipsState.update {
                    it.copy(
                        searchParams = "",
                        protoFilters = Filters(sources = setOf(action.source), types = it.protoFilters.types),
                    )
                }

                searchAndFilter(true)
            }

            is ClipAction.FilterByType -> {
                _clipsState.update {
                    it.copy(
                        searchParams = "",
                        protoFilters = Filters(types = setOf(action.type), sources = it.protoFilters.sources),
                    )
                }

                searchAndFilter(true)
            }
        }
    }

    fun addClip(clip: Clip) {
        viewModelScope.launch {
            clipsDao.upsert(clip.toClipEntity().copy(source = if (clip.source in miscViewModel.state.value.allApps.keys) clip.source else null))
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

    fun deleteSelected() {
        viewModelScope.launch {
            log("delete all selected clips", "deleteSelected")
            clipsDao.deleteSelected(
                clipsToDelete = clipsState.value.selectedClips.map {
                    it.clipID
                }
            )
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

        if (!value) {
            _clipsState.update {
                it.copy(
                    searchParams = "",
                    selectedClips = emptySet(),
                    isOnGoingSearch = false,
                    protoFilters = Filters(sources = miscViewModel.state.value.allApps.keys),
                    searchFilter = Filters(sources = miscViewModel.state.value.allApps.keys),
                    searchResults = emptyList()
                )
            }
        }
    }

    fun setShowFilters (value: Boolean) {
        _clipsState.update {
            it.copy(showFilters = value)
        }
    }

    fun setSearchParams (value: String) {
        _clipsState.update {
            it.copy(searchParams = value, searchResults = if (value.isBlank()) emptyList() else it.searchResults)
        }

        searchAndFilter()
    }
    
    fun toggleSelectClip(clip: Clip) {
        _clipsState.update {
            if (clip in it.selectedClips) {
                it.copy(selectedClips = it.selectedClips - clip)
            } else {
                it.copy(selectedClips = it.selectedClips + clip)
            }
        }
    }

    fun setSelectedClips (value: Set<Clip>) {
        _clipsState.update {
            it.copy(selectedClips = value)
        }
    }

    fun searchAndFilter (searchWithFilters: Boolean = false) {
        viewModelScope.launch {
            _clipsState.update {
                val customFilterApplied = it.protoFilters.sources.size != miscViewModel.state.value.allApps.size || it.protoFilters.pinState != null || it.protoFilters.types.size != ClipType.entries.size || it.protoFilters.copyTime != null || it.protoFilters.lineCount != null

                it.copy(searchFilter = it.protoFilters, showFilters = false, isSearching = true, customFilterApplied = customFilterApplied)
            }

            if (clipsState.value.searchParams.isNotEmpty() || searchWithFilters) {
                println("searching start")

                _clipsState.update {
                    it.copy(isOnGoingSearch = true)
                }
                val searchResults = search(searchParams = clipsState.value.searchParams, filters = clipsState.value.searchFilter, clips = clipsState.value.pinnedClips + clipsState.value.otherClips, allApps = miscViewModel.state.value.allApps)

                delay(300)

                _clipsState.update {
                    it.copy(searchResults = searchResults, isOnGoingSearch = false)
                }

                println("searching end")
            }
        }
    }

    fun setFilters(filters: Filters) {
        _clipsState.update {
            it.copy(protoFilters = filters)
        }
    }

    fun setSearchFilters(filters: Filters) {
        _clipsState.update {
            it.copy(searchFilter = filters)
        }
    }
}