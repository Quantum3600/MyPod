package com.trishit.mypod.data.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ipod_settings")

class ThemeRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val CLICK_SOUND_ENABLED = booleanPreferencesKey("click_sound_enabled")
    }

    val selectedThemeFlow: Flow<ThemePreset> = context.dataStore.data.map { preferences ->
        val presetId = preferences[PreferencesKeys.THEME_PRESET] ?: ThemePreset.SPACE_GRAY.id
        ThemePreset.fromId(presetId)
    }

    val clickSoundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CLICK_SOUND_ENABLED] ?: true
    }

    suspend fun setThemePreset(preset: ThemePreset) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_PRESET] = preset.id
        }
    }

    suspend fun setClickSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLICK_SOUND_ENABLED] = enabled
        }
    }
}
