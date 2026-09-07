package com.bytekoders.mypod.data.lyrics

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LyricsRepository(
    context: Context,
    private val api: LrclibApi = LrclibApi.create(),
    private val dao: LyricsDao = LyricsDatabase.getInstance(context).lyricsDao()
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

        val cacheKey = createKey(artist, title)

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
            val response = api.getLyrics(
                trackName = title,
                artistName = artist,
                albumName = if (album.isNotBlank()) album else null,
                durationSeconds = durationSecs
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                syncedLrc = body.syncedLyrics
                plainLrc = body.plainLyrics
                isFound = !syncedLrc.isNullOrBlank() || !plainLrc.isNullOrBlank()
            } else {
                // Fallback search
                val searchResponse = api.searchLyrics("$artist $title")
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

        // 3. Cache to Room DB only if lyrics were found
        if (isFound) {
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
            isFound = isFound,
            sourceName = "LRCLIB API"
        )
    }

    private fun createKey(artist: String, title: String): String {
        return "${artist.trim().lowercase()}_${title.trim().lowercase()}"
            .replace(Regex("[^a-z0-9_]"), "_")
    }
}
