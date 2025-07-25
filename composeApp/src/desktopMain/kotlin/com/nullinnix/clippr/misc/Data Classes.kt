package com.nullinnix.clippr.misc

import org.jetbrains.skia.Bitmap

data class Clip(
    val clipID: String,
    val text: String,
    val uris: List<String>,
    val copiedAt: Long,
    var isPinned: Boolean,
    val mimeType: String,
    val isImage: Boolean
)

const val CLIP_ID = "clipID"
const val TEXT = "text"
const val URIS  = "uris"
const val COPIED_AT = "copiedAt"
const val IS_PINNED = "isPinned"
const val MIME_TYPE = "mimeType"