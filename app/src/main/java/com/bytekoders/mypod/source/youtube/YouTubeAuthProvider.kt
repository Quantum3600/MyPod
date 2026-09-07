package com.bytekoders.mypod.source.youtube

import com.bytekoders.mypod.source.AuthProvider
import com.bytekoders.mypod.source.AuthState
import com.bytekoders.mypod.source.PlaybackSourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class YouTubeAuthProvider : AuthProvider {

    override val serviceType: PlaybackSourceType = PlaybackSourceType.YOUTUBE

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun signIn() {
        _authState.value = AuthState.LoggedIn(
            userId = "yt_user_1",
            displayName = "YouTube Music Subscriber",
            email = "user@gmail.com"
        )
    }

    override suspend fun signOut() {
        _authState.value = AuthState.LoggedOut
    }
}
