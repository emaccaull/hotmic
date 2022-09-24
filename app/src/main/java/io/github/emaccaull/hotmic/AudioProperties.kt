package io.github.emaccaull.hotmic

import android.content.Context
import android.content.pm.PackageManager

class AudioProperties(private val context: Context) {

    fun hasLowLatencyAudio(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)
    }

    fun hasProAudio(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)
    }
}
