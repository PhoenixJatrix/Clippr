package com.nullinnix.clippr.misc

data class Clip(
    val clipID: String,
    val content: String,
    val copiedAt: Long,
    var isPinned: Boolean,
    val mimeType: String,
    val isImage: Boolean,
    val exists: Boolean
)

const val CLIP_ID = "clipID"
const val CONTENT = "content"
const val URIS  = "uris"
const val COPIED_AT = "copiedAt"
const val IS_PINNED = "isPinned"
const val MIME_TYPE = "mimeType"

const val MIME_TYPE_PLAIN_TEXT = "text/plain"
const val MIME_TYPE_DIR = "dir/folder"