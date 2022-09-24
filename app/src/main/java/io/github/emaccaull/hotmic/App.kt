package io.github.emaccaull.hotmic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            logFeatures()
        }
    }

    private fun logFeatures() {
        AudioProperties(this).let { properties ->
            Timber.d("Has low latency audio (sub 45ms)? %b", properties.hasLowLatencyAudio())
            Timber.d("Has pro audio (sub 20ms)? %b", properties.hasProAudio())
        }
    }
}
