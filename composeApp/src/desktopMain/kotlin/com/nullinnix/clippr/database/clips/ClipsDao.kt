package com.nullinnix.clippr.database.clips

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.nullinnix.clippr.misc.ClipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipsDao {
    @Upsert
    suspend fun upsert(clip: ClipEntity)

    @Delete
    suspend fun delete(clip: ClipEntity)

    @Query("SELECT * FROM clips WHERE not isPinned ORDER BY copiedAt DESC")
    fun getOtherClips(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips WHERE isPinned ORDER BY pinnedAt DESC")
    fun getPinnedClips(): Flow<List<ClipEntity>>

    @Query("DELETE FROM clips WHERE NOT isPinned")
    suspend fun deleteAllUnpinned()

    @Query("DELETE FROM clips WHERE :currentEpoch - copiedAt > 2592000 AND NOT isPinned")
    suspend fun deleteUnpinnedOlderThan30(currentEpoch: Long)

    @Query("DELETE FROM clips WHERE :currentEpoch - copiedAt > :deleteThreshold AND NOT isPinned")
    suspend fun deleteOldUnpinnedClips(currentEpoch: Long, deleteThreshold: Long)

    @Query("DELETE FROM clips WHERE clipID IN (:clipsToDelete)")
    suspend fun deleteSelected(clipsToDelete: List<String>)

    @Query("""
        UPDATE clips
        SET isPinned = :state,
            pinnedAt = :pinnedAt
        WHERE clipID IN (:clips)
    """)
    suspend fun setMultiplePinnedState(clips: List<String>, state: Boolean, pinnedAt: Long)
}