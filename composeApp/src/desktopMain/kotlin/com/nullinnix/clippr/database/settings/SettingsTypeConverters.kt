package com.nullinnix.clippr.database.settings

import androidx.room.TypeConverter
import com.nullinnix.clippr.misc.ClipType
import com.nullinnix.clippr.misc.SettingsState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsTypeConverters {
    @TypeConverter
    fun fromSettings(settingsState: SettingsState): String {
        return Json.encodeToString(settingsState)
    }

    @TypeConverter
    fun toSettings(json: String): SettingsState {
        return Json.decodeFromString<SettingsState>(json)
    }

    @TypeConverter
    fun fromClipType(clipType: ClipType): String {
        return Json.encodeToString(clipType)
    }

    @TypeConverter
    fun toClipType(json: String): ClipType {
        return Json.decodeFromString<ClipType>(json)
    }
}