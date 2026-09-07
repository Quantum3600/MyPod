package com.trishit.mypod.data.lyrics

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_lyrics")
data class LyricEntity(
    @PrimaryKey val id: String,
    val trackTitle: String,
    val artistName: String,
    val albumName: String,
    val syncedLyrics: String?,
    val plainLyrics: String?,
    val cachedAt: Long
)
