package com.juntang2.unlink.common.util

import org.junit.Assert.*
import org.junit.Test

class URLParserTest {

    @Test
    fun testExtractUrls() {
        val text = "Check out this link https://example.com/test and also http://another.org/path?query=1"
        val extracted = URLParser.extractUrls(text)
        assertEquals(2, extracted.size)
        assertEquals("https://example.com/test", extracted[0])
        assertEquals("http://another.org/path?query=1", extracted[1])
    }

    @Test
    fun testExtractUrlsNoMatch() {
        val text = "This is a simple text without any url."
        val extracted = URLParser.extractUrls(text)
        assertTrue(extracted.isEmpty())
    }

    @Test
    fun testIsValidUrl() {
        assertTrue(URLParser.isValidUrl("https://google.com"))
        assertTrue(URLParser.isValidUrl("http://example.com/page"))
        assertTrue(URLParser.isValidUrl("google.com")) // normalized format works
        assertFalse(URLParser.isValidUrl(""))
        assertFalse(URLParser.isValidUrl("   "))
        assertFalse(URLParser.isValidUrl("not a url"))
        assertFalse(URLParser.isValidUrl("http://google.com space"))
    }

    @Test
    fun testNormalizeUrl() {
        assertEquals("https://google.com", URLParser.normalizeUrl("google.com"))
        assertEquals("https://google.com", URLParser.normalizeUrl("https://google.com"))
        assertEquals("http://google.com", URLParser.normalizeUrl("http://google.com"))
        assertEquals("", URLParser.normalizeUrl(""))
    }

    @Test
    fun testGetDomain() {
        assertEquals("google.com", URLParser.getDomain("https://www.google.com/search"))
        assertEquals("example.co.uk", URLParser.getDomain("http://example.co.uk"))
        assertEquals("google.com", URLParser.getDomain("google.com"))
        assertEquals("", URLParser.getDomain("not a valid url!!!"))
    }

    @Test
    fun testCleanUrlDelegate() {
        val input = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&si=1234567890"
        val expected = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val cleaned = URLParser.cleanUrl(input)
        assertEquals(expected, cleaned)
    }
}
