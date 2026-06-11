package com.juntang2.unlink

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for UnLink.
 * Annotated with [HiltAndroidApp] to kickstart Hilt dependency injection.
 */
@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
