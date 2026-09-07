package com.trishit.mypod.source.youtube

import com.trishit.mypod.data.local.LocalSource
import com.trishit.mypod.playback.AudioEngine
import com.trishit.mypod.source.AlbumInfo
import com.trishit.mypod.source.PlaybackSource
import com.trishit.mypod.source.PlaybackSourceType
import com.trishit.mypod.source.PlaylistInfo
import com.trishit.mypod.source.TrackMetadata
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

    private val onlineSampleStreams = listOf(
        TrackMetadata(
            id = "ytdlp_stream_1",
            title = "Lofi Study Stream (yt-dlp Direct)",
            artist = "Lofi Producer",
            album = "Lofi Beats & Study",
            mediaUri = "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3?filename=lofi-study-112191.mp3",
            artUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=500&q=80",
            sourceType = PlaybackSourceType.YTDLP,
            durationMs = 180000L
        ),
        TrackMetadata(
            id = "ytdlp_stream_2",
            title = "Synthwave 80s Retro Stream",
            artist = "Cyberpunk Waves",
            album = "Synthwave Classics",
            mediaUri = "https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8a73467.mp3?filename=synthwave-80s-110045.mp3",
            artUri = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&w=500&q=80",
            sourceType = PlaybackSourceType.YTDLP,
            durationMs = 240000L
        ),
        TrackMetadata(
            id = "ytdlp_stream_3",
            title = "Acoustic Guitars Session",
            artist = "Acoustic Solo",
            album = "Acoustic Dreams",
            mediaUri = "https://cdn.pixabay.com/download/audio/2021/08/09/audio_884ca9c00b.mp3?filename=guitars-acoustic-11219.mp3",
            artUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=500&q=80",
            sourceType = PlaybackSourceType.YTDLP,
            durationMs = 210000L
        )
    )

    private val onlineAlbums = listOf(
        AlbumInfo("ytdlp_alb_1", "Lofi Beats & Study", "Lofi Producer", trackCount = 1, artUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=500&q=80"),
        AlbumInfo("ytdlp_alb_2", "Synthwave Classics", "Cyberpunk Waves", trackCount = 1, artUri = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&w=500&q=80"),
        AlbumInfo("ytdlp_alb_3", "Acoustic Dreams", "Acoustic Solo", trackCount = 1, artUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=500&q=80"),
        AlbumInfo("ytdlp_alb_top", "yt-dlp Top Trending Hits", "Online Streams", trackCount = 3, artUri = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=500&q=80")
    )

    override suspend fun getTracks(): List<TrackMetadata> {
        val userTracks = localSource?.getTracks()?.map { it.copy(sourceType = PlaybackSourceType.YTDLP) } ?: emptyList()
        return onlineSampleStreams + userTracks
    }

    override suspend fun getArtists(): List<String> = listOf("Lofi Producer", "Cyberpunk Waves", "Acoustic Solo") + (localSource?.getArtists() ?: emptyList())

    override suspend fun getAlbums(): List<AlbumInfo> = onlineAlbums + (localSource?.getAlbums() ?: emptyList())

    override suspend fun getPlaylists(): List<PlaylistInfo> = localSource?.getPlaylists() ?: emptyList()
    override suspend fun getGenres(): List<String> = listOf("Online Streams", "Lofi", "Synthwave") + (localSource?.getGenres() ?: emptyList())

    override suspend fun getTracksForArtist(artist: String): List<TrackMetadata> {
        return getTracks().filter { it.artist.equals(artist, ignoreCase = true) }.ifEmpty { getTracks() }
    }

    override suspend fun getTracksForAlbum(albumId: String): List<TrackMetadata> {
        if (albumId == "ytdlp_alb_top") return onlineSampleStreams
        val matchedAlbum = onlineAlbums.find { it.id == albumId }
        if (matchedAlbum != null) {
            val matchedTracks = onlineSampleStreams.filter { it.album.equals(matchedAlbum.name, ignoreCase = true) }
            if (matchedTracks.isNotEmpty()) return matchedTracks
        }
        return getTracks().filter { it.album.equals(albumId, ignoreCase = true) || it.id == albumId }.ifEmpty { onlineSampleStreams }
    }

    override suspend fun getTracksForPlaylist(playlistId: String): List<TrackMetadata> {
        return localSource?.getTracksForPlaylist(playlistId)?.map { it.copy(sourceType = PlaybackSourceType.YTDLP) } ?: getTracks()
    }

    override suspend fun getTracksForGenre(genre: String): List<TrackMetadata> {
        return getTracks().filter { it.genre.equals(genre, ignoreCase = true) }.ifEmpty { getTracks() }
    }

    private fun resolveTrackUri(track: TrackMetadata): TrackMetadata {
        if (!track.mediaUri.isNullOrBlank()) {
            return track
        }
        val defaultUri = onlineSampleStreams.first().mediaUri
        return track.copy(mediaUri = defaultUri)
    }

    override suspend fun playTrack(track: TrackMetadata) {
        val resolved = resolveTrackUri(track)
        _currentTrack.value = resolved
        audioEngine.playTrack(resolved)
    }

    override suspend fun playQueue(tracks: List<TrackMetadata>, startIndex: Int) {
        if (tracks.isNotEmpty()) {
            val resolvedQueue = tracks.map { resolveTrackUri(it) }
            val idx = startIndex.coerceIn(resolvedQueue.indices)
            _currentTrack.value = resolvedQueue[idx]
            audioEngine.playQueue(resolvedQueue, idx)
        }
    }
}
