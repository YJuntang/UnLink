package com.juntang2.unlink.ui

import androidx.lifecycle.ViewModel
import com.juntang2.unlink.core.model.URLHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor() : ViewModel() {
    private val _historyList = MutableStateFlow<List<URLHistory>>(emptyList())
    val historyList: StateFlow<List<URLHistory>> = _historyList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun toggleFavorite(item: URLHistory) {
        _historyList.value = _historyList.value.map {
            if (it.id == item.id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun deleteHistory(item: URLHistory) {
        _historyList.value = _historyList.value.filter { it.id != item.id }
    }
}
