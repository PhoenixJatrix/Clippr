package com.nullinnix.clippr.misc

import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "clips")
data class ClipEntity (
    @PrimaryKey val clipID: String,
    val content: String,
    val copiedAt: Long,
    var isPinned: Boolean,
    val mimeType: String,
    val isImage: Boolean,
    val exists: Boolean,
    val pinnedAt: Long,
    val associatedIcon: String,
    val source: String? = null
)

data class Clip (
    val clipID: String,
    val content: String,
    val copiedAt: Long,
    var isPinned: Boolean,
    val mimeType: String,
    val isImage: Boolean,
    val exists: Boolean,
    val pinnedAt: Long,
    val associatedIcon: String,
    val source: String? = null,
)

data class ClipsState(
    val pinnedClips: List<Clip> = emptyList(),
    val otherClips: List<Clip> = emptyList(),
    val currentPinnedClipsFetchOffset: Int = 0,
    val currentOtherClipsFetchOffset: Int = 0,
    val showMainApp: Boolean = false,
    val currentTab: Tab = Tab.ClipsTab,
    val isSearching: Boolean = false,
    val searchParams: String = "",
    val selectedPinnedClips: Set<Clip> = emptySet(),
    val selectedOtherClips: Set<Clip> = emptySet(),
)

@Entity(tableName = "settings")
data class SettingsClass (
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val settingsState: SettingsState
)

@Serializable
data class SettingsState (
    val recordingEnabled: Boolean = true,
    val clearAllUnpinnedClipsOnDeviceStart: Boolean = false,
    val maximumRememberableUnpinnedClips: Int = 1000,
    val enableMetaShiftVPopup: Boolean = false,
    val deleteUnpinnedClipsAfter30Days: Boolean = false,
    val startAtLogin: Boolean = false
)

data class MiscViewModelState (
    val allApps: Map<String, MacApp> = emptyMap(),
    val loadedIcns: Map<String, ImageBitmap> = emptyMap()
)

data class MacApp(
    val name: String,
    val bundleId: String,
    val iconPath: String?
)

const val CLIP_ID = "clipID"
const val MIME_TYPE = "mimeType"

const val MIME_TYPE_PLAIN_TEXT = "text/plain"
const val MIME_TYPE_DIR = "dir/folder"