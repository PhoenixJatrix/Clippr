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

data class ClipsState (
    val pinnedClips: List<Clip> = emptyList(),
    val otherClips: List<Clip> = emptyList(),
    val currentTab: Tab = Tab.ClipsTab,
    val isSearching: Boolean = false,
    val searchParams: String = "",
    val selectedClips: Set<Clip> = emptySet(),
    val protoFilters: Filters = Filters(),
    val searchFilter: Filters = Filters(),
    val customFilterApplied: Boolean = false,
    val searchResults: List<Clip> = emptyList(),
    val isShowingFilters: Boolean = false,
    val isOnGoingSearch: Boolean = false
)

data class Filters (
    val types: Set<ClipType> = ClipType.entries.toSet(),
    val sources: Set<String> = emptySet(),
    val copyTime: Long? = null,
    val pinState: Boolean? = null,
    val lineCount: Int? = null,
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
    val maximumRememberableUnpinnedClips: Int = 100,
    val enableMetaShiftVPopup: Boolean = false,
    val startAtLogin: Boolean = false,
    val sourcesExceptions: Set<String> = emptySet(),
    val clipTypesExceptions: Set<ClipType> = emptySet(),
    val clipDeleteTime: ClipDeleteTime = ClipDeleteTime(TimeCode.Day, 7),
    val secondsBeforePaste: Int = 3,
    val pasteFilesAsText: Boolean = true,
)

data class MiscViewModelState (
    val allApps: Map<String, MacApp> = emptyMap(),
    val loadedIcns: Map<String, ImageBitmap> = emptyMap(),
    val metaHeldDown: Boolean = false,
    val altHeldDown: Boolean = false,
    val lastHoveredClip: Clip? = null
)

data class MacApp(
    val name: String,
    val bundleId: String,
    val iconPath: String?
)

enum class TimeCode (val secondsPer: Int, val desc: String){
    Minute(60, "Minute"),
    Hour(Minute.secondsPer * 60, "Hour"),
    Day(Hour.secondsPer * 24, "Day"),
}

@Serializable
data class ClipDeleteTime (
    val timeCode: TimeCode,
    val unit: Int
)

const val CLIP_ID = "clipID"
const val MIME_TYPE = "mimeType"

const val MIME_TYPE_PLAIN_TEXT = "text/plain"
const val MIME_TYPE_DIR = "dir/folder"