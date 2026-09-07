package com.trishit.mypod.data.lyrics

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LyricsDao {

    @Query("SELECT * FROM cached_lyrics WHERE id = :id LIMIT 1")
    suspend fun getLyric(id: String): LyricEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyric(lyric: LyricEntity)

    @Query("DELETE FROM cached_lyrics WHERE cachedAt < :thresholdTimestamp")
    suspend fun deleteOldLyrics(thresholdTimestamp: Long)
}
