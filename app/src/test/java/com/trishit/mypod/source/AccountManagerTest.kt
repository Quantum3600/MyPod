package com.trishit.mypod.source

import com.trishit.mypod.source.applemusic.AppleMusicAuthProvider
import com.trishit.mypod.source.applemusic.AppleMusicSource
import com.trishit.mypod.source.spotify.SpotifyAuthProvider
import com.trishit.mypod.source.youtube.YouTubeAuthProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountManagerTest {

    @Test
    fun `auth providers return correct service type and initial state`() = runTest {
        val spotifyAuth = SpotifyAuthProvider()
        val youtubeAuth = YouTubeAuthProvider()
        val appleAuth = AppleMusicAuthProvider()

        assertEquals(PlaybackSourceType.SPOTIFY, spotifyAuth.serviceType)
        assertEquals(PlaybackSourceType.YOUTUBE, youtubeAuth.serviceType)
        assertEquals(PlaybackSourceType.APPLE_MUSIC, appleAuth.serviceType)

        assertTrue(spotifyAuth.authState.value is AuthState.LoggedOut)
        assertTrue(youtubeAuth.authState.value is AuthState.LoggedOut)
        assertTrue(appleAuth.authState.value is AuthState.ComingSoon)
    }

    @Test
    fun `apple music source is stubbed and unavailable`() = runTest {
        val appleSource = AppleMusicSource()
        assertEquals(PlaybackSourceType.APPLE_MUSIC, appleSource.sourceType)
        assertEquals(false, appleSource.isAvailable)
        assertTrue(appleSource.getTracks().isEmpty())
        assertTrue(appleSource.getArtists().isEmpty())
        assertTrue(appleSource.getAlbums().isEmpty())
        assertTrue(appleSource.getPlaylists().isEmpty())
        assertTrue(appleSource.getGenres().isEmpty())
    }
}
