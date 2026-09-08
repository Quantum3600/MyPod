package com.trishit.mypod.data.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ytdlp_listened_tracks")
data class YtDlpTrackEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val mediaUri: String,
    val artUri: String? = null,
    val durationMs: Long,
    val listenedAt: Long = System.currentTimeMillis()
)
