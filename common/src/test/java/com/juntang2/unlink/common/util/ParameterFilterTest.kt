package com.juntang2.unlink.common.util

import org.junit.Assert.*
import org.junit.Test

class ParameterFilterTest {

    @Test
    fun testCleanUrlEmptyAndBlank() {
        assertEquals("", ParameterFilter.cleanUrl(""))
        assertEquals("", ParameterFilter.cleanUrl("   "))
    }

    @Test
    fun testCleanUrlDefaultTrackers() {
        val input = "https://example.com/path?utm_source=google&utm_medium=cpc&utm_campaign=summer&fbclid=123&gclid=456&msclkid=789&normal_param=value"
        val expected = "https://example.com/path?normal_param=value"
        assertEquals(expected, ParameterFilter.cleanUrl(input))
    }

    @Test
    fun testCleanUrlYouTubeRules() {
        // v and t should be kept, si should be removed
        val input = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=30s&si=xyz123"
        val expected = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=30s"
        assertEquals(expected, ParameterFilter.cleanUrl(input))
        
        val shortInput = "https://youtu.be/dQw4w9WgXcQ?si=xyz123&t=30"
        val expectedShort = "https://youtu.be/dQw4w9WgXcQ?t=30"
        assertEquals(expectedShort, ParameterFilter.cleanUrl(shortInput))
    }

    @Test
    fun testCleanUrlThreadsAndSocial() {
        // Threads/Instagram tracking
        val inputThreads = "https://www.threads.com/@user/post/123?xmt=abc&slof=1&igshid=def"
        val expectedThreads = "https://www.threads.com/@user/post/123"
        assertEquals(expectedThreads, ParameterFilter.cleanUrl(inputThreads))
        
        // Twitter tracking
        val inputTwitter = "https://twitter.com/user/status/123?t=xyz&s=19&twterm=abc"
        val expectedTwitter = "https://twitter.com/user/status/123"
        assertEquals(expectedTwitter, ParameterFilter.cleanUrl(inputTwitter))
    }

    @Test
    fun testCleanUrlAmazonRules() {
        // ref should be removed, tag should be kept only if keepAffiliate is true
        val input = "https://www.amazon.com/dp/B000000000?tag=my_tag-20&ref=dp_carousel"
        
        // case 1: keepAffiliate = false (default)
        val expectedNoAffiliate = "https://www.amazon.com/dp/B000000000"
        assertEquals(expectedNoAffiliate, ParameterFilter.cleanUrl(input, keepAffiliate = false))
        
        // case 2: keepAffiliate = true
        val expectedWithAffiliate = "https://www.amazon.com/dp/B000000000?tag=my_tag-20"
        assertEquals(expectedWithAffiliate, ParameterFilter.cleanUrl(input, keepAffiliate = true))
    }

    @Test
    fun testCleanUrlCustomRules() {
        val input = "https://example.com/page?keep_me=1&remove_me=2&utm_source=google"
        
        val result = ParameterFilter.cleanUrl(
            url = input,
            customKeepRules = setOf("keep_me", "utm_source"),
            customRemoveRules = setOf("remove_me")
        )
        
        // utm_source is normally a default tracker but should be kept due to customKeepRules
        assertTrue(result.contains("keep_me=1"))
        assertTrue(result.contains("utm_source=google"))
        assertFalse(result.contains("remove_me"))
    }

    @Test
    fun testCleanUrlAggressiveMode() {
        val input = "https://example.com/page?q=search_query&custom_param=123&sec_token=xyz&utm_source=google"
        
        // aggressiveMode = false (default): custom_param should be kept, utm_source removed
        val resultNormal = ParameterFilter.cleanUrl(input, aggressiveMode = false)
        assertTrue(resultNormal.contains("custom_param=123"))
        assertTrue(resultNormal.contains("q=search_query"))
        assertFalse(resultNormal.contains("utm_source"))

        // aggressiveMode = true: custom_param should be removed (not a common key), sec_token kept (starts with sec), q kept (common key)
        val resultAggressive = ParameterFilter.cleanUrl(input, aggressiveMode = true)
        assertFalse(resultAggressive.contains("custom_param"))
        assertTrue(resultAggressive.contains("sec_token=xyz"))
        assertTrue(resultAggressive.contains("q=search_query"))
        assertFalse(resultAggressive.contains("utm_source"))
    }

    @Test
    fun testCleanUrlFragmentStripping() {
        // Default trackers in fragment should be stripped
        val input = "https://example.com/page#section1&utm_source=facebook&valid_frag=hello"
        val expected = "https://example.com/page#section1&valid_frag=hello"
        assertEquals(expected, ParameterFilter.cleanUrl(input))

        // Entire fragment is tracking parameters -> fragment is fully stripped
        val inputAllTrack = "https://example.com/page#utm_campaign=summer&fbclid=xyz"
        val expectedAllTrack = "https://example.com/page"
        assertEquals(expectedAllTrack, ParameterFilter.cleanUrl(inputAllTrack))
    }

    @Test
    fun testCleanUrlMalformed() {
        // Should return normalized/original url gracefully on exception
        val malformed = "ht!@#$%tp://bad.com?utm_source=1"
        assertEquals("https://ht!@#$%tp://bad.com?utm_source=1", ParameterFilter.cleanUrl(malformed))
    }
}
