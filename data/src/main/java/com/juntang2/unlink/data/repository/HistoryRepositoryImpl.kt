package com.juntang2.unlink.data.repository

import com.juntang2.unlink.core.model.URLHistory
import com.juntang2.unlink.core.repository.HistoryRepository
import com.juntang2.unlink.data.local.db.HistoryDao
import com.juntang2.unlink.data.local.db.HistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {
    override fun getAllHistory(): Flow<List<URLHistory>> {
        return historyDao.getAllHistory().map { list -> list.map { it.toDomain() } }
    }

    override fun getFavorites(): Flow<List<URLHistory>> {
        return historyDao.getFavorites().map { list -> list.map { it.toDomain() } }
    }

    override fun searchHistory(query: String): Flow<List<URLHistory>> {
        return historyDao.searchHistory(query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertHistory(history: URLHistory) {
        historyDao.insert(HistoryEntity.fromDomain(history))
    }

    override suspend fun deleteHistory(history: URLHistory) {
        historyDao.delete(HistoryEntity.fromDomain(history))
    }

    override suspend fun clearHistory() {
        historyDao.clearAll()
    }

    override suspend fun updateHistory(history: URLHistory) {
        historyDao.update(HistoryEntity.fromDomain(history))
    }
}
