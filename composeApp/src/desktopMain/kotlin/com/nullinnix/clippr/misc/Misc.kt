package com.nullinnix.clippr.misc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import com.nullinnix.clippr.viewmodels.MiscViewModel
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Locale

fun String.hash(): String {
    val digest = MessageDigest.getInstance("SHA-256")

    for (char in this) {
        digest.update((char).code.toByte())
    }

    return digest.digest().joinToString("") { "%02x".format(it) }
}

fun Collection<Any>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (item in this@toJsonArray) {
            add(JsonPrimitive(item.toString()))
        }
    }
}

fun JsonArray.toStringList(): List<String> {
    return this.map { it.jsonPrimitive.content }
}

fun JsonObject.getData(key: String): String {
    return if (this.containsKey(key)) this[key]!!.jsonPrimitive.content else ""
}

fun formatText(string: String): String {
    return string.replace("\n", " ").trimIndent().trimMargin()
}

fun formatMillisToTime(millis: Long, returnEmptyStrIfNoUsage: Boolean = true, includeSeconds: Boolean = false): String {
    val seconds = millis / 1000
    val minutes = (seconds / 60) % 60
    val hours = (seconds / 60) / 60

    var hoursStr = ""
    var minutesStr = ""
    var secondsStr = ""

    if (hours > 0) {
        hoursStr = " ${hours}h"
    }

    if (minutes > 0) {
        minutesStr = " ${minutes}m"
    }

    if (includeSeconds) {
        secondsStr = " ${seconds % 60}s"
    }

    return if (hoursStr.isEmpty() && minutesStr.isEmpty()) {
        if (includeSeconds) {
            secondsStr
        } else {
            if (returnEmptyStrIfNoUsage) {
                ""
            } else {
                " 0s"
            }
        }
    } else {
        "${hoursStr}${minutesStr}${secondsStr}"
    }
}

fun formatSecondsToDays(secondsToFormat: Long): String {
    val seconds = secondsToFormat % 60
    val minutes = (secondsToFormat / 60) % 60
    val hours = ((secondsToFormat / 60) / 60) % 24
    val days = ((secondsToFormat / 60) / 60) / 24

    var hoursStr = ""
    var daysStr = ""
    var minutesStr = ""
    var secondsStr = " 0s"

    if (days > 0) {
        daysStr = " ${days}d"
    }

    if (hours > 0) {
        hoursStr = " ${hours}h"
    }

    if (minutes > 0) {
        minutesStr = " ${minutes}m"
    }

    if (seconds > 0) {
        secondsStr = " ${seconds % 60}s"
    }

    return if (daysStr.isEmpty() && hoursStr.isEmpty() && minutesStr.isEmpty()) {
        return secondsStr
    } else {
        "${daysStr}${hoursStr}${minutesStr}${secondsStr}"
    }
}

fun formatMinutesToTime(minutes: Long): String {
    val m = minutes % 60
    val hours = minutes / 60

    var hoursStr = ""
    var minutesStr = ""

    if (hours > 0) {
        hoursStr = hours.toString() + "h"
    }

    if (m > 0) {
        minutesStr = " ${m}m"
    }

    return if (hoursStr.isEmpty() && minutesStr.isEmpty()) "0m" else "${hoursStr}${minutesStr}"
}

fun formatSignificantUnitsOnly(millis: Long): String {
    val seconds = millis / 1000
    val minutes = (seconds / 60) % 60
    val hours = (seconds / 60) / 60

    var hoursStr = ""
    var minutesStr = ""
    var secondsStr = ""

    if (hours >= 1) {
        hoursStr = hours.toString() + "h "
    }

    if (minutes >= 1) {
        minutesStr = "${minutes}m "
    }

    if (seconds >= 0) {
        secondsStr = "${seconds % 60}s "
    }

    if (hours >= 1) {
        return hoursStr
    }

    if (minutes >= 1) {
        return minutesStr
    }

    if (seconds % 60 > 0) {
        return secondsStr
    }

    return "0s"
}

