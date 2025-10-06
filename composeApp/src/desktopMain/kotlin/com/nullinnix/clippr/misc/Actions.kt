package com.nullinnix.clippr.misc

sealed interface ClipAction {
    data class OnTogglePin(val clip: Clip): ClipAction
    data class OnDelete(val clip: Clip): ClipAction
    data class OnCopyToClipboard(val clip: Clip): ClipAction
    data class OnAddClip(val clip: Clip): ClipAction
    data class ToggleSelectClip(val clip: Clip): ClipAction
    data class Search(val searchParams: String): ClipAction
}

sealed interface Tab {
    object ClipsTab: Tab
    object SettingsTab: Tab
}

//const val AUDIO = "audio"
//const val BLANK = "blank"
//const val BROKEN = "broken"
//const val CODE = "code"
//const val DIRECTORY = "directory"
//const val IMAGE = "image"
//const val RUNNABLE = "runnable"
//const val TEXT = "text"
//const val UNKNOWN = "unknown"
//const val VIDEO = "video"
//const val WEB = "web"
//const val ZIP = "zip"

enum class ClipType(val id: String) {
    AUDIO("audio"),
    BLANK("blank"),
    BROKEN("broken"),
    CODE("code"),
    DIRECTORY("directory"),
    IMAGE("image"),
    RUNNABLE("runnable"),
    TEXT("text"),
    UNKNOWN("unknown"),
    VIDEO("video"),
    WEB("web"),
    ZIP("zip");
}

fun Tab.name(): String {
    return when (this) {
        Tab.ClipsTab -> "Clips"
        Tab.SettingsTab -> "Settings"
    }
}

sealed interface SettingsAction {
    object ToggleClearAllUnpinnedDevicesOnStart: SettingsAction
    object ToggleEnableMetaShiftV: SettingsAction
    object ToggleEnableClipping: SettingsAction
    object ToggleDeleteUnpinnedAfter30: SettingsAction
    object ToggleStartAtLogin: SettingsAction
    data class SetStartAtLogin(val value: Boolean): SettingsAction
}

sealed interface Filter {
    data class ByType(val clipTypes: List<ClipType>): Filter
    data class BySource(val source: String): Filter
    data class ByCopyTime(val copyTime: Long): Filter
    data class ByPinState(val state: Boolean): Filter
    data class ByLineCount(val count: Int): Filter
}