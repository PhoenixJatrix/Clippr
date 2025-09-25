package com.nullinnix.clippr.misc

//open class ClipActions(id: String) {}
//val COPY = ClipActions("copy")
//val LONG_CLICK_SELECT = ClipActions("long_click_select")
//val TOGGLE_PIN = ClipActions("toggle_pin")

sealed interface ClipAction {
    data class OnAddClip(val clip: Clip): ClipAction
    data class OnTogglePin(val clip: Clip): ClipAction
    data class OnDelete(val clip: Clip): ClipAction
}
