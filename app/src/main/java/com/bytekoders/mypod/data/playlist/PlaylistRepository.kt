package com.bytekoders.mypod.data.playlist

import android.content.Context
import com.bytekoders.mypod.source.PlaybackSourceType
import com.bytekoders.mypod.source.TrackMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlaylistRepository(context: Context) {

    private val db = PlaylistDatabase.getInstance(context)
    private val dao = db.playlistDao()

    val playlistsFlow: Flow<List<PlaylistEntity>> = dao.getAllPlaylistsFlow()

    suspend fun initDefaultPlaylists() = withContext(Dispatchers.IO) {
        val existing = dao.getAllPlaylists()
        if (existing.none { it.id == "favorites" }) {
            dao.insertPlaylist(PlaylistEntity(id = "favorites", name = "❤️ Favorites", isSystem = true))
        }
        if (existing.none { it.id == "recently_played" }) {
            dao.insertPlaylist(PlaylistEntity(id = "recently_played", name = "🕒 Recently Played", isSystem = true))
        }
    }

    suspend fun createPlaylist(name: String): String = withContext(Dispatchers.IO) {
        val id = "pl_${System.currentTimeMillis()}"
        dao.insertPlaylist(PlaylistEntity(id = id, name = name, isSystem = false))
        id
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, track: TrackMetadata) = withContext(Dispatchers.IO) {
        dao.insertPlaylistTrack(
            PlaylistTrackEntity(
                playlistId = playlistId,
                trackId = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                mediaUri = track.mediaUri ?: "",
                durationMs = track.durationMs,
                sourceType = track.sourceType.name
            )
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        dao.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun getTracksForPlaylist(playlistId: String): List<TrackMetadata> = withContext(Dispatchers.IO) {
        dao.getTracksForPlaylist(playlistId).map { entity ->
            TrackMetadata(
                id = entity.trackId,
                title = entity.title,
                artist = entity.artist,
                album = entity.album,
                mediaUri = entity.mediaUri.ifEmpty { null },
                durationMs = entity.durationMs,
                sourceType = try { PlaybackSourceType.valueOf(entity.sourceType) } catch (_: Exception) { PlaybackSourceType.LOCAL }
            )
        }
    }

    fun isFavoriteFlow(trackId: String): Flow<Boolean> = dao.isFavoriteFlow(trackId)

    suspend fun toggleFavorite(track: TrackMetadata): Boolean = withContext(Dispatchers.IO) {
        val currentlyFav = dao.isFavorite(track.id)
        if (currentlyFav) {
            dao.removeTrackFromPlaylist("favorites", track.id)
            false
        } else {
            addTrackToPlaylist("favorites", track)
            true
        }
    }
}
