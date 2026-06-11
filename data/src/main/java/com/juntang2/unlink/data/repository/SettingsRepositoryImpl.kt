package com.juntang2.unlink.data.repository

import com.juntang2.unlink.core.model.CleaningSettings
import com.juntang2.unlink.core.model.ThemeMode
import com.juntang2.unlink.core.repository.SettingsRepository
import com.juntang2.unlink.data.local.preferences.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {
    override fun getSettings(): Flow<CleaningSettings> {
        return settingsDataStore.settingsFlow
    }

    override fun getCustomKeepRules(): Flow<Set<String>> {
        return settingsDataStore.customKeepRulesFlow
    }

    override fun getCustomRemoveRules(): Flow<Set<String>> {
        return settingsDataStore.customRemoveRulesFlow
    }

    override suspend fun updateAggressiveMode(enabled: Boolean) {
        settingsDataStore.updateAggressiveMode(enabled)
    }

    override suspend fun updateKeepAffiliate(enabled: Boolean) {
        settingsDataStore.updateKeepAffiliate(enabled)
    }

    override suspend fun updateExpandShortUrl(enabled: Boolean) {
        settingsDataStore.updateExpandShortUrl(enabled)
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        settingsDataStore.updateThemeMode(themeMode)
    }

    override suspend fun addCustomKeepRule(rule: String) {
        settingsDataStore.addCustomKeepRule(rule)
    }

    override suspend fun removeCustomKeepRule(rule: String) {
        settingsDataStore.removeCustomKeepRule(rule)
    }

    override suspend fun addCustomRemoveRule(rule: String) {
        settingsDataStore.addCustomRemoveRule(rule)
    }

    override suspend fun removeCustomRemoveRule(rule: String) {
        settingsDataStore.removeCustomRemoveRule(rule)
    }

    override suspend fun resetSettings() {
        settingsDataStore.resetSettings()
    }
}
