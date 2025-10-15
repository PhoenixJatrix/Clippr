package com.nullinnix.clippr.misc

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

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
    val source: String? = null,
    val edited: Boolean? = null
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
    val edited: Boolean? = null
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
    val isOnGoingSearch: Boolean = false,
    val showClipPreview: Boolean = false,
    val currentlyPreviewingClip: Clip? = null,
    val editedClip: Clip? = null,
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
    val shiftHeldDown: Boolean = false,
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

data class MergeOptions (
    val removeDuplicates: Boolean,
    val saveToDesktop: Boolean,
    val trim: Boolean,
    val copyAfterMerge: Boolean,
    val deleteOriginal: Boolean
)

sealed interface NotificationType {
    data class Info(val color: Color = Color.Black): NotificationType
    data class Warning(val color: Color = Color.Red): NotificationType
    data class DelayedOperation(val color: Color = Color.Yellow, val delay: Int, val action: String): NotificationType
}

data class Notification (
    val duration: Long,
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val content: String,
    val startedAt: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
)

data class NotificationsState (
    val notifications: List<Notification> = emptyList(),
    val pendingNotifications: List<Notification> = emptyList()
)

const val MIME_TYPE_PLAIN_TEXT = "text/plain"
const val MIME_TYPE_DIR = "dir/folder"