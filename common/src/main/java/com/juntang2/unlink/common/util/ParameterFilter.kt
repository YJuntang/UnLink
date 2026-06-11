package com.juntang2.unlink.common.util

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

object ParameterFilter {
    private val DEFAULT_TRACKERS = setOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_content", "utm_term", "utm_id",
        "fbclid", "gclid", "gclsrc", "dclid", "msclkid",
        // Threads / Instagram
        "xmt", "slof", "igshid", "ig_rid",
        // Twitter
        "twcamp", "twt", "twterm",
        // YouTube / Spotify
        "si", "ndid",
        // Other common trackers
        "mc_cid", "mc_eid", "_bta_tid", "_bta_c", "mkt_tok"
    )

    fun cleanUrl(
        url: String,
        aggressiveMode: Boolean = false,
        keepAffiliate: Boolean = false,
        customKeepRules: Set<String> = emptySet(),
        customRemoveRules: Set<String> = emptySet()
    ): String {
        if (url.trim().isEmpty()) return ""
        val normalized = URLParser.normalizeUrl(url)
        return try {
            val uri = URI(normalized)
            val domain = URLParser.getDomain(normalized)
            val query = uri.rawQuery

            val params = if (query != null) {
                query.split("&").mapNotNull {
                    val parts = it.split("=", limit = 2)
                    if (parts.isEmpty()) return@mapNotNull null
                    val key = URLDecoder.decode(parts[0], "UTF-8")
                    val value = if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
                    key to value
                }
            } else {
                emptyList()
            }

            val cleanedParams = params.filter { (key, _) ->
                // 1. Custom remove rules take precedence
                if (customRemoveRules.contains(key)) return@filter false

                // 2. Custom keep rules take precedence over clean logic
                if (customKeepRules.contains(key)) return@filter true

                // 3. YouTube specific rule
                if (domain.contains("youtube.com") || domain.contains("youtu.be")) {
                    if (key == "si") return@filter false
                    if (key == "v" || key == "t") return@filter true
                }

                // 4. Amazon affiliate tag
                if (domain.contains("amazon.")) {
                    if (key == "tag") return@filter keepAffiliate
                    if (key == "ref") return@filter false
                }

                // 5. Twitter / X specific rule
                if (domain.contains("twitter.com") || domain.contains("x.com")) {
                    if (key == "t" || key == "s") return@filter false
                }

                // 6. Default tracker check
                if (DEFAULT_TRACKERS.contains(key.lowercase())) return@filter false

                // 6. Aggressive mode: remove extra parameters that aren't common standard keys
                if (aggressiveMode) {
                    val commonKeys = setOf("id", "q", "v", "t", "search", "query", "page", "p", "ref", "tag")
                    if (!commonKeys.contains(key.lowercase()) && !key.lowercase().startsWith("sec")) {
                        return@filter false
                    }
                }

                true
            }

            val newQuery = cleanedParams.joinToString("&") { (key, value) ->
                val encodedKey = URLEncoder.encode(key, "UTF-8")
                val encodedValue = URLEncoder.encode(value, "UTF-8")
                if (value.isNotEmpty()) "$encodedKey=$encodedValue" else encodedKey
            }

            val newUri = URI(
                uri.scheme,
                uri.authority,
                uri.path,
                if (newQuery.isNotEmpty()) newQuery else null,
                uri.fragment
            )
            
            // Fragment tracking stripping (TC1.5: fragment has utm_content etc)
            var result = newUri.toString()
            val fragment = uri.fragment
            if (fragment != null) {
                val fragmentParts = fragment.split("&").filter {
                    val key = it.split("=")[0].lowercase()
                    !DEFAULT_TRACKERS.contains(key) && !customRemoveRules.contains(key)
                }
                result = if (fragmentParts.isNotEmpty()) {
                    result.substringBefore("#") + "#" + fragmentParts.joinToString("&")
                } else {
                    result.substringBefore("#")
                }
            }
            result
        } catch (e: Exception) {
            normalized
        }
    }
}
