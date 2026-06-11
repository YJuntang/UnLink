package com.juntang2.unlink.core.model

enum class ThemeMode {
    LIGHT, DARK, AUTO
}

data class CleaningSettings(
    val aggressiveMode: Boolean = false,
    val keepAffiliate: Boolean = false,
    val expandShortUrl: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.AUTO
)
