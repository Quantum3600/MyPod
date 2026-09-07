package com.bytekoders.mypod.data.lyrics

data class LrcLine(
    val timestampMs: Long,
    val text: String
)

data class LyricsResult(
    val syncedLines: List<LrcLine> = emptyList(),
    val plainLyrics: String? = null,
    val isFound: Boolean = false,
    val sourceName: String = "LRCLIB"
)
