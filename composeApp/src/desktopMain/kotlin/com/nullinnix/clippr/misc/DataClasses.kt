package com.nullinnix.clippr.misc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clips")
data class Clip(
    @PrimaryKey val clipID: String,
    val content: String,
    val copiedAt: Long,
    var isPinned: Boolean,
    val mimeType: String,
    val isImage: Boolean,
    val exists: Boolean,
    val pinnedAt: Long
)

data class ClipsState(
    val pinnedClips: List<Clip> = emptyList(),
    val otherClips: List<Clip> = emptyList(),
    val currentPinnedClipsFetchOffset: Int = 0,
    val currentOtherClipsFetchOffset: Int = 0,
    val showMainApp: Boolean = false,
    val currentTab: Tab = Tab.ClipsTab
)

const val CLIP_ID = "clipID"
const val MIME_TYPE = "mimeType"

const val MIME_TYPE_PLAIN_TEXT = "text/plain"
const val MIME_TYPE_DIR = "dir/folder"