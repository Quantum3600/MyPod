package com.bytekoders.mypod.source.applemusic

import com.bytekoders.mypod.source.AlbumInfo
import com.bytekoders.mypod.source.PlaybackSource
import com.bytekoders.mypod.source.PlaybackSourceType
import com.bytekoders.mypod.source.PlaylistInfo
import com.bytekoders.mypod.source.TrackMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppleMusicSource : PlaybackSource {

    override val sourceType: PlaybackSourceType = PlaybackSourceType.APPLE_MUSIC
    override val isAvailable: Boolean = false // Stubbed until MusicKit credentials exist

    private val _currentTrack = MutableStateFlow<TrackMetadata?>(null)
    override val currentTrackFlow: StateFlow<TrackMetadata?> = _currentTrack.asStateFlow()

    override suspend fun getTracks(): List<TrackMetadata> = emptyList()
    override suspend fun getArtists(): List<String> = emptyList()
    override suspend fun getAlbums(): List<AlbumInfo> = emptyList()
    override suspend fun getPlaylists(): List<PlaylistInfo> = emptyList()
    override suspend fun getGenres(): List<String> = emptyList()

    override suspend fun getTracksForArtist(artist: String): List<TrackMetadata> = emptyList()
    override suspend fun getTracksForAlbum(albumId: String): List<TrackMetadata> = emptyList()
    override suspend fun getTracksForPlaylist(playlistId: String): List<TrackMetadata> = emptyList()
    override suspend fun getTracksForGenre(genre: String): List<TrackMetadata> = emptyList()

    override suspend fun playTrack(track: TrackMetadata) {
        // Stub
    }

    override suspend fun playQueue(tracks: List<TrackMetadata>, startIndex: Int) {
        // Stub
    }
}
