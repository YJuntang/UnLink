package com.juntang2.unlink.util

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert.assertEquals

object IntentTestHelper {
    fun verifyShareIntent(expectedText: String) {
        intended(allOf(
            hasAction(Intent.ACTION_SEND),
            hasType("text/plain"),
            hasExtra(Intent.EXTRA_TEXT, expectedText)
        ))
    }

    fun verifyClipboardContent(expectedUrl: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val clipData = clipboard.primaryClip
            val text = clipData?.getItemAt(0)?.text?.toString()
            assertEquals(expectedUrl, text)
        }
    }

    fun clearClipboard() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            clipboard.clearPrimaryClip()
        }
    }
}
