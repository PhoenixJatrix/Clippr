package com.nullinnix.clippr.database

import ClipsDatabase
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.io.File

class ClipsDatabaseFactory {
    fun create(): ClipsDatabase {
        val userHome = System.getProperty("user.home")
        val appDataDir = File(userHome, "Library/Application Support/Clippr")

        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }

        val dbFile = File(appDataDir, "clips")
        return Room.databaseBuilder<ClipsDatabase>(dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .addMigrations(Migration1To2())
            .build()
    }
}

class Migration1To2: Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE clips ADD COLUMN pinnedAt INTEGER NOT NULL DEFAULT 0")
    }
}