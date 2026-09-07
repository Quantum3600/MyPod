package com.bytekoders.mypod.source.youtube

import com.bytekoders.mypod.playback.AudioEngine
import com.bytekoders.mypod.source.AlbumInfo
import com.bytekoders.mypod.source.AuthState
import com.bytekoders.mypod.source.PlaybackSource
import com.bytekoders.mypod.source.PlaybackSourceType
import com.bytekoders.mypod.source.PlaylistInfo
import com.bytekoders.mypod.source.TrackMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class YouTubeSource(
    private val authProvider: YouTubeAuthProvider,
    private val audioEngine: AudioEngine
) : PlaybackSource {

    override val sourceType: PlaybackSourceType = PlaybackSourceType.YOUTUBE

    override val isAvailable: Boolean
        get() = authProvider.authState.value is AuthState.LoggedIn

    private val _currentTrack = MutableStateFlow<TrackMetadata?>(null)
    override val currentTrackFlow: StateFlow<TrackMetadata?> = _currentTrack.asStateFlow()

    private val sampleTracks = listOf(
        TrackMetadata("yt_1", "Synthwave Chill Mix", "Lofi Girl", "YouTube Radio", sourceType = PlaybackSourceType.YOUTUBE, durationMs = 300000L),
        TrackMetadata("yt_2", "Midnight City", "M83", "Hurry Up, We're Dreaming", sourceType = PlaybackSourceType.YOUTUBE, durationMs = 243000L),
        TrackMetadata("yt_3", "Resonance", "HOME", "Odyssey", sourceType = PlaybackSourceType.YOUTUBE, durationMs = 212000L)
    )

    override suspend fun getTracks(): List<TrackMetadata> {
        return if (isAvailable) sampleTracks else emptyList()
    }

    override suspend fun getArtists(): List<String> {
        return if (isAvailable) sampleTracks.map { it.artist }.distinct() else emptyList()
    }

    override suspend fun getAlbums(): List<AlbumInfo> {
        return if (isAvailable) listOf(AlbumInfo("yt_alb_1", "YouTube Radio", "Various", trackCount = 20)) else emptyList()
    }

    override suspend fun getPlaylists(): List<PlaylistInfo> {
        return if (isAvailable) listOf(PlaylistInfo("yt_pl_1", "Trending Music", trackCount = 50)) else emptyList()
    }

    override suspend fun getGenres(): List<String> {
        return if (isAvailable) listOf("Electronic", "Synthwave", "Chillout") else emptyList()
    }

    override suspend fun getTracksForArtist(artist: String): List<TrackMetadata> = getTracks()
    override suspend fun getTracksForAlbum(albumId: String): List<TrackMetadata> = getTracks()
    override suspend fun getTracksForPlaylist(playlistId: String): List<TrackMetadata> = getTracks()
    override suspend fun getTracksForGenre(genre: String): List<TrackMetadata> = getTracks()

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
