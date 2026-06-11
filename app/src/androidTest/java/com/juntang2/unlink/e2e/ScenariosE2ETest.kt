package com.juntang2.unlink.e2e

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
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
class ScenariosE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testTC6_1_ShareIntentHandlingFlow() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://example.com/share?utm_source=intent")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.onNodeWithTag(TestTags.RESULT_CARD).assertIsDisplayed()
            composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/share")
        }
    }

    @Test
    fun testTC6_2_DeepLinkIntentHandlingFlow() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("unlink://clean?url=https://example.com/deep?utm_source=deeplink")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.onNodeWithTag(TestTags.RESULT_CARD).assertIsDisplayed()
            composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/deep")
        }
    }

    @Test
    fun testTC6_3_CuratorWorkflow() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            // 1. Clean link in Main
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/curator?utm_source=1")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
            composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/curator")
            
            // 2. Click Bulk and clean multiple links
            composeRule.onNodeWithText("Bulk").performClick()
            composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://example.com/curator?utm_source=1\nhttps://example.com/curator2?utm_source=2")
            composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
            
            // 3. Export to JSON
            composeRule.onNodeWithTag(TestTags.BULK_EXPORT_JSON).performClick()
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
            val jsonFile = File(targetContext.cacheDir, "cleaned_links.json")
            assert(jsonFile.exists())
            assert(jsonFile.readText().contains("https://example.com/curator"))
        }
    }

    @Test
    fun testTC6_4_SettingsInfluencesBulkAndHistory() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            // Set Keep Affiliate in settings
            composeRule.onNodeWithText("Settings").performClick()
            composeRule.onNodeWithTag(TestTags.RESET_SETTINGS_BUTTON).performClick()
            composeRule.onNodeWithTag(TestTags.KEEP_AFFILIATE_SWITCH).performClick()
            
            // Go to Bulk Screen and Clean Amazon links
            composeRule.onNodeWithText("Bulk").performClick()
            composeRule.onNodeWithTag(TestTags.BULK_INPUT).performTextInput("https://amazon.com/dp/B123?tag=aff-20&ref=sr_1")
            composeRule.onNodeWithTag(TestTags.BULK_CLEAN_BUTTON).performClick()
            
            // Verify cleaned text preserves tag
            composeRule.onNodeWithText("Cleaned: https://amazon.com/dp/B123?tag=aff-20").assertIsDisplayed()
            
            // Go to History Screen and verify same text is stored
            composeRule.onNodeWithText("History").performClick()
            composeRule.onNodeWithText("https://amazon.com/dp/B123?tag=aff-20").assertIsDisplayed()
        }
    }

    @Test
    fun testTC6_5_HistoryToMainRecleaning() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            // Clean a link to put it in history
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/reclean?utm_source=re")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
            
            // Go to History and read text
            composeRule.onNodeWithText("History").performClick()
            composeRule.onNodeWithText("https://example.com/reclean").assertIsDisplayed()
            
            // Go back to Main, clean a different link
            composeRule.onNodeWithText("Main").performClick()
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://example.com/reclean")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
            
            composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/reclean")
        }
    }

    @Test
    fun testTC6_6_ResetEverythingWorkflow() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.onNodeWithText("Settings").performClick()
            composeRule.onNodeWithTag(TestTags.AGGRESSIVE_MODE_SWITCH).performClick()
            composeRule.onNodeWithTag(TestTags.CUSTOM_KEEP_INPUT).performTextInput("cust_keep")
            composeRule.onNodeWithText("Add Keep").performClick()
            
            composeRule.onNodeWithText("Main").performClick()
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?cust_keep=1&custom_track=2")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
            
            // Go to Settings, reset settings and clear history
            composeRule.onNodeWithText("Settings").performClick()
            composeRule.onNodeWithTag(TestTags.CLEAR_HISTORY_BUTTON).performClick()
            composeRule.onNodeWithTag(TestTags.RESET_SETTINGS_BUTTON).performClick()
            
            // Check History is empty
            composeRule.onNodeWithText("History").performClick()
            composeRule.onNodeWithText("No History Found").assertIsDisplayed()
            
            // Check Main screen cleaning with old custom keep is now stripped
            composeRule.onNodeWithText("Main").performClick()
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://example.com/p?cust_keep=1")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
            composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p")
        }
    }

    @Test
    fun testTC6_7_ShortLinkChainResolutionToHistory() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.onNodeWithText("Settings").performClick()
            composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).performClick()
            
            composeRule.onNodeWithText("Main").performClick()
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://t.co/chain1")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
            
            // Check resolved target url
            composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/target")
            
            // Verify final target url is in history
            composeRule.onNodeWithText("History").performClick()
            composeRule.onNodeWithText("https://example.com/target").assertIsDisplayed()
        }
    }

    @Test
    fun testTC6_8_OfflineQrCodeGeneration() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/qr")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
            
            // Click QR Code button
            composeRule.onNodeWithTag(TestTags.QR_CODE_BUTTON).performClick()
            
            // Verify QR image and card are shown
            composeRule.onNodeWithTag(TestTags.QR_CODE_CARD).assertIsDisplayed()
            composeRule.onNodeWithTag(TestTags.QR_IMAGE).assertIsDisplayed()
            
            // Click Share QR Code, which writes to cache directory
            composeRule.onNodeWithText("Share QR Code").performClick()
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
            val qrFile = File(targetContext.cacheDir, "qr_code.png")
            assert(qrFile.exists())
        }
    }

    @Test
    fun testTC6_9_LoopingRedirectionProtection() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.onNodeWithText("Settings").performClick()
            composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).performClick()
            
            composeRule.onNodeWithText("Main").performClick()
            // loop.com/a redirects to loop.com/b redirects to loop.com/a
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://loop.com/a")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
            
            // Should stop resolving and complete successfully without hanging/crashing
            composeRule.onNodeWithTag(TestTags.RESULT_CARD).assertIsDisplayed()
        }
    }

    @Test
    fun testTC6_10_CustomRulesPrecedenceAndHistory() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.onNodeWithText("Settings").performClick()
            // Add custom keep: override
            composeRule.onNodeWithTag(TestTags.CUSTOM_KEEP_INPUT).performTextInput("override")
            composeRule.onNodeWithText("Add Keep").performClick()
            // Add custom remove: override (so they conflict)
            composeRule.onNodeWithTag(TestTags.CUSTOM_REMOVE_INPUT).performTextInput("override")
            composeRule.onNodeWithText("Add Remove").performClick()
            
            composeRule.onNodeWithText("Main").performClick()
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?override=123")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
            
            // Should be removed because remove rule takes precedence
            composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p")
            
            // Verify history has it cleaned correctly
            composeRule.onNodeWithText("History").performClick()
            composeRule.onNodeWithText("https://example.com/p").assertIsDisplayed()
        }
    }
}
