package com.trishit.mypod.source.spotify

import com.trishit.mypod.source.AuthProvider
import com.trishit.mypod.source.AuthState
import com.trishit.mypod.source.PlaybackSourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpotifyAuthProvider : AuthProvider {

    override val serviceType: PlaybackSourceType = PlaybackSourceType.SPOTIFY

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun signIn() {
        // Simulates Spotify OAuth / App Remote authentication handshake
        _authState.value = AuthState.LoggedIn(
            userId = "spotify_user_1",
            displayName = "Spotify Premium User",
            email = "user@spotify.com"
        )
    }

    override suspend fun signOut() {
        _authState.value = AuthState.LoggedOut
    }
}
