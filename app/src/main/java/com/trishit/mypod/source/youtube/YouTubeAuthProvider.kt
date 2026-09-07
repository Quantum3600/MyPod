package com.trishit.mypod.source.youtube

import com.trishit.mypod.source.AuthProvider
import com.trishit.mypod.source.AuthState
import com.trishit.mypod.source.PlaybackSourceType
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
