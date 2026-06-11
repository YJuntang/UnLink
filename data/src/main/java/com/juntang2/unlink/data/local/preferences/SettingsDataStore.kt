package com.juntang2.unlink.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.juntang2.unlink.core.model.CleaningSettings
import com.juntang2.unlink.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val AGGRESSIVE_MODE = booleanPreferencesKey("aggressive_mode")
        val KEEP_AFFILIATE = booleanPreferencesKey("keep_affiliate")
        val EXPAND_SHORT_URL = booleanPreferencesKey("expand_short_url")
        val AUTO_SAVE_HISTORY = booleanPreferencesKey("auto_save_history")
        val CUSTOM_KEEP_RULES = stringSetPreferencesKey("custom_keep_rules")
        val CUSTOM_REMOVE_RULES = stringSetPreferencesKey("custom_remove_rules")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settingsFlow: Flow<CleaningSettings> = dataStore.data
        .map { preferences ->
            CleaningSettings(
                aggressiveMode = preferences[AGGRESSIVE_MODE] ?: false,
                keepAffiliate = preferences[KEEP_AFFILIATE] ?: false,
                expandShortUrl = preferences[EXPAND_SHORT_URL] ?: false,
                themeMode = preferences[THEME_MODE]?.let {
                    try {
                        ThemeMode.valueOf(it)
                    } catch (e: Exception) {
                        ThemeMode.AUTO
                    }
                } ?: ThemeMode.AUTO
            )
        }

    val customKeepRulesFlow: Flow<Set<String>> = dataStore.data
        .map { preferences -> preferences[CUSTOM_KEEP_RULES] ?: emptySet() }

    val customRemoveRulesFlow: Flow<Set<String>> = dataStore.data
        .map { preferences -> preferences[CUSTOM_REMOVE_RULES] ?: emptySet() }

    val autoSaveHistoryFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[AUTO_SAVE_HISTORY] ?: true }

    suspend fun updateAggressiveMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AGGRESSIVE_MODE] = enabled
        }
    }

    suspend fun updateKeepAffiliate(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEEP_AFFILIATE] = enabled
        }
    }

    suspend fun updateExpandShortUrl(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[EXPAND_SHORT_URL] = enabled
        }
    }

    suspend fun updateAutoSaveHistory(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_SAVE_HISTORY] = enabled
        }
    }

    suspend fun addCustomKeepRule(rule: String) {
        dataStore.edit { preferences ->
            val current = preferences[CUSTOM_KEEP_RULES] ?: emptySet()
            preferences[CUSTOM_KEEP_RULES] = current + rule
        }
    }

    suspend fun removeCustomKeepRule(rule: String) {
        dataStore.edit { preferences ->
            val current = preferences[CUSTOM_KEEP_RULES] ?: emptySet()
            preferences[CUSTOM_KEEP_RULES] = current - rule
        }
    }

    suspend fun addCustomRemoveRule(rule: String) {
        dataStore.edit { preferences ->
            val current = preferences[CUSTOM_REMOVE_RULES] ?: emptySet()
            preferences[CUSTOM_REMOVE_RULES] = current + rule
        }
    }

    suspend fun removeCustomRemoveRule(rule: String) {
        dataStore.edit { preferences ->
            val current = preferences[CUSTOM_REMOVE_RULES] ?: emptySet()
            preferences[CUSTOM_REMOVE_RULES] = current - rule
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    suspend fun resetSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
