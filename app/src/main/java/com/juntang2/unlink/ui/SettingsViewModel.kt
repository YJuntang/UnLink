package com.juntang2.unlink.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juntang2.unlink.core.model.CleaningSettings
import com.juntang2.unlink.core.model.ThemeMode
import com.juntang2.unlink.core.repository.HistoryRepository
import com.juntang2.unlink.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val settings: StateFlow<CleaningSettings> = settingsRepository.getSettings()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            CleaningSettings()
        )

    val customKeepRules: StateFlow<Set<String>> = settingsRepository.getCustomKeepRules()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptySet()
        )

    val customRemoveRules: StateFlow<Set<String>> = settingsRepository.getCustomRemoveRules()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptySet()
        )

    fun updateAggressiveMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAggressiveMode(enabled)
        }
    }

    fun updateKeepAffiliate(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateKeepAffiliate(enabled)
        }
    }

    fun updateExpandShortUrl(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateExpandShortUrl(enabled)
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }

    fun addCustomKeepRule(rule: String) {
        val trimmed = rule.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ") || trimmed.length > 100) return
        viewModelScope.launch {
            settingsRepository.addCustomKeepRule(trimmed)
        }
    }

    fun removeCustomKeepRule(rule: String) {
        viewModelScope.launch {
            settingsRepository.removeCustomKeepRule(rule)
        }
    }

    fun addCustomRemoveRule(rule: String) {
        val trimmed = rule.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ") || trimmed.length > 100) return
        viewModelScope.launch {
            settingsRepository.addCustomRemoveRule(trimmed)
        }
    }

    fun removeCustomRemoveRule(rule: String) {
        viewModelScope.launch {
            settingsRepository.removeCustomRemoveRule(rule)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.resetSettings()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}