@Composable
fun corners(
    top: Dp = 15.dp,
    bottom: Dp = 15.dp,
): RoundedCornerShape {
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

@Composable
fun corners(
    all: Dp = 15.dp
): RoundedCornerShape {
    return RoundedCornerShape(all)
}

fun Modifier.noGleamTaps(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val emptyClick = {}
    this then Modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = if (enabled) onClick else emptyClick
    )
}

fun Modifier.noGleamCombinedClickable(enabled: Boolean = true, onClick: () -> Unit = {}, onLongClick: () -> Unit = {}, onDoubleClick: () -> Unit = {}): Modifier = composed {
    val emptyClick = {}
    this then Modifier.combinedClickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = if (enabled) onClick else emptyClick,
        onLongClick = if (enabled) onLongClick else emptyClick,
        onDoubleClick = if (enabled) onDoubleClick else emptyClick
    )
}

fun epochToReadableTime (epoch: Long): String {

    //todo
    val dateTime = LocalDateTime.now()
    val timeElapsedSinceEpoch = dateTime.toEpochSecond(ZoneOffset.UTC) - epoch

    val endOfToday = LocalDateTime.of(dateTime.year, dateTime.month, dateTime.dayOfMonth, 23, 59, 59)

    return when (timeElapsedSinceEpoch) {
        in Long.MIN_VALUE..10800L -> "${formatSignificantUnitsOnly(timeElapsedSinceEpoch * 1000).trim()} ago"
        in 10800L..86400 -> "Today"
        in 86400..86400L * 2 -> "Yesterday"
        else -> {
            val constructedDayTime = LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC)
            "${constructedDayTime.dayOfWeek.name.substring(0, 3).lowercase().capitalize(Locale.ROOT)}, ${constructedDayTime.dayOfMonth} ${constructedDayTime.month.name.lowercase().capitalize(Locale.ROOT)} ${constructedDayTime.year}"
        }
    }
}

fun String.coerce(maxChar: Int, addEllipses: Boolean = true): String {
    return run {
        if (this.length <= maxChar)
            this
        else {
            if (addEllipses) {
                this.substring(0, maxChar - 3) + "..."
            } else {
                this.substring(0, maxChar)
            }
        }
    }
}

fun log(content: String, from: String) {
    val time = Calendar.getInstance().time.toString()
    val fullMessage = "MESSAGE: $content\nFROM: $from\nAT: $time\n\n"

    val file = File(System.getProperty("user.home"), "Library/Application Support/Clippr/log.txt")
    file.appendText(fullMessage)
}

fun ClipEntity.toClip(): Clip {
    return Clip (
        clipID = this.clipID,
        content = this.content,
        copiedAt = this.copiedAt,
        isPinned = this.isPinned,
        mimeType = this.mimeType,
        isImage = this.isImage,
        exists = this.exists,
        pinnedAt = this.pinnedAt,
        associatedIcon = this.associatedIcon,
        source = this.source,
        edited = this.edited
    )
}

fun Clip.toClipEntity(): ClipEntity {
    return ClipEntity (
        clipID = this.clipID,
        content = this.content,
        copiedAt = this.copiedAt,
        isPinned = this.isPinned,
        mimeType = this.mimeType,
        isImage = this.isImage,
        exists = this.exists,
        pinnedAt = this.pinnedAt,
        associatedIcon = this.associatedIcon,
        source = this.source,
        edited = this.edited
    )
}

fun List<ClipEntity>.toClip(): List<Clip> {
    return this.map {
        it.toClip()
    }
}

fun List<Clip>.toClipEntity(): List<ClipEntity> {
    return this.map {
        it.toClipEntity()
    }
}

fun String.toClipType(): ClipType {
    return ClipType.entries.find { it.id == this } ?: ClipType.UNKNOWN
}

