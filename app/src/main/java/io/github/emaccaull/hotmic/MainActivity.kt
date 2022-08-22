package io.github.emaccaull.hotmic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.github.emaccaull.hotmic.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: MainViewModel
    private var fakeRecordState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        logFeatures()

        binding.recordButton.setOnClickListener {
            fakeRecordState = !fakeRecordState
            binding.recordButton.text =
                if (fakeRecordState) getString(R.string.record_stop) else getString(R.string.record_start)
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO(emmanuel): should check rationale.
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        }

        val adapter = AudioDeviceArrayAdapter(this)
        binding.audioDeviceSpinner.adapter = adapter

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getAudioDevices(AudioSourceFilter.INPUT).observe(this) { devices ->
            adapter.clear()
            adapter.addAll(devices)
        }
    }

    override fun onResume() {
        super.onResume()
        AudioEngine.getInstance().start()
    }

    override fun onPause() {
        AudioEngine.getInstance().shutdown()
        super.onPause()
    }

    private fun logFeatures() {
        val hasLowLatencyFeature: Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)
        val hasProFeature: Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)
        Timber.d("Has low latency audio (sub 45ms)? %b", hasLowLatencyFeature)
        Timber.d("Has pro audio (sub 20ms)? %b", hasProFeature)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 101
    }
}
