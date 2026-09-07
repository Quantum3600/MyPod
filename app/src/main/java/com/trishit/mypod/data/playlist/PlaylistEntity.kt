package com.trishit.mypod.data.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
