package com.juntang2.unlink.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.juntang2.unlink.core.model.URLHistory
import com.juntang2.unlink.common.util.URLParser

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalUrl: String,
    val cleanedUrl: String,
    val timestamp: Long,
    val isFavorite: Boolean = false,
    val domain: String
) {
    fun toDomain(): URLHistory = URLHistory(
        id = id,
        originalUrl = originalUrl,
        cleanedUrl = cleanedUrl,
        isFavorite = isFavorite,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(domainModel: URLHistory): HistoryEntity = HistoryEntity(
            id = domainModel.id,
            originalUrl = domainModel.originalUrl,
            cleanedUrl = domainModel.cleanedUrl,
            isFavorite = domainModel.isFavorite,
            timestamp = if (domainModel.timestamp > 0L) domainModel.timestamp else System.currentTimeMillis(),
            domain = URLParser.getDomain(domainModel.cleanedUrl)
        )
    }
}
