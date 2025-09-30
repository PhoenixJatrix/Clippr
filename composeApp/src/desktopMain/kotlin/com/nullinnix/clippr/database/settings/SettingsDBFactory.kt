package com.nullinnix.clippr.database.settings

import ClipsDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

class SettingsDatabaseFactory {
    fun create(): SettingsDatabase {
        val userHome = System.getProperty("user.home")
        val appDataDir = File(userHome, "Library/Application Support/Clippr")

        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }

        val dbFile = File(appDataDir, "settings")
        return Room.databaseBuilder<SettingsDatabase>(dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .addMigrations()
            .build()
    }
}