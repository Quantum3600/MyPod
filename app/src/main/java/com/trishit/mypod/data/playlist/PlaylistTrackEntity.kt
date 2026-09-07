package com.trishit.mypod.data.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_tracks")
data class PlaylistTrackEntity(
    @PrimaryKey(autoGenerate = true) val autoId: Long = 0,
    val playlistId: String,
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val mediaUri: String,
    val durationMs: Long,
    val sourceType: String = "LOCAL",
    val addedAt: Long = System.currentTimeMillis()
)
