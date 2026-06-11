package com.juntang2.unlink.e2e

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juntang2.unlink.MainActivity
import com.juntang2.unlink.util.TestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
        // Reset settings
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.RESET_SETTINGS_BUTTON).performClick()
        composeRule.onNodeWithText("Main").performClick()
    }

    @Test
    fun testTC5_1_SettingsAggressiveModeToggle() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.AGGRESSIVE_MODE_SWITCH).performClick()
        composeRule.onNodeWithText("Main").performClick()

        // With aggressive mode, non-standard tracker "custom_track" is removed
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?custom_track=abc&id=10")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p?id=10")
    }

    @Test
    fun testTC5_2_SettingsAggressiveModePersistent() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.AGGRESSIVE_MODE_SWITCH).performClick()
        
        // Navigate away and back
        composeRule.onNodeWithText("Main").performClick()
        composeRule.onNodeWithText("Settings").performClick()
        
        composeRule.onNodeWithTag(TestTags.AGGRESSIVE_MODE_SWITCH).assertIsOn()
    }

    @Test
    fun testTC5_3_SettingsKeepAffiliateToggle() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.KEEP_AFFILIATE_SWITCH).performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://amazon.com/dp/B123?tag=aff-20&ref=sr_1")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://amazon.com/dp/B123?tag=aff-20")
    }

    @Test
    fun testTC5_4_SettingsKeepAffiliatePersistent() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.KEEP_AFFILIATE_SWITCH).performClick()
        
        composeRule.onNodeWithText("Main").performClick()
        composeRule.onNodeWithText("Settings").performClick()
        
        composeRule.onNodeWithTag(TestTags.KEEP_AFFILIATE_SWITCH).assertIsOn()
    }

    @Test
    fun testTC5_5_SettingsExpandShortUrlToggle() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://bit.ly/short1")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/target?id=42")
    }

    @Test
    fun testTC5_6_SettingsExpandShortUrlPersistent() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).performClick()
        
        composeRule.onNodeWithText("Main").performClick()
        composeRule.onNodeWithText("Settings").performClick()
        
        composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).assertIsOn()
    }

    @Test
    fun testTC5_7_SettingsAddCustomKeepRule() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.CUSTOM_KEEP_INPUT).performTextInput("custom_keep")
        composeRule.onNodeWithText("Add Keep").performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?custom_keep=val&utm_source=google")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        // custom_keep is preserved, utm_source is removed
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p?custom_keep=val")
    }

    @Test
    fun testTC5_8_SettingsAddCustomRemoveRule() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.CUSTOM_REMOVE_INPUT).performTextInput("custom_remove")
        composeRule.onNodeWithText("Add Remove").performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?custom_remove=val&id=1")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        // custom_remove is stripped, id is kept
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p?id=1")
    }

    @Test
    fun testTC5_9_SettingsCustomRemovePrecedence() {
        composeRule.onNodeWithText("Settings").performClick()
        // Add to keep
        composeRule.onNodeWithTag(TestTags.CUSTOM_KEEP_INPUT).performTextInput("both")
        composeRule.onNodeWithText("Add Keep").performClick()
        // Add to remove
        composeRule.onNodeWithTag(TestTags.CUSTOM_REMOVE_INPUT).performTextInput("both")
        composeRule.onNodeWithText("Add Remove").performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?both=123")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        // Should be removed because remove rule takes precedence
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p")
    }

    @Test
    fun testTC5_10_SettingsAddDuplicateCustomRules() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.CUSTOM_KEEP_INPUT).performTextInput("dup")
        composeRule.onNodeWithText("Add Keep").performClick()
        composeRule.onNodeWithTag(TestTags.CUSTOM_KEEP_INPUT).performTextInput("dup")
        composeRule.onNodeWithText("Add Keep").performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?dup=1&utm_source=2")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p?dup=1")
    }

    @Test
    fun testTC5_11_SettingsInvalidCustomRule() {
        composeRule.onNodeWithText("Settings").performClick()
        
        // rule with spaces
        composeRule.onNodeWithTag(TestTags.CUSTOM_KEEP_INPUT).performTextInput("invalid space")
        composeRule.onNodeWithText("Add Keep").performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?invalid%20space=1&utm_source=g")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        // Should be stripped since it was invalid and not added to keeps
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p")
    }

    @Test
    fun testTC5_12_SettingsResetAll() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.AGGRESSIVE_MODE_SWITCH).performClick()
        composeRule.onNodeWithTag(TestTags.KEEP_AFFILIATE_SWITCH).performClick()
        composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).performClick()
        
        composeRule.onNodeWithTag(TestTags.RESET_SETTINGS_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TestTags.AGGRESSIVE_MODE_SWITCH).assertIsOff()
        composeRule.onNodeWithTag(TestTags.KEEP_AFFILIATE_SWITCH).assertIsOff()
        composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).assertIsOff()
    }

    @Test
    fun testTC5_13_SettingsCustomKeepTriggersAlways() {
        composeRule.onNodeWithText("Settings").performClick()
        // Add utm_source as custom keep
        composeRule.onNodeWithTag(TestTags.CUSTOM_KEEP_INPUT).performTextInput("utm_source")
        composeRule.onNodeWithText("Add Keep").performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?utm_source=override")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        // utm_source should be kept because of keep rule
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p?utm_source=override")
    }

    @Test
    fun testTC5_14_SettingsCustomRemoveTriggersAlways() {
        composeRule.onNodeWithText("Settings").performClick()
        // Add id as custom remove
        composeRule.onNodeWithTag(TestTags.CUSTOM_REMOVE_INPUT).performTextInput("id")
        composeRule.onNodeWithText("Add Remove").performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?id=123")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        // id should be removed because of custom remove rule
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p")
    }

    @Test
    fun testTC5_15_SettingsResetClearsCustomRulesText() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.CUSTOM_KEEP_INPUT).performTextInput("mykeep")
        composeRule.onNodeWithText("Add Keep").performClick()
        
        composeRule.onNodeWithTag(TestTags.RESET_SETTINGS_BUTTON).performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?mykeep=val&utm_source=g")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        // mykeep should be stripped because custom rules were reset
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p")
    }
}
