package com.trishit.mypod.source.spotify

import com.trishit.mypod.playback.AudioEngine
import com.trishit.mypod.source.AlbumInfo
import com.trishit.mypod.source.AuthState
import com.trishit.mypod.source.PlaybackSource
import com.trishit.mypod.source.PlaybackSourceType
import com.trishit.mypod.source.PlaylistInfo
import com.trishit.mypod.source.TrackMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpotifySource(
    private val authProvider: SpotifyAuthProvider,
    private val audioEngine: AudioEngine
) : PlaybackSource {

    override val sourceType: PlaybackSourceType = PlaybackSourceType.SPOTIFY

    override val isAvailable: Boolean
        get() = authProvider.authState.value is AuthState.LoggedIn

    private val _currentTrack = MutableStateFlow<TrackMetadata?>(null)
    override val currentTrackFlow: StateFlow<TrackMetadata?> = _currentTrack.asStateFlow()

    private val sampleTracks = listOf(
        TrackMetadata("sp_1", "Blinding Lights", "The Weeknd", "After Hours", sourceType = PlaybackSourceType.SPOTIFY, durationMs = 200000L),
        TrackMetadata("sp_2", "Starboy", "The Weeknd", "Starboy", sourceType = PlaybackSourceType.SPOTIFY, durationMs = 230000L),
        TrackMetadata("sp_3", "As It Was", "Harry Styles", "Harry's House", sourceType = PlaybackSourceType.SPOTIFY, durationMs = 167000L),
        TrackMetadata("sp_4", "Levitating", "Dua Lipa", "Future Nostalgia", sourceType = PlaybackSourceType.SPOTIFY, durationMs = 203000L)
    )

    override suspend fun getTracks(): List<TrackMetadata> {
        return if (isAvailable) sampleTracks else emptyList()
    }

    override suspend fun getArtists(): List<String> {
        return if (isAvailable) sampleTracks.map { it.artist }.distinct() else emptyList()
    }

    override suspend fun getAlbums(): List<AlbumInfo> {
        return if (isAvailable) {
            listOf(
                AlbumInfo("sp_alb_1", "After Hours", "The Weeknd", trackCount = 14),
                AlbumInfo("sp_alb_2", "Future Nostalgia", "Dua Lipa", trackCount = 11)
            )
        } else emptyList()
    }

    override suspend fun getPlaylists(): List<PlaylistInfo> {
        return if (isAvailable) {
            listOf(
                PlaylistInfo("sp_pl_1", "Discover Weekly", trackCount = 30),
                PlaylistInfo("sp_pl_2", "Release Radar", trackCount = 25)
            )
        } else emptyList()
    }

    override suspend fun getGenres(): List<String> {
        return if (isAvailable) listOf("Pop", "R&B", "Synthpop", "Dance") else emptyList()
    }

    override suspend fun getTracksForArtist(artist: String): List<TrackMetadata> {
        return getTracks().filter { it.artist.equals(artist, ignoreCase = true) }
    }

    override suspend fun getTracksForAlbum(albumId: String): List<TrackMetadata> {
        return getTracks().filter { it.album.equals(albumId, ignoreCase = true) || it.id == albumId }
    }

    override suspend fun getTracksForPlaylist(playlistId: String): List<TrackMetadata> {
        return getTracks()
    }

    override suspend fun getTracksForGenre(genre: String): List<TrackMetadata> {
        return getTracks()
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
