package com.nullinnix.clippr.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.nullinnix.clippr.misc.Clip

class ViewModel: ViewModel() {
    companion object {
        val clips = mutableStateMapOf<String, Clip>()
        val pinnedClipKeys = mutableStateListOf<String>()
        val otherClipKeys = mutableStateListOf<String>()
        var lastCopiedItemHash = ""
    }
}