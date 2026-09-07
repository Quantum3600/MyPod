package com.bytekoders.mypod.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bytekoders.mypod.source.PlaybackSourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userSettingsDataStore by preferencesDataStore(name = "user_settings")

class UserSettingsRepository(private val context: Context) {

    private object Keys {
        val ACTIVE_SOURCE = stringPreferencesKey("active_source")
        val YTDLP_RESOLVER_ENABLED = booleanPreferencesKey("ytdlp_resolver_enabled")
        val SAF_FOLDER_URI = stringPreferencesKey("saf_folder_uri")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    }

    val activeSourceFlow: Flow<PlaybackSourceType> = context.userSettingsDataStore.data.map { prefs ->
        val name = prefs[Keys.ACTIVE_SOURCE] ?: PlaybackSourceType.LOCAL.name
        runCatching { PlaybackSourceType.valueOf(name) }.getOrDefault(PlaybackSourceType.LOCAL)
    }

    val ytdlpResolverEnabledFlow: Flow<Boolean> = context.userSettingsDataStore.data.map { prefs ->
        prefs[Keys.YTDLP_RESOLVER_ENABLED] ?: false
    }

    val safFolderUriFlow: Flow<String?> = context.userSettingsDataStore.data.map { prefs ->
        prefs[Keys.SAF_FOLDER_URI]
    }

    val geminiApiKeyFlow: Flow<String> = context.userSettingsDataStore.data.map { prefs ->
        prefs[Keys.GEMINI_API_KEY] ?: ""
    }

    val hasCompletedOnboardingFlow: Flow<Boolean> = context.userSettingsDataStore.data.map { prefs ->
        prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false
    }

    suspend fun setActiveSource(sourceType: PlaybackSourceType) {
        context.userSettingsDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_SOURCE] = sourceType.name
        }
    }

    suspend fun setYtdlpResolverEnabled(enabled: Boolean) {
        context.userSettingsDataStore.edit { prefs ->
            prefs[Keys.YTDLP_RESOLVER_ENABLED] = enabled
        }
    }

    suspend fun setSafFolderUri(uri: String?) {
        context.userSettingsDataStore.edit { prefs ->
            if (uri != null) {
                prefs[Keys.SAF_FOLDER_URI] = uri
            } else {
                prefs.remove(Keys.SAF_FOLDER_URI)
            }
        }
    }

    suspend fun setGeminiApiKey(apiKey: String) {
        context.userSettingsDataStore.edit { prefs ->
            prefs[Keys.GEMINI_API_KEY] = apiKey
        }
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        context.userSettingsDataStore.edit { prefs ->
            prefs[Keys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }
}
