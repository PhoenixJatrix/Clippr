package com.nullinnix.clippr.database.settings

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nullinnix.clippr.misc.SettingsClass

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