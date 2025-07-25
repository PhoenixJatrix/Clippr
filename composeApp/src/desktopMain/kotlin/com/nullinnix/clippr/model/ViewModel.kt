package com.nullinnix.clippr.model

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.nullinnix.clippr.misc.Clip

class ViewModel: ViewModel() {
    companion object {
        val clips = mutableStateMapOf<String, Clip>()
        var lastCopiedItemHash = ""
    }
}