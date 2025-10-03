package com.nullinnix.clippr.database.settings

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nullinnix.clippr.misc.SettingsClass
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Upsert
    suspend fun save(SettingsClass: SettingsClass)

    @Query("SELECT * FROM settings")
    fun getSettings(): Flow<List<SettingsClass>>
}