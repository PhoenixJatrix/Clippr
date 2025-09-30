package com.nullinnix.clippr.database.clips

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

//    @Query("SELECT * FROM clips")
    @Query("SELECT * FROM clips WHERE not isPinned ORDER BY copiedAt DESC LIMIT :offset OFFSET 0")
    fun getOtherClips(offset: Int): Flow<List<Clip>>

    @Query("SELECT * FROM clips WHERE isPinned ORDER BY pinnedAt DESC LIMIT :offset OFFSET 0")
    fun getPinnedClips(offset: Int): Flow<List<Clip>>
}