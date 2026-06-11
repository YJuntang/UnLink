package com.juntang2.unlink.core.repository

import com.juntang2.unlink.core.model.URLHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<URLHistory>>
    fun getFavorites(): Flow<List<URLHistory>>
    fun searchHistory(query: String): Flow<List<URLHistory>>
    suspend fun insertHistory(history: URLHistory)
    suspend fun deleteHistory(history: URLHistory)
    suspend fun clearHistory()
    suspend fun updateHistory(history: URLHistory)
}
