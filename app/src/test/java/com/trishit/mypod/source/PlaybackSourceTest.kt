package com.trishit.mypod.source

import com.trishit.mypod.source.applemusic.AppleMusicAuthProvider
import com.trishit.mypod.source.applemusic.AppleMusicSource
import com.trishit.mypod.source.spotify.SpotifyAuthProvider
import com.trishit.mypod.source.youtube.YouTubeAuthProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSourceTest {

    @Test
    fun `apple music source is stubbed as coming soon`() = runTest {
        val appleAuth = AppleMusicAuthProvider()
        val appleSource = AppleMusicSource()

        val authState = appleAuth.authState.value
        assertTrue(authState is AuthState.ComingSoon)
        assertEquals("Apple Music", (authState as AuthState.ComingSoon).serviceName)

        assertFalse(appleSource.isAvailable)
        assertTrue(appleSource.getTracks().isEmpty())
    }

    @Test
    fun `spotify source availability depends on auth state`() = runTest {
        val spotifyAuth = SpotifyAuthProvider()
        assertTrue(spotifyAuth.authState.value is AuthState.LoggedOut)

        spotifyAuth.signIn()
        val signedInState = spotifyAuth.authState.value
        assertTrue(signedInState is AuthState.LoggedIn)
        assertEquals("Spotify Premium User", (signedInState as AuthState.LoggedIn).displayName)

        spotifyAuth.signOut()
        assertTrue(spotifyAuth.authState.value is AuthState.LoggedOut)
    }

    @Test
    fun `youtube source auth state sign in and sign out`() = runTest {
        val youtubeAuth = YouTubeAuthProvider()
        assertTrue(youtubeAuth.authState.value is AuthState.LoggedOut)

        youtubeAuth.signIn()
        val signedInState = youtubeAuth.authState.value
        assertTrue(signedInState is AuthState.LoggedIn)
        assertEquals("YouTube Music Subscriber", (signedInState as AuthState.LoggedIn).displayName)
    }

    @Test
    fun `playback source types have display names and badges`() {
        assertEquals("Local Library", PlaybackSourceType.LOCAL.displayName)
        assertEquals("LOCAL", PlaybackSourceType.LOCAL.badge)
        assertEquals("Spotify", PlaybackSourceType.SPOTIFY.displayName)
        assertEquals("SPOTIFY", PlaybackSourceType.SPOTIFY.badge)
    }
}
