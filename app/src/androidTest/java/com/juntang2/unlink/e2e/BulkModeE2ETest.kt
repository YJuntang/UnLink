package com.juntang2.unlink.e2e

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.juntang2.unlink.MainActivity
import com.juntang2.unlink.util.TestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BulkModeE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testTC3_1_BulkCleanSimple() {
        composeRule.onNodeWithText("Bulk").performClick()
        
        composeRule.onNodeWithTag(TestTags.BULK_INPUT)
            .performTextInput("https://example.com/path?utm_source=google&id=10")
        
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("Original: https://example.com/path?utm_source=google&id=10").assertIsDisplayed()
        composeRule.onNodeWithText("Cleaned: https://example.com/path?id=10").assertIsDisplayed()
    }

    @Test
    fun testTC3_2_BulkCleanMultiple() {
        composeRule.onNodeWithText("Bulk").performClick()
        
        val input = "https://example.com/path?utm_source=google&id=10\nhttps://example.com/path2?utm_medium=cpc&ref=xyz"
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput(input)
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("Cleaned: https://example.com/path?id=10").assertIsDisplayed()
        composeRule.onNodeWithText("Cleaned: https://example.com/path2?ref=xyz").assertIsDisplayed()
    }

    @Test
    fun testTC3_3_BulkCleanWithMixedText() {
        composeRule.onNodeWithText("Bulk").performClick()
        
        val input = "Check this out: https://example.com/path?utm_source=google&id=10 and also https://example.com/path2?utm_medium=cpc"
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput(input)
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("Cleaned: https://example.com/path?id=10").assertIsDisplayed()
        composeRule.onNodeWithText("Cleaned: https://example.com/path2").assertIsDisplayed()
    }

    @Test
    fun testTC3_4_BulkCleanEmpty() {
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.BULK_RESULTS_LIST).assertExists()
    }

    @Test
    fun testTC3_5_BulkCleanWhitespaceOnly() {
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("   \n   ")
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.BULK_RESULTS_LIST).assertExists()
    }

    @Test
    fun testTC3_6_BulkCleanDuplicates() {
        composeRule.onNodeWithText("Bulk").performClick()
        val input = "https://example.com/p?utm_source=g\nhttps://example.com/p?utm_source=g"
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput(input)
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onAllNodesWithText("Cleaned: https://example.com/p").assertCountEquals(2)
    }

    @Test
    fun testTC3_7_BulkExportCSV() {
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://example.com/p?utm_source=g")
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TestTags.BULK_EXPORT_CSV).performClick()
        
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val csvFile = File(targetContext.cacheDir, "cleaned_links.csv")
        assert(csvFile.exists())
        val content = csvFile.readText()
        assert(content.contains("https://example.com/p"))
    }

    @Test
    fun testTC3_8_BulkExportJSON() {
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://example.com/p?utm_source=g")
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TestTags.BULK_EXPORT_JSON).performClick()
        
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val jsonFile = File(targetContext.cacheDir, "cleaned_links.json")
        assert(jsonFile.exists())
        val content = jsonFile.readText()
        assert(content.contains("https://example.com/p"))
    }

    @Test
    fun testTC3_9_BulkClear() {
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://example.com/p?utm_source=g")
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        composeRule.onNodeWithText("Cleaned: https://example.com/p").assertIsDisplayed()
        
        composeRule.onNodeWithText("Clear").performClick()
        composeRule.onNodeWithText("Cleaned: https://example.com/p").assertDoesNotExist()
    }

    @Test
    fun testTC3_10_BulkWithNonUtmUrls() {
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://example.com/p?id=10")
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        composeRule.onNodeWithText("Cleaned: https://example.com/p?id=10").assertIsDisplayed()
    }

    @Test
    fun testTC3_11_BulkAggressiveMode() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.AGGRESSIVE_MODE_SWITCH).performClick()
        
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://example.com/p?custom_track=abc&id=10")
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("Cleaned: https://example.com/p?id=10").assertIsDisplayed()
    }

    @Test
    fun testTC3_12_BulkKeepAffiliate() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.RESET_SETTINGS_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.KEEP_AFFILIATE_SWITCH).performClick()
        
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://amazon.com/dp/B123?tag=aff-20&ref=sr_1")
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("Cleaned: https://amazon.com/dp/B123?tag=aff-20").assertIsDisplayed()
    }

    @Test
    fun testTC3_13_BulkCustomRules() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.RESET_SETTINGS_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CUSTOM_REMOVE_INPUT).performTextInput("my_track")
        composeRule.onNodeWithText("Add Remove").performClick()
        
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://example.com/p?my_track=123&other=456")
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("Cleaned: https://example.com/p?other=456").assertIsDisplayed()
    }

    @Test
    fun testTC3_14_BulkHistoryAutoSave() {
        composeRule.onNodeWithText("Bulk").performClick()
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://example.com/bulk_save?utm_source=bulk")
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("https://example.com/bulk_save").assertIsDisplayed()
    }

    @Test
    fun testTC3_15_BulkParallelCleaning() {
        composeRule.onNodeWithText("Bulk").performClick()
        val urls = (1..50).joinToString("\n") { "https://example.com/p$it?utm_source=bulk$it" }
        composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput(urls)
        composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("Cleaned: https://example.com/p1").assertIsDisplayed()
        composeRule.onNodeWithText("Cleaned: https://example.com/p25").assertIsDisplayed()
        composeRule.onNodeWithText("Cleaned: https://example.com/p50").assertIsDisplayed()
    }
}
