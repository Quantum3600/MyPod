package com.bytekoders.mypod.source

import kotlinx.coroutines.flow.StateFlow

sealed interface AuthState {
    data object LoggedOut : AuthState
    data class LoggedIn(
        val userId: String,
        val displayName: String,
        val email: String? = null,
        val avatarUrl: String? = null
    ) : AuthState
    data class ComingSoon(val serviceName: String) : AuthState
}

interface AuthProvider {
    val serviceType: PlaybackSourceType
    val authState: StateFlow<AuthState>

    suspend fun signIn()
    suspend fun signOut()
}
