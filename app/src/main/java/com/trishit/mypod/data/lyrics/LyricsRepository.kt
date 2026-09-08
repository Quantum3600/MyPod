package com.trishit.mypod.data.lyrics

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class LyricsRepository(
    context: Context? = null,
    private val api: LrclibApi = LrclibApi.create(),
    private val dao: LyricsDao = if (context != null) LyricsDatabase.getInstance(context).lyricsDao() else object : LyricsDao {
        override suspend fun getLyric(id: String): LyricEntity? = null
        override suspend fun insertLyric(lyric: LyricEntity) {}
        override suspend fun deleteOldLyrics(thresholdTimestamp: Long) {}
    },
) {

    suspend fun fetchLyrics(
        title: String,
        artist: String,
        album: String = "",
        durationMs: Long = 0L
    ): LyricsResult = withContext(Dispatchers.IO) {
        if (title.isBlank() && artist.isBlank()) {
            return@withContext LyricsResult(isFound = false)
        }

        val cleanedTitle = cleanTitle(title)
        val cleanedArtist = cleanArtist(artist)
        val cacheKey = createKey(cleanedArtist.ifBlank { artist }, cleanedTitle.ifBlank { title })

        // 1. Check Room Cache
        try {
            val cached = dao.getLyric(cacheKey)
            if (cached != null && (!cached.syncedLyrics.isNullOrBlank() || !cached.plainLyrics.isNullOrBlank())) {
                val parsedLines = LrcParser.parse(cached.syncedLyrics)
                return@withContext LyricsResult(
                    syncedLines = parsedLines,
                    plainLyrics = cached.plainLyrics,
                    isFound = true,
                    sourceName = "Cached Room DB"
                )
            }
        } catch (_: Exception) {}

        // 2. Fetch from LRCLIB API
        var syncedLrc: String? = null
        var plainLrc: String? = null
        var isFound = false

        try {
            val durationSecs = if (durationMs > 0) (durationMs / 1000).toInt() else null

            // First attempt: clean track and artist name with duration
            var response = api.getLyrics(
                trackName = cleanedTitle.ifBlank { title },
                artistName = cleanedArtist.ifBlank { artist },
                albumName = null,
                durationSeconds = durationSecs
            )

            // Second attempt: without duration constraint
            if (!response.isSuccessful || (response.body()?.syncedLyrics.isNullOrBlank() && response.body()?.plainLyrics.isNullOrBlank())) {
                response = api.getLyrics(
                    trackName = cleanedTitle.ifBlank { title },
                    artistName = cleanedArtist.ifBlank { artist },
                    albumName = null,
                    durationSeconds = null
                )
            }

            // Third attempt: using uncleaned title and artist if they were different
            if ((!response.isSuccessful || (response.body()?.syncedLyrics.isNullOrBlank() && response.body()?.plainLyrics.isNullOrBlank())) &&
                (cleanedTitle != title || cleanedArtist != artist)) {
                response = api.getLyrics(
                    trackName = title,
                    artistName = artist,
                    albumName = null,
                    durationSeconds = null
                )
            }

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                syncedLrc = body.syncedLyrics
                plainLrc = body.plainLyrics
                isFound = !syncedLrc.isNullOrBlank() || !plainLrc.isNullOrBlank()
            }

            // Fallback: LRCLIB search API
            if (!isFound) {
                val query = if (cleanedArtist.isNotBlank()) "$cleanedArtist $cleanedTitle" else cleanedTitle
                val searchResponse = api.searchLyrics(query)
                if (searchResponse.isSuccessful && !searchResponse.body().isNullOrEmpty()) {
                    val match = searchResponse.body()!!.firstOrNull {
                        !it.syncedLyrics.isNullOrBlank() || !it.plainLyrics.isNullOrBlank()
                    } ?: searchResponse.body()!!.first()
                    syncedLrc = match.syncedLyrics
                    plainLrc = match.plainLyrics
                    isFound = !syncedLrc.isNullOrBlank() || !plainLrc.isNullOrBlank()
                }
            }
        } catch (_: Exception) {}

        // 3. Fallback: Generate Time-Synced Example Lyrics if no online lyrics match
        var isExample = false
        if (!isFound) {
            syncedLrc = generateExampleLyrics(cleanedTitle.ifBlank { title }, cleanedArtist.ifBlank { artist }, durationMs)
            isExample = true
        }

        // 4. Cache to Room DB ONLY IF real online lyrics were found (do not cache generated example templates)
        if (!isExample && (!syncedLrc.isNullOrBlank() || !plainLrc.isNullOrBlank())) {
            val entity = LyricEntity(
                id = cacheKey,
                trackTitle = title,
                artistName = artist,
                albumName = album,
                syncedLyrics = syncedLrc,
                plainLyrics = plainLrc,
                cachedAt = System.currentTimeMillis()
            )
            try {
                dao.insertLyric(entity)
            } catch (_: Exception) {}
        }

        val parsedLines = LrcParser.parse(syncedLrc)
        LyricsResult(
            syncedLines = parsedLines,
            plainLyrics = plainLrc,
            isFound = true,
            sourceName = if (isExample) "Example Synced Template" else "LRCLIB API"
        )
    }

    private fun cleanTitle(rawTitle: String): String {
        return rawTitle
            .replace(Regex("(?i)\\.(mp3|flac|wav|m4a|aac|ogg|wma)$"), "")
            .replace(Regex("(?i)\\s*[\\[(](official|video|audio|lyric|remastered|feat|ft).*"), "")
            .replace(Regex("^\\d+[.\\s-]+"), "")
            .trim()
    }

    private fun cleanArtist(rawArtist: String): String {
        return rawArtist
            .replace(Regex("(?i)\\s*feat\\..*|(?i)\\s*ft\\..*"), "")
            .replace(Regex("(?i)<unknown>|unknown artist"), "")
            .trim()
    }

    private fun generateExampleLyrics(title: String, artist: String, durationMs: Long): String {
        val totalSec = if (durationMs > 0) (durationMs / 1000).toInt() else 180
        val displayTitle = title.ifBlank { "Track" }
        val displayArtist = artist.ifBlank { "MyPod Library" }

        val step = (totalSec / 8).coerceAtLeast(4)

        fun formatTimestamp(sec: Int): String {
            val m = sec / 60
            val s = sec % 60
            return String.format(Locale.US, "[%02d:%02d.00]", m, s)
        }

        return """
            ${formatTimestamp(0)} ♪ Playing: $displayTitle ♪
            ${formatTimestamp(step)} Artist: $displayArtist
            ${formatTimestamp(step * 2)} Welcome to MyPod Classic Music Player
            ${formatTimestamp(step * 3)} Synced lyrics engine active
            ${formatTimestamp(step * 4)} Enjoy pixel-accurate iPod Classic controls
            ${formatTimestamp(step * 5)} Spin the click wheel to scroll and adjust volume
            ${formatTimestamp(step * 6)} Double-tap Center button on Now Playing to toggle 3D Artwork
            ${formatTimestamp(step * 7)} ♪ MyPod - Pixel-Accurate iPod Experience ♪
        """.trimIndent()
    }

    private fun createKey(artist: String, title: String): String {
        return "${artist.trim().lowercase()}_${title.trim().lowercase()}"
            .replace(Regex("[^a-z0-9_]"), "_")
    }
}
