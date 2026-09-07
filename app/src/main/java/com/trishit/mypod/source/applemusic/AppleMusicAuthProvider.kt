package com.trishit.mypod.source.applemusic

import com.trishit.mypod.source.AuthProvider
import com.trishit.mypod.source.AuthState
import com.trishit.mypod.source.PlaybackSourceType
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
