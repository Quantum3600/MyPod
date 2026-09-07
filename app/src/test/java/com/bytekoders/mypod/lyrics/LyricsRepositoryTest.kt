package com.bytekoders.mypod.lyrics

import com.bytekoders.mypod.data.lyrics.LyricEntity
import com.bytekoders.mypod.data.lyrics.LyricsDao
import com.bytekoders.mypod.data.lyrics.LyricsRepository
import com.bytekoders.mypod.data.lyrics.LrclibApi
import com.bytekoders.mypod.data.lyrics.LrclibResponse
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class FakeLyricsDao : LyricsDao {
    private val db = mutableMapOf<String, LyricEntity>()

    override suspend fun getLyric(id: String): LyricEntity? = db[id]

    override suspend fun insertLyric(lyric: LyricEntity) {
        db[lyric.id] = lyric
    }

    override suspend fun deleteOldLyrics(thresholdTimestamp: Long) {
        db.entries.removeIf { it.value.cachedAt < thresholdTimestamp }
    }
}

class FakeLrclibApi : LrclibApi {
    override suspend fun getLyrics(
        trackName: String,
        artistName: String,
        albumName: String?,
        durationSeconds: Int?
    ): Response<LrclibResponse> {
        if (trackName.contains("Get Lucky", ignoreCase = true)) {
            return Response.success(
                LrclibResponse(
                    id = 1L,
                    trackName = "Get Lucky",
                    artistName = "Daft Punk",
                    syncedLyrics = "[00:05.00]Like the legend of the phoenix"
                )
            )
        }
        return Response.error(404, "Not Found".toResponseBody(null))
    }

    override suspend fun searchLyrics(query: String): Response<List<LrclibResponse>> {
        return Response.success(emptyList())
    }
}

class LyricsRepositoryTest {

    @Test
    fun `fetchLyrics returns LRCLIB response when available`() = runTest {
        val fakeDao = FakeLyricsDao()
        val fakeApi = FakeLrclibApi()
        val repo = LyricsRepository(
            api = fakeApi,
            dao = fakeDao
        )

        val result = repo.fetchLyrics("Get Lucky.mp3", "Daft Punk", "", 240000L)

        assertTrue(result.isFound)
        assertEquals(1, result.syncedLines.size)
        assertEquals("Like the legend of the phoenix", result.syncedLines[0].text)
    }

    @Test
    fun `fetchLyrics returns Example Synced Template when online lyrics not found`() = runTest {
        val fakeDao = FakeLyricsDao()
        val fakeApi = FakeLrclibApi()
        val repo = LyricsRepository(
            api = fakeApi,
            dao = fakeDao
        )

        val result = repo.fetchLyrics("Unknown Track 01.mp3", "Unknown Artist", "", 180000L)

        assertTrue(result.isFound)
        assertTrue(result.syncedLines.isNotEmpty())
        assertEquals("Example Synced Template", result.sourceName)
    }
}
