package com.nullinnix.clippr.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.nullinnix.clippr.misc.Clip
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipsDao {
    @Upsert
    suspend fun upsert(clip: Clip)

    @Delete
    suspend fun delete(clip: Clip)

    @Query("SELECT * FROM clips")
    fun getClips(): Flow<List<Clip>>
}