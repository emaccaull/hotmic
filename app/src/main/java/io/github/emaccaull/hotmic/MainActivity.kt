package io.github.emaccaull.hotmic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.emaccaull.hotmic.databinding.ActivityMainBinding
import io.github.emaccaull.hotmic.level.Dbfs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<MainViewModel>()
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AudioEngine.initialize(this)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        logFeatures()

        binding.audioDeviceSpinner.isEnabled = !AudioEngine.getInstance().isRecording

        binding.recordButton.setOnClickListener {
            val device = binding.audioDeviceSpinner.selectedItem as AudioDevice?
            if (device != null) {
                viewModel.toggleListeningForDevice(device.id)
            }
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

        viewModel.getAudioDevices(AudioSourceFilter.INPUT).observe(this) { devices ->
            adapter.clear()
            adapter.addAll(devices)
        }

        viewModel.viewState.observe(this) { viewState ->
            binding.audioDeviceSpinner.isEnabled = viewState.deviceDropDownEnabled
            binding.recordButton.text = getString(viewState.recordButtonText)

            if (viewState.listening) {
                onStartedListening()
            } else {
                onStoppedListening()
            }
        }
    }

    private fun onStartedListening() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        job =
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.pollMicInput()
                        .onCompletion { binding.peakMeter.level = Dbfs.MIN }
                        .collectLatest { level ->
                            binding.peakMeter.level = level
                        }
                }
            }
    }

    private fun onStoppedListening() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val currentJob = job
        job = null
        currentJob?.cancel()
    }

    override fun onDestroy() {
        AudioEngine.getInstance().close()
        super.onDestroy()
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
