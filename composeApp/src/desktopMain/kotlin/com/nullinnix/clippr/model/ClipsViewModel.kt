package com.nullinnix.clippr.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullinnix.clippr.database.ClipsDao
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.ClipsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClipsViewModel(
    private val clipsDao: ClipsDao
): ViewModel() {
    private val _clipsState = MutableStateFlow(ClipsState())
    val clipsState = _clipsState.asStateFlow()

    init {
        clipsDao
            .getClips()
            .onEach {
                println("${it.size}")

                val pinned = mutableListOf<Clip>()
                val other = mutableListOf<Clip>()

                it.forEach {
                    if (it.isPinned) {
                        pinned.add(it)
                    } else {
                        other.add(it)
                    }
                }

                _clipsState.update {
                    it.copy(pinnedClips = pinned, otherClips = other)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction (action: ClipAction) {
        when (action) {
            is ClipAction.OnAddClip -> {
                println("on action ${action::class.java.simpleName}")

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
        addClip(clip.copy(isPinned = !clip.isPinned))
    }
}