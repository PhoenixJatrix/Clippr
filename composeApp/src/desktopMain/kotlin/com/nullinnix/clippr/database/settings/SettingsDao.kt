package com.nullinnix.clippr.database.settings

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.SettingsState
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Upsert
    suspend fun save(settingsState: SettingsState)

    @Query("SELECT * FROM settings")
    fun getSettings(): Flow<List<SettingsState>>
}