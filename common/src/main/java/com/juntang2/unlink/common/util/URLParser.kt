package com.juntang2.unlink.common.util

import java.net.URL

object URLParser {
    private val URL_REGEX = Regex(
        "(https?://[\\w\\-]+(\\.[\\w\\-]+)+(/\\S*)?)",
        RegexOption.IGNORE_CASE
    )

    fun extractUrls(text: String): List<String> {
        return URL_REGEX.findAll(text).map { it.value }.toList()
    }

    fun isValidUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ")) return false
        return try {
            URL(trimmed)
            true
        } catch (e: Exception) {
            try {
                val normalized = "https://$trimmed"
                URL(normalized)
                trimmed.contains(".") && !trimmed.startsWith(".") && !trimmed.endsWith(".")
            } catch (ex: Exception) {
                false
            }
        }
    }

    fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return ""
        return if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }
    
    fun getDomain(url: String): String {
        return try {
            val normalized = normalizeUrl(url)
            URL(normalized).host.removePrefix("www.")
        } catch (e: Exception) {
            ""
        }
    }

    fun cleanUrl(
        url: String,
        aggressiveMode: Boolean = false,
        keepAffiliate: Boolean = false,
        customKeepRules: Set<String> = emptySet(),
        customRemoveRules: Set<String> = emptySet()
    ): String {
        return ParameterFilter.cleanUrl(
            url = url,
            aggressiveMode = aggressiveMode,
            keepAffiliate = keepAffiliate,
            customKeepRules = customKeepRules,
            customRemoveRules = customRemoveRules
        )
    }
}
