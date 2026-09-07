package com.bytekoders.mypod.source.applemusic

import com.bytekoders.mypod.source.AuthProvider
import com.bytekoders.mypod.source.AuthState
import com.bytekoders.mypod.source.PlaybackSourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppleMusicAuthProvider : AuthProvider {

    override val serviceType: PlaybackSourceType = PlaybackSourceType.APPLE_MUSIC

    private val _authState = MutableStateFlow<AuthState>(AuthState.ComingSoon("Apple Music"))
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun signIn() {
        // Stub - Coming Soon until MusicKit developer credentials are configured
    }

    override suspend fun signOut() {
        // Stub
    }
}
