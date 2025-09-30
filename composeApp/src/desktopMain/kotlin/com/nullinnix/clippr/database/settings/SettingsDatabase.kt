package com.nullinnix.clippr.database.settings

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nullinnix.clippr.database.clips.ClipsDao
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.SettingsState

@Database(
    entities = [SettingsState::class],
    version = 1,
    exportSchema = true
)
abstract class SettingsDatabase: RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
}