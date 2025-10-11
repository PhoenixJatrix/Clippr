package com.nullinnix.clippr.misc

import kotlinx.serialization.Serializable

sealed interface ClipAction {
    data class OnTogglePin(val clip: Clip): ClipAction
    data class OnDelete(val clip: Clip): ClipAction
    data class OnCopyToClipboard(val clip: Clip, val altHeldDown: Boolean): ClipAction
    data class OnAddClip(val clip: Clip): ClipAction
    data class ToggleSelectClip(val clip: Clip): ClipAction
    data class FilterBySource(val source: String): ClipAction
    data class FilterByType(val type: ClipType): ClipAction
    object Search: ClipAction
}

enum class ClipMenuAction() {
    PasteAsText,
    PasteAsFile,
    CopyAsText,
    CopyAsFile,
    Pin,
    Unpin,
    Preview,
    OpenAsLink,
    RevealInFinder,
    Delete
}

//for when searching. no clip highlight just add number of copied on top the menus. when right click on other that is not selected, remove all selected

//paste one by one in x seconds
//copy all files
//copy all text, sep by new line
//pin all
//unpin all
//merge -> comma separated, new line separated, numbered separation, no separation, space separation, tab sep
//delete all

sealed interface Tab {
    object ClipsTab: Tab
    object SettingsTab: Tab
}

@Serializable
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
    object ToggleStartAtLogin: SettingsAction
    data class SetStartAtLogin(val value: Boolean): SettingsAction
    data class SetClipTypes(val value: Set<ClipType>): SettingsAction
    data class SetSourceExceptions(val value: Set<String>): SettingsAction
    data class SetClipDeleteTime(val value: ClipDeleteTime): SettingsAction
    data class SetMaximumRememberableUnpinnedClips(val value: Int): SettingsAction
    data class SetSecondsBeforePaste(val value: Int): SettingsAction
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