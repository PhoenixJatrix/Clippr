package com.nullinnix.clippr.database.settings

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nullinnix.clippr.database.clips.ClipsDao
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.SettingsClass
import com.nullinnix.clippr.misc.SettingsState

@Database(
    entities = [SettingsClass::class],
    version = 2,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(SettingsTypeConverters::class)
abstract class SettingsDatabase: RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
}