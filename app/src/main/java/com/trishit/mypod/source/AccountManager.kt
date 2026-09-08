package com.trishit.mypod.source

import android.content.Context
import com.trishit.mypod.data.local.UserSettingsRepository
import com.trishit.mypod.source.applemusic.AppleMusicAuthProvider
import com.trishit.mypod.source.spotify.SpotifyAuthProvider
import com.trishit.mypod.source.youtube.YouTubeAuthProvider
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

    val enabledSourcesState: StateFlow<Set<PlaybackSourceType>> = userSettingsRepository.enabledSourcesFlow.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = setOf(PlaybackSourceType.LOCAL, PlaybackSourceType.YTDLP)
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

    val hasCompletedOnboardingState: StateFlow<Boolean> = userSettingsRepository.hasCompletedOnboardingFlow.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun setActiveSource(sourceType: PlaybackSourceType) {
        scope.launch(Dispatchers.IO) {
            userSettingsRepository.setActiveSource(sourceType)
        }
    }

    fun toggleSourceEnabled(sourceType: PlaybackSourceType) {
        scope.launch(Dispatchers.IO) {
            userSettingsRepository.toggleSourceEnabled(sourceType)
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

    fun setHasCompletedOnboarding(completed: Boolean) {
        scope.launch(Dispatchers.IO) {
            userSettingsRepository.setHasCompletedOnboarding(completed)
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
