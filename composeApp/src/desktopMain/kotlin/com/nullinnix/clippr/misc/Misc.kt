package com.nullinnix.clippr.misc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset
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
    return string.replace("\n", " ")
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

fun epochToReadableTime (epoch: Long): String {
    val timeElapsedSinceEpoch = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - epoch

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