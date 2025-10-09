package com.nullinnix.clippr.misc

sealed interface ClipAction {
    data class OnTogglePin(val clip: Clip): ClipAction
    data class OnDelete(val clip: Clip): ClipAction
    data class OnCopyToClipboard(val clip: Clip): ClipAction
    data class OnAddClip(val clip: Clip): ClipAction
    data class ToggleSelectClip(val clip: Clip): ClipAction
    data class FilterBySource(val source: String): ClipAction
    data class FilterByType(val type: ClipType): ClipAction
    object Search: ClipAction
}

sealed interface Tab {
    object ClipsTab: Tab
    object SettingsTab: Tab
}

enum class ClipType(val id: String) {
    AUDIO("audio"),
    PLAIN_TEXT("plain_text"),
    BROKEN("broken"),
    CODE("code"),
    FOLDER("folder"),
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

sealed interface SearchAction {
    data class SearchParamsChanged(val params: String): SearchAction
    object OnExit: SearchAction
    object Filter: SearchAction
    object OnSearchStart: SearchAction
}

enum class EscPriorityConsumers {
    FilterEsc,
    SearchEsc
}