fun LocalDateTime.startOfDay(): LocalDateTime {
    return LocalDateTime.of(this.year, this.month, this.dayOfMonth, 0, 0, 0)
}

fun highlightedAnnotatedString (
    text: String,
    keywords: List<String>,
    caseSensitive: Boolean = false,
    wholeWords: Boolean = false,
    highlightColor: Color = Color.Red
): AnnotatedString {
    val builder = AnnotatedString.Builder()

    if (text.isEmpty() || keywords.isEmpty()) {
        builder.append(text)
        return builder.toAnnotatedString()
    }

    val normalizedKeywords = keywords
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .ifEmpty {
            builder.append(text)
            return builder.toAnnotatedString()
        }

    val source = if (caseSensitive) text else text.lowercase()
    val keywordsNorm = if (caseSensitive) normalizedKeywords else normalizedKeywords.map { it.lowercase() }

    val ranges = mutableListOf<IntRange>()
    for (kw in keywordsNorm) {
        var searchStart = 0
        while (true) {
            val idx = source.indexOf(kw, startIndex = searchStart)
            if (idx < 0) break

            val end = idx + kw.length
            val ok = if (wholeWords) {
                val beforeOk = idx == 0 || !source[idx - 1].isLetterOrDigit() && source[idx - 1] != '_'
                val afterOk = end == source.length || !source[end].isLetterOrDigit() && source[end] != '_'
                beforeOk && afterOk
            } else true

            if (ok) ranges.add(idx until end + 1)
            searchStart = idx + 1
        }
    }

    if (ranges.isEmpty()) {
        builder.append(text)
        return builder.toAnnotatedString()
    }

    val merged = ranges
        .sortedWith(compareBy<IntRange> { it.first }.thenByDescending { it.last })
        .fold(mutableListOf<IntRange>()) { acc, r ->
            if (acc.isEmpty()) acc.add(r)
            else {
                val last = acc.last()
                if (r.first <= last.last) {
                    acc[acc.lastIndex] = last.first..maxOf(last.last, r.last)
                } else acc.add(r)
            }
            acc
        }

    val firstMatchStart = merged.first().first
    val startOffset = firstMatchStart.coerceAtLeast(0)
    val visibleText = text.substring(startOffset)
    val prefix = if (startOffset > 0) "… " else ""

    builder.append(prefix)
    builder.append(visibleText)

    val offsetDelta = prefix.length - startOffset

    for (range in merged) {
        val start = range.first + offsetDelta
        val end = range.last + offsetDelta
        if (start < builder.length && end > 0) {
            builder.addStyle(
                SpanStyle(color = highlightColor),
                start.coerceAtLeast(0),
                end.coerceAtMost(builder.length)
            )
        }
    }

    return builder.toAnnotatedString()
}

