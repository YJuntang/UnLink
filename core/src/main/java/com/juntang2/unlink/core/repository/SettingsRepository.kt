package com.juntang2.unlink.core.repository

import com.juntang2.unlink.core.model.CleaningSettings
import com.juntang2.unlink.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<CleaningSettings>
    fun getCustomKeepRules(): Flow<Set<String>>
    fun getCustomRemoveRules(): Flow<Set<String>>
    
    suspend fun updateAggressiveMode(enabled: Boolean)
    suspend fun updateKeepAffiliate(enabled: Boolean)
    suspend fun updateExpandShortUrl(enabled: Boolean)
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun addCustomKeepRule(rule: String)
    suspend fun removeCustomKeepRule(rule: String)
    suspend fun addCustomRemoveRule(rule: String)
    suspend fun removeCustomRemoveRule(rule: String)
    suspend fun resetSettings()
}
