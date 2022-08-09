package io.github.emaccaull.hotmic

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.emaccaull.hotmic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val hasLowLatencyFeature: Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)
        binding.textLowLatency.text = getString(R.string.audio_feature_low_latency, hasLowLatencyFeature)

        val hasProFeature: Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)
        binding.textProAudio.text = getString(R.string.audio_feature_pro, hasProFeature)
    }
}
