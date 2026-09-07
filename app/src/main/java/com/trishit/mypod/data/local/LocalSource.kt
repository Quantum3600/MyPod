package com.trishit.mypod.data.local

import android.content.Context
import com.trishit.mypod.playback.AudioEngine
import com.trishit.mypod.source.AlbumInfo
import com.trishit.mypod.source.PlaybackSource
import com.trishit.mypod.source.PlaybackSourceType
import com.trishit.mypod.source.PlaylistInfo
import com.trishit.mypod.source.TrackMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class LocalSource(
    context: Context,
    private val audioEngine: AudioEngine
) : PlaybackSource {

    private val scanner = MediaStoreScanner(context)

    override val sourceType: PlaybackSourceType = PlaybackSourceType.LOCAL
    override val isAvailable: Boolean = true

    private val _currentTrack = MutableStateFlow<TrackMetadata?>(null)
    override val currentTrackFlow: StateFlow<TrackMetadata?> = _currentTrack.asStateFlow()

    override suspend fun getTracks(): List<TrackMetadata> = withContext(Dispatchers.IO) {
        scanner.scanAllTracks()
    }

    override suspend fun getArtists(): List<String> = withContext(Dispatchers.IO) {
        scanner.scanArtists()
    }

    override suspend fun getAlbums(): List<AlbumInfo> = withContext(Dispatchers.IO) {
        scanner.scanAlbums()
    }

    override suspend fun getPlaylists(): List<PlaylistInfo> = withContext(Dispatchers.IO) {
        scanner.scanPlaylists()
    }

    override suspend fun getGenres(): List<String> = withContext(Dispatchers.IO) {
        scanner.scanGenres()
    }

    override suspend fun getTracksForArtist(artist: String): List<TrackMetadata> = withContext(Dispatchers.IO) {
        getTracks().filter { it.artist.equals(artist, ignoreCase = true) }
    }

    override suspend fun getTracksForAlbum(albumId: String): List<TrackMetadata> = withContext(Dispatchers.IO) {
        val all = getTracks()
        val direct = all.filter {
            it.album.equals(albumId, ignoreCase = true) || it.id == albumId
        }
        if (direct.isNotEmpty()) return@withContext direct

        val albumObj = getAlbums().find { it.id == albumId || it.name.equals(albumId, ignoreCase = true) }
        if (albumObj != null) {
            all.filter { it.album.equals(albumObj.name, ignoreCase = true) }
        } else {
            all
        }
    }

    override suspend fun getTracksForPlaylist(playlistId: String): List<TrackMetadata> = withContext(Dispatchers.IO) {
        scanner.scanTracksForPlaylist(playlistId)
    }

    override suspend fun getTracksForGenre(genre: String): List<TrackMetadata> = withContext(Dispatchers.IO) {
        getTracks().filter { it.genre.equals(genre, ignoreCase = true) }
    }

    override suspend fun playTrack(track: TrackMetadata) {
        _currentTrack.value = track
        audioEngine.playTrack(track)
    }

    override suspend fun playQueue(tracks: List<TrackMetadata>, startIndex: Int) {
        if (tracks.isNotEmpty()) {
            val idx = startIndex.coerceIn(tracks.indices)
            _currentTrack.value = tracks[idx]
        }
        audioEngine.playQueue(tracks, startIndex)
    }
}
