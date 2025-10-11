package com.nullinnix.clippr.viewmodels

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.MacApp
import com.nullinnix.clippr.misc.MiscViewModelState
import com.nullinnix.clippr.misc.getAllApps
import com.nullinnix.clippr.misc.loadIcns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MiscViewModel: ViewModel() {
    private val _state = MutableStateFlow(MiscViewModelState())
    val state = _state.asStateFlow()

    init {
        getAllApps { apps ->
            setAllApps(apps)
            loadIcns(apps.values.toList()) { icns ->
                setLoadedIcns(icns)
            }
        }
    }

    fun setAllApps (apps: Map<String, MacApp>) {
        _state.update {
            it.copy(allApps = apps)
        }
    }

    fun setLoadedIcns (icns: Map<String, ImageBitmap>) {
        _state.update {
            it.copy(loadedIcns = icns)
        }
    }

    fun setMetaHeldDown(value: Boolean) {
        _state.update {
            it.copy(metaHeldDown = value)
        }
    }

    fun setAltHeldDown(value: Boolean) {
        _state.update {
            it.copy(altHeldDown = value)
        }
    }

    fun setLastHoveredClip(value: Clip?) {
        _state.update {
            println("last hovered = $value")
            it.copy(lastHoveredClip = value)
        }
    }
}