fun manageKeyEvent(event: KeyEvent, clipsViewModel: ClipsViewModel, miscViewModel: MiscViewModel): Boolean {
    var intercepted = false
    val clipsState = clipsViewModel.clipsState.value
    val miscViewModelState = miscViewModel.state.value

    if (event.type == KeyEventType.KeyDown) {
        when (event.key) {
            Key.MetaLeft, Key.MetaRight -> {
//                intercepted = true
                miscViewModel.setMetaHeldDown(true)
            }

            Key.AltLeft, Key.AltRight -> {
//                intercepted = true
                miscViewModel.setAltHeldDown(true)
            }

            Key.ShiftLeft, Key.ShiftRight -> {
//                intercepted = true
                miscViewModel.setShiftHeldDown(true)
            }

            Key.V -> {
                if ((miscViewModelState.metaHeldDown || miscViewModelState.altHeldDown) && clipsState.currentTab == Tab.ClipsTab && miscViewModelState.lastHoveredClip != null) {
                    if (clipsState.isSearching) {
                        if (miscViewModelState.metaHeldDown && clipsState.selectedClips.size > 1) {
                            intercepted = true
                            clipsViewModel.onMultiSelectAction(MultiSelectClipMenuAction.Paste)
                        }
                    } else {
                        if (miscViewModelState.metaHeldDown) {
                            clipsViewModel.onClipMenuAction(ClipMenuAction.PasteAsText, miscViewModelState.lastHoveredClip)
                        } else {
                            clipsViewModel.onClipMenuAction(ClipMenuAction.PasteAsFile, miscViewModelState.lastHoveredClip)
                        }
                        intercepted = true
                    }
                }
            }

            Key.C -> {
                if ((miscViewModelState.metaHeldDown || miscViewModelState.altHeldDown) && clipsState.currentTab == Tab.ClipsTab && miscViewModelState.lastHoveredClip != null) {
                    if (clipsState.isSearching) {
                        if (miscViewModelState.metaHeldDown && clipsState.selectedClips.size > 1) {
                            intercepted = true
                            clipsViewModel.onMultiSelectAction(MultiSelectClipMenuAction.CopyFiles)
                        }
                    } else {
                        intercepted = true

                        if (miscViewModelState.metaHeldDown) {
                            clipsViewModel.onClipMenuAction(ClipMenuAction.CopyAsText, miscViewModelState.lastHoveredClip)
                        } else {
                            clipsViewModel.onClipMenuAction(ClipMenuAction.CopyAsFile, miscViewModelState.lastHoveredClip)
                        }
                    }
                }
            }

            Key.P -> {
                if ((miscViewModelState.metaHeldDown || miscViewModelState.altHeldDown) && clipsState.currentTab == Tab.ClipsTab && miscViewModelState.lastHoveredClip != null) {
                    if (clipsState.isSearching) {
                        if (clipsState.selectedClips.size > 1) {
                            intercepted = true

                            if (miscViewModelState.metaHeldDown) {
                                clipsViewModel.onMultiSelectAction(MultiSelectClipMenuAction.PinAll)
                            } else {
                                clipsViewModel.onMultiSelectAction(MultiSelectClipMenuAction.UnpinAll)
                            }
                        }
                    } else {
                        intercepted = true
                        clipsViewModel.onClipMenuAction(if (miscViewModelState.lastHoveredClip.isPinned) ClipMenuAction.Unpin else ClipMenuAction.Pin, miscViewModelState.lastHoveredClip)
                    }
                }
            }

            Key.S -> {
                if ((miscViewModelState.metaHeldDown || miscViewModelState.shiftHeldDown) && clipsState.currentTab == Tab.ClipsTab && miscViewModelState.lastHoveredClip != null && clipsState.showClipPreview && clipsState.currentlyPreviewingClip != null) {
                    if (miscViewModelState.metaHeldDown && miscViewModelState.shiftHeldDown) {
                        intercepted = true
                        clipsViewModel.onSaveAction(SaveAs.SaveAsCopy)
                    } else {
                        intercepted = true
                        clipsViewModel.onSaveAction(SaveAs.Save)
                    }
                }
            }

            Key.Enter -> {
                if (clipsState.isShowingFilters) {
                    intercepted = true
                    clipsViewModel.searchAndFilter(true)
                } else if (clipsState.currentTab == Tab.ClipsTab && miscViewModelState.lastHoveredClip != null && !clipsState.showClipPreview) {
                    if (!clipsState.isSearching && miscViewModelState.metaHeldDown) {
                        val action = if (miscViewModelState.lastHoveredClip.associatedIcon.toClipType() == ClipType.WEB) {
                            ClipMenuAction.OpenAsLink
                        } else {
                            if (miscViewModelState.lastHoveredClip.associatedIcon.toClipType() != ClipType.PLAIN_TEXT) {
                                ClipMenuAction.RevealInFinder
                            } else {
                                null
                            }
                        }

                        if (action != null) {
                            intercepted = true
                            clipsViewModel.onClipMenuAction(action, miscViewModelState.lastHoveredClip)
                        }
                    }

                    if (!miscViewModelState.metaHeldDown){
                        intercepted = true
                        clipsViewModel.onClipMenuAction(ClipMenuAction.Edit, miscViewModelState.lastHoveredClip)
                    }
                }
            }

            Key.Backspace -> {
                if (miscViewModelState.metaHeldDown) {
                    if (clipsState.selectedClips.isNotEmpty() && clipsState.isSearching) {
                        intercepted = true

                        if (showConfirmDialog("Delete selected clips?", "${clipsState.selectedClips.size }${if (clipsState.selectedClips.size == 1) " clip" else " clips"} will be deleted", false)) {
                            clipsViewModel.deleteSelected()
                            clipsViewModel.setIsSearching(false)
                        }
                    } else {
                        if (!clipsState.isSearching && clipsState.currentTab == Tab.ClipsTab && miscViewModelState.lastHoveredClip != null) {
                            intercepted = true

                            if (showConfirmDialog("Delete clip", "'${miscViewModelState.lastHoveredClip.content.coerce(50)}' will be deleted", false)) {
                                clipsViewModel.onClipMenuAction(ClipMenuAction.Delete, miscViewModelState.lastHoveredClip)
                            }
                        }
                    }
                }
            }

            Key.Escape -> {
                for (consumer in EscPriorityConsumers.entries) {
                    when (consumer) {
                        EscPriorityConsumers.FilterEsc -> {
                            if (clipsState.isShowingFilters) {
                                intercepted = true
                                clipsViewModel.setShowFilters(false)
                                break
                            }
                        }

                        EscPriorityConsumers.SearchEsc -> {
                            if (clipsState.isSearching) {
                                intercepted = true
                                clipsViewModel.setIsSearching(false)
                                break
                            }
                        }

                        EscPriorityConsumers.PreviewEsc -> {
                            if (clipsState.showClipPreview) {
                                intercepted = true
                                clipsViewModel.setShowClipPreview(false)
                                break
                            }
                        }
                    }
                }
            }

            Key.F -> {
                if (miscViewModelState.metaHeldDown && clipsState.currentTab == Tab.ClipsTab) {
                    intercepted = true
                    clipsViewModel.setIsSearching(true)
                }
            }

            Key.Comma -> {
                if (miscViewModelState.metaHeldDown && !clipsState.isSearching && clipsState.currentTab == Tab.ClipsTab) {
                    intercepted = true
                    clipsViewModel.switchTab(Tab.SettingsTab)
                }
            }

            Key.A -> {
                if (miscViewModelState.metaHeldDown && clipsState.isSearching && clipsState.searchResults.isNotEmpty()) {
                    intercepted = true
                    clipsViewModel.setSelectedClips(clipsState.searchResults.toSet())
                }
            }
        }

        println("event = ${event.key}. ${if (intercepted) "intercepted" else "passed to children"}")
    } else if (event.type == KeyEventType.KeyUp) {
        when (event.key) {
            Key.MetaLeft, Key.MetaRight -> {
//                intercepted = true
                miscViewModel.setMetaHeldDown(false)
            }

            Key.AltLeft, Key.AltRight -> {
//                intercepted = true
                miscViewModel.setAltHeldDown(false)
            }

            Key.ShiftLeft, Key.ShiftRight -> {
//                intercepted = true
                miscViewModel.setShiftHeldDown(false)
            }
        }
    }

    return intercepted
}

fun LocalDateTime.epoch (): Long {
    return this.toEpochSecond(ZoneOffset.UTC)
}

@Composable
fun Modifier.relaxedShadow (
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor,
): Modifier {
    val density = LocalDensity.current

    val windowWidth = with(density) {LocalWindowInfo.current.containerSize.width.toDp()}

    return this
        .then(
            Modifier.shadow(
                elevation = if (windowWidth > 500.dp) elevation / 2 else elevation,
                shape = shape,
                clip = clip,
                ambientColor = ambientColor,
                spotColor = spotColor
            )
        )
}