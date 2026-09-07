package com.bytekoders.mypod.source

import android.content.Context
import com.bytekoders.mypod.data.local.UserSettingsRepository
import com.bytekoders.mypod.source.applemusic.AppleMusicAuthProvider
import com.bytekoders.mypod.source.spotify.SpotifyAuthProvider
import com.bytekoders.mypod.source.youtube.YouTubeAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountManager(
    context: Context,
    private val scope: CoroutineScope
) {
    val userSettingsRepository = UserSettingsRepository(context.applicationContext)

    val spotifyAuthProvider = SpotifyAuthProvider()
    val youtubeAuthProvider = YouTubeAuthProvider()
    val appleMusicAuthProvider = AppleMusicAuthProvider()

    val activeSourceState: StateFlow<PlaybackSourceType> = userSettingsRepository.activeSourceFlow.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaybackSourceType.LOCAL
    )

    val ytdlpResolverEnabledState: StateFlow<Boolean> = userSettingsRepository.ytdlpResolverEnabledFlow.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val geminiApiKeyState: StateFlow<String> = userSettingsRepository.geminiApiKeyFlow.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    fun setActiveSource(sourceType: PlaybackSourceType) {
        scope.launch(Dispatchers.IO) {
            userSettingsRepository.setActiveSource(sourceType)
        }
    }

    fun setYtdlpResolverEnabled(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            userSettingsRepository.setYtdlpResolverEnabled(enabled)
        }
    }

    fun setGeminiApiKey(apiKey: String) {
        scope.launch(Dispatchers.IO) {
            userSettingsRepository.setGeminiApiKey(apiKey)
        }
    }

    fun getAuthProvider(type: PlaybackSourceType): AuthProvider? {
        return when (type) {
            PlaybackSourceType.SPOTIFY -> spotifyAuthProvider
            PlaybackSourceType.YOUTUBE -> youtubeAuthProvider
            PlaybackSourceType.APPLE_MUSIC -> appleMusicAuthProvider
            else -> null
        }
    }
}
