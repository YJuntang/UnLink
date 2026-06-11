package com.juntang2.unlink.e2e

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juntang2.unlink.MainActivity
import com.juntang2.unlink.util.TestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CleanerE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testTC1_1_StandardUtmCleaning() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/path?utm_source=google&utm_medium=cpc&id=10")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/path?id=10")
    }

    @Test
    fun testTC1_2_YouTubeVideoPreservation() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://www.youtube.com/watch?v=dQw4w9WgXcQ&si=12345&t=30")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://youtube.com/watch?v=dQw4w9WgXcQ&t=30")
    }

    @Test
    fun testTC1_3_AmazonAffiliateStripping() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://amazon.com/dp/B12345?tag=myaff-20&ref=sr_1_1")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://amazon.com/dp/B12345")
    }

    @Test
    fun testTC1_4_NoSchemeNormalization() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("example.com/page?utm_campaign=winter")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/page")
    }

    @Test
    fun testTC1_5_FragmentTrackingStripping() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/page?id=1#utm_content=banner")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/page?id=1")
    }

    @Test
    fun testTC1_6_BitlyResolution() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://bit.ly/short1")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/target?id=42")
    }

    @Test
    fun testTC1_7_YoutubeResolution() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://youtu.be/abc?si=123")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://youtube.com/watch?v=abc")
    }

    @Test
    fun testTC1_8_ExpansionToggleOff() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.RESET_SETTINGS_BUTTON).performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://bit.ly/short1")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://bit.ly/short1")
    }

    @Test
    fun testTC1_9_NestedRedirectionChain() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://t.co/chain1")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/target")
    }

    @Test
    fun testTC1_10_TinyurlResolution() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.EXPAND_SHORT_SWITCH).performClick()
        composeRule.onNodeWithText("Main").performClick()

        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://tinyurl.com/short2")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://tinyurl.com/short2")
    }

    @Test
    fun testTC2_1_MalformedUrlString() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("ht!@#$%tp://bad.com")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithText("Invalid URL format").assertIsDisplayed()
    }

    @Test
    fun testTC2_2_ParameterBloat() {
        val longBuilder = StringBuilder("https://example.com/path?")
        for (i in 1..100) {
            longBuilder.append("param$i=value$i&")
        }
        longBuilder.append("utm_source=bloat")
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput(longBuilder.toString())
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        val cleaned = composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).fetchText()
        assert(!cleaned.contains("utm_source"))
        assert(cleaned.contains("param100=value100"))
    }

    @Test
    fun testTC2_3_InternationalDomains() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://xn--mgbh0fb.xn--kgbechtv/صفحة?name=محمد&utm_campaign=winter")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://xn--mgbh0fb.xn--kgbechtv/صفحة?name=محمد")
    }

    @Test
    fun testTC2_4_EmptyWhitespaceInput() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("   ")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithText("Invalid URL format").assertIsDisplayed()
    }

    @Test
    fun testTC2_5_DeduplicateParameters() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p?utm_source=a&utm_source=b&id=1&id=2")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEANED_URL_TEXT).assertTextEquals("https://example.com/p?id=1&id=2")
    }

    private fun SemanticsNodeInteraction.fetchText(): String {
        return fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.toString()
            ?: fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.toString()
            ?: ""
    }
}
