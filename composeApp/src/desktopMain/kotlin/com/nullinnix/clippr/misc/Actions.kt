package com.nullinnix.clippr.misc

sealed interface ClipAction {
    data class OnTogglePin(val clip: Clip): ClipAction
    data class OnDelete(val clip: Clip): ClipAction
    data class OnCopyToClipboard(val clip: Clip): ClipAction
    data class OnAddClip(val clip: Clip): ClipAction
    data class ToggleSelectClip(val clip: Clip): ClipAction
}

sealed interface Tab {
    object ClipsTab: Tab
    object SettingsTab: Tab
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