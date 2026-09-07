package com.bytekoders.mypod.source.youtube

import com.bytekoders.mypod.data.local.LocalSource
import com.bytekoders.mypod.playback.AudioEngine
import com.bytekoders.mypod.source.AlbumInfo
import com.bytekoders.mypod.source.PlaybackSource
import com.bytekoders.mypod.source.PlaybackSourceType
import com.bytekoders.mypod.source.PlaylistInfo
import com.bytekoders.mypod.source.TrackMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class YtDlpSource(
    private val audioEngine: AudioEngine,
    private val localSource: LocalSource? = null
) : PlaybackSource {

    override val sourceType: PlaybackSourceType = PlaybackSourceType.YTDLP
    override val isAvailable: Boolean = true

    private val _currentTrack = MutableStateFlow<TrackMetadata?>(null)
    override val currentTrackFlow: StateFlow<TrackMetadata?> = _currentTrack.asStateFlow()

    private val sampleStreams = listOf(
        TrackMetadata("ytdlp_1", "Direct Audio Stream #1", "yt-dlp Extractor", "Live Stream", sourceType = PlaybackSourceType.YTDLP, durationMs = 180000L),
        TrackMetadata("ytdlp_2", "Direct Audio Stream #2", "yt-dlp Extractor", "Live Stream", sourceType = PlaybackSourceType.YTDLP, durationMs = 240000L)
    )

    override suspend fun getTracks(): List<TrackMetadata> {
        val userTracks = localSource?.getTracks() ?: emptyList()
        return if (userTracks.isNotEmpty()) {
            userTracks.map { it.copy(sourceType = PlaybackSourceType.YTDLP) }
        } else {
            sampleStreams
        }
    }

    override suspend fun getArtists(): List<String> = localSource?.getArtists()?.ifEmpty { listOf("yt-dlp Extractor") } ?: listOf("yt-dlp Extractor")
    override suspend fun getAlbums(): List<AlbumInfo> = localSource?.getAlbums()?.ifEmpty { listOf(AlbumInfo("ytdlp_alb_1", "Live Stream", "yt-dlp", trackCount = 2)) } ?: listOf(AlbumInfo("ytdlp_alb_1", "Live Stream", "yt-dlp", trackCount = 2))
    override suspend fun getPlaylists(): List<PlaylistInfo> = localSource?.getPlaylists() ?: emptyList()
    override suspend fun getGenres(): List<String> = localSource?.getGenres() ?: emptyList()

    override suspend fun getTracksForArtist(artist: String): List<TrackMetadata> {
        return localSource?.getTracksForArtist(artist)?.map { it.copy(sourceType = PlaybackSourceType.YTDLP) } ?: getTracks()
    }

    override suspend fun getTracksForAlbum(albumId: String): List<TrackMetadata> {
        return localSource?.getTracksForAlbum(albumId)?.map { it.copy(sourceType = PlaybackSourceType.YTDLP) } ?: getTracks()
    }

    override suspend fun getTracksForPlaylist(playlistId: String): List<TrackMetadata> {
        return localSource?.getTracksForPlaylist(playlistId)?.map { it.copy(sourceType = PlaybackSourceType.YTDLP) } ?: getTracks()
    }

    override suspend fun getTracksForGenre(genre: String): List<TrackMetadata> {
        return localSource?.getTracksForGenre(genre)?.map { it.copy(sourceType = PlaybackSourceType.YTDLP) } ?: getTracks()
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
