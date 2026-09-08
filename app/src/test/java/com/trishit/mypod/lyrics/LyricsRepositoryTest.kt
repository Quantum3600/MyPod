package com.trishit.mypod.lyrics

import com.trishit.mypod.data.lyrics.LyricEntity
import com.trishit.mypod.data.lyrics.LyricsDao
import com.trishit.mypod.data.lyrics.LyricsRepository
import com.trishit.mypod.data.lyrics.LrclibApi
import com.trishit.mypod.data.lyrics.LrclibResponse
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class FakeLyricsDao : LyricsDao {
    val db = mutableMapOf<String, LyricEntity>()

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
        if (trackName.contains("Bohemian Rhapsody", ignoreCase = true)) {
            return Response.success(
                LrclibResponse(
                    id = 2L,
                    trackName = "Bohemian Rhapsody",
                    artistName = "Queen",
                    syncedLyrics = "[00:00.00]Is this the real life?\n[00:04.50]Is this just fantasy?"
                )
            )
        }
        if (trackName.contains("Hello", ignoreCase = true)) {
            return Response.success(
                LrclibResponse(
                    id = 3L,
                    trackName = "Hello",
                    artistName = "Adele",
                    syncedLyrics = "[00:01.05]Hello, it's me"
                )
            )
        }
        return Response.error(404, "Not Found".toResponseBody(null))
    }

    override suspend fun searchLyrics(query: String): Response<List<LrclibResponse>> {
        if (query.contains("Yellow", ignoreCase = true)) {
            return Response.success(
                listOf(
                    LrclibResponse(
                        id = 4L,
                        trackName = "Yellow",
                        artistName = "Coldplay",
                        syncedLyrics = "[00:27.50]Look at the stars"
                    )
                )
            )
        }
        return Response.success(emptyList())
    }
}

class LyricsRepositoryTest {

    @Test
    fun `fetchLyrics returns LRCLIB response for Get Lucky`() = runTest {
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
        assertEquals("LRCLIB API", result.sourceName)
    }

    @Test
    fun `fetchLyrics returns LRCLIB response for Bohemian Rhapsody`() = runTest {
        val fakeDao = FakeLyricsDao()
        val fakeApi = FakeLrclibApi()
        val repo = LyricsRepository(
            api = fakeApi,
            dao = fakeDao
        )

        val result = repo.fetchLyrics("01 - Bohemian Rhapsody", "Queen", "", 354000L)

        assertTrue(result.isFound)
        assertEquals(2, result.syncedLines.size)
        assertEquals("Is this the real life?", result.syncedLines[0].text)
        assertEquals("Is this just fantasy?", result.syncedLines[1].text)
    }

    @Test
    fun `fetchLyrics returns LRCLIB response via search API for Yellow`() = runTest {
        val fakeDao = FakeLyricsDao()
        val fakeApi = FakeLrclibApi()
        val repo = LyricsRepository(
            api = fakeApi,
            dao = fakeDao
        )

        val result = repo.fetchLyrics("Yellow", "Coldplay", "", 269000L)

        assertTrue(result.isFound)
        assertEquals(1, result.syncedLines.size)
        assertEquals("Look at the stars", result.syncedLines[0].text)
    }

    @Test
    fun `fetchLyrics returns Example Synced Template when online lyrics not found and does NOT pollute Room cache`() = runTest {
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

        // Verify Room DB was NOT populated with the example template
        val cachedEntity = fakeDao.getLyric("unknown_artist_unknown_track_01")
        assertNull(cachedEntity)
    }
}
