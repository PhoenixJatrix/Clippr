package com.nullinnix.clippr.misc

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource

@Entity(tableName = "clips")
data class Clip(
    @PrimaryKey val clipID: String,
    val content: String,
    val copiedAt: Long,
    var isPinned: Boolean,
    val mimeType: String,
    val isImage: Boolean,
    val exists: Boolean,
    val pinnedAt: Long,
    val associatedIcon: String
)

data class ClipsState(
    val pinnedClips: List<Clip> = emptyList(),
    val otherClips: List<Clip> = emptyList(),
    val currentPinnedClipsFetchOffset: Int = 0,
    val currentOtherClipsFetchOffset: Int = 0,
    val showMainApp: Boolean = false,
    val currentTab: Tab = Tab.ClipsTab
)

@Entity(tableName = "settings")
data class SettingsState (
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val recordingEnabled: Boolean = true,
    val clearAllUnpinnedClipsOnDeviceStart: Boolean = false,
    val maximumRememberableUnpinnedClips: Int = 1000,
    val enableMetaShiftVPopup: Boolean = false,
    val deleteUnpinnedClipsAfter30Days: Boolean = true
)

const val CLIP_ID = "clipID"
const val MIME_TYPE = "mimeType"

const val MIME_TYPE_PLAIN_TEXT = "text/plain"
const val MIME_TYPE_DIR = "dir/folder"