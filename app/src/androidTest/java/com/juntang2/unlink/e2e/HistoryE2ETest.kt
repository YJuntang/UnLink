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
class HistoryE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
        // Reset settings and clear history first to ensure clean state
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.RESET_SETTINGS_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEAR_HISTORY_BUTTON).performClick()
        composeRule.onNodeWithText("Main").performClick()
    }

    @Test
    fun testTC4_1_HistorySavedOnClean() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/p1?utm_source=history")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("https://example.com/p1").assertIsDisplayed()
    }

    @Test
    fun testTC4_2_HistoryListDisplayed() {
        // Clean multiple
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/a?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://example.com/b?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("https://example.com/a").assertIsDisplayed()
        composeRule.onNodeWithText("https://example.com/b").assertIsDisplayed()
    }

    @Test
    fun testTC4_3_HistoryEmptyState() {
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("No History Found").assertIsDisplayed()
    }

    @Test
    fun testTC4_4_HistorySearchMatches() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/match?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://example.com/other?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithTag(TestTags.SEARCH_BAR).performTextInput("match")
        
        composeRule.onNodeWithText("https://example.com/match").assertIsDisplayed()
        composeRule.onNodeWithText("https://example.com/other").assertDoesNotExist()
    }

    @Test
    fun testTC4_5_HistorySearchNoMatches() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/match?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithTag(TestTags.SEARCH_BAR).performTextInput("nomatch")
        
        composeRule.onNodeWithText("No History Found").assertIsDisplayed()
    }

    @Test
    fun testTC4_6_HistoryFavoriteToggle() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/fav?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("☆").performClick()
        // Should toggle to favorite
        composeRule.onNodeWithText("★").assertIsDisplayed()
        
        composeRule.onNodeWithText("★").performClick()
        composeRule.onNodeWithText("☆").assertIsDisplayed()
    }

    @Test
    fun testTC4_7_HistoryFavoritesFilter() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/fav?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://example.com/notfav?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        // Star the fav item
        composeRule.onNodeWithText("https://example.com/fav")
            .onAncestors()
            .filterToOne(hasAnyChild(hasText("☆")))
            .performClick()
            
        composeRule.onNodeWithTag(TestTags.FAVORITES_TOGGLE).performClick()
        
        composeRule.onNodeWithText("https://example.com/fav").assertIsDisplayed()
        composeRule.onNodeWithText("https://example.com/notfav").assertDoesNotExist()
    }

    @Test
    fun testTC4_8_HistoryDeleteSingle() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/delete?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("🗑").performClick()
        
        composeRule.onNodeWithText("No History Found").assertIsDisplayed()
    }

    @Test
    fun testTC4_9_HistoryClearAll() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/a?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://example.com/b?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag(TestTags.CLEAR_HISTORY_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("No History Found").assertIsDisplayed()
    }

    @Test
    fun testTC4_10_HistoryDomainSortingOrFiltering() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://google.com/search?q=1&utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://yahoo.com/search?q=2&utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithTag(TestTags.SEARCH_BAR).performTextInput("google.com")
        
        composeRule.onNodeWithText("https://google.com/search?q=1").assertIsDisplayed()
        composeRule.onNodeWithText("https://yahoo.com/search?q=2").assertDoesNotExist()
    }

    @Test
    fun testTC4_11_HistoryMultipleDelete() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/1?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://example.com/2?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onAllNodesWithText("🗑").onFirst().performClick()
        composeRule.onAllNodesWithText("🗑").onFirst().performClick()
        
        composeRule.onNodeWithText("No History Found").assertIsDisplayed()
    }

    @Test
    fun testTC4_12_HistoryDuplicateEntries() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/dup?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        // duplicate cleans will result in two history entries
        composeRule.onAllNodesWithText("https://example.com/dup").assertCountEquals(2)
    }

    @Test
    fun testTC4_13_HistoryFavoriteFilterUpdate() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/fav?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("☆").performClick()
        composeRule.onNodeWithTag(TestTags.FAVORITES_TOGGLE).performClick()
        
        composeRule.onNodeWithText("https://example.com/fav").assertIsDisplayed()
        
        // Unstar it while favorites only is selected
        composeRule.onNodeWithText("★").performClick()
        // Should immediately disappear
        composeRule.onNodeWithText("https://example.com/fav").assertDoesNotExist()
    }

    @Test
    fun testTC4_14_HistorySearchAndFavoriteCombined() {
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextInput("https://example.com/fav_match?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://example.com/fav_other?utm_source=h")
        composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        
        composeRule.onNodeWithText("History").performClick()
        
        // Star the fav_match
        composeRule.onNodeWithText("https://example.com/fav_match")
            .onAncestors()
            .filterToOne(hasAnyChild(hasText("☆")))
            .performClick()
            
        composeRule.onNodeWithTag(TestTags.FAVORITES_TOGGLE).performClick()
        composeRule.onNodeWithTag(TestTags.SEARCH_BAR).performTextInput("match")
        
        composeRule.onNodeWithText("https://example.com/fav_match").assertIsDisplayed()
        composeRule.onNodeWithText("https://example.com/fav_other").assertDoesNotExist()
    }

    @Test
    fun testTC4_15_HistoryPerformanceWithLargeSet() {
        // We will generate 30 elements
        for (i in 1..30) {
            composeRule.onNodeWithTag(TestTags.URL_INPUT).performTextReplacement("https://example.com/item$i?utm_source=h")
            composeRule.onNodeWithTag(TestTags.CLEAN_BUTTON).performClick()
        }
        
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithTag(TestTags.SEARCH_BAR).performTextInput("item30")
        composeRule.onNodeWithText("https://example.com/item30").assertIsDisplayed()
    }
}
