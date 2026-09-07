package com.bytekoders.mypod.source

data class TrackMetadata(
    val id: String,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val artUri: String? = null,
    val mediaUri: String? = null,
    val durationMs: Long = 0L,
    val trackNumber: Int = 0,
    val genre: String? = null,
    val sourceType: PlaybackSourceType = PlaybackSourceType.LOCAL
)

data class AlbumInfo(
    val id: String,
    val name: String,
    val artist: String,
    val artUri: String? = null,
    val trackCount: Int = 0
)

data class PlaylistInfo(
    val id: String,
    val name: String,
    val trackCount: Int = 0,
    val artUri: String? = null
)

data class NowPlayingState(
    val currentTrack: TrackMetadata? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val playbackSourceType: PlaybackSourceType = PlaybackSourceType.LOCAL,
    val queue: List<TrackMetadata> = emptyList(),
    val queueIndex: Int = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = 0 // 0: Off, 1: One, 2: All
)
