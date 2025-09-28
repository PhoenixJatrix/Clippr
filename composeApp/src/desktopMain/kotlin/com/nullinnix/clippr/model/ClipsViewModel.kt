package com.nullinnix.clippr.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullinnix.clippr.database.ClipsDao
import com.nullinnix.clippr.focusWindow
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.ClipsState
import com.nullinnix.clippr.misc.Tab
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.awt.Window
import java.time.LocalDateTime
import java.time.ZoneOffset

const val FETCH_OFFSET = 100

class ClipsViewModel(
    private val clipsDao: ClipsDao
): ViewModel() {
    private val _clipsState = MutableStateFlow(ClipsState())
    val clipsState = _clipsState.asStateFlow()

    init {
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
            is ClipAction.OnAddClip -> {
                addClip(action.clip)
            }

            is ClipAction.OnDelete -> {
                deleteClip(action.clip)
            }

            is ClipAction.OnTogglePin -> {
                togglePinnedClip(action.clip)
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
}