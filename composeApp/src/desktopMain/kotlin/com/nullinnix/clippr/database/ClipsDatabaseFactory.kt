package com.nullinnix.clippr.database

import ClipsDatabase
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
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
            .build()
    }
}