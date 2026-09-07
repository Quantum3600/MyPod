package com.trishit.mypod.source

enum class PlaybackSourceType(val displayName: String, val badge: String) {
    LOCAL("Local Library", "LOCAL"),
    FILES("SAF Files", "FILES"),
    SPOTIFY("Spotify", "SPOTIFY"),
    YOUTUBE("YouTube Music", "YOUTUBE"),
    YTDLP("yt-dlp Stream", "YTDLP"),
    APPLE_MUSIC("Apple Music", "APPLE")
}
