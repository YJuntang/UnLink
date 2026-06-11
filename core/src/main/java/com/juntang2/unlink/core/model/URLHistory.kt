package com.juntang2.unlink.core.model

data class URLHistory(
    val id: Long = 0,
    val originalUrl: String = "",
    val cleanedUrl: String = "",
    val isFavorite: Boolean = false,
    val timestamp: Long = 0L
)
