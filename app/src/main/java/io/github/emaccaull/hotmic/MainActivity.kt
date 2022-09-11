package io.github.emaccaull.hotmic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.emaccaull.hotmic.databinding.ActivityMainBinding
import io.github.emaccaull.hotmic.level.Dbfs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
            if (AudioEngine.getInstance().isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
            val recording = AudioEngine.getInstance().isRecording
            binding.recordButton.text =
                if (recording) {
                    getString(R.string.record_stop)
                } else {
                    getString(R.string.record_start)
                }
            binding.audioDeviceSpinner.isEnabled = !recording
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
        binding.audioDeviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val device = binding.audioDeviceSpinner.selectedItem as AudioDevice
                Timber.d("Selected audio input %s", device.name)
                AudioEngine.getInstance().setRecordingDeviceId(device.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nothing to do
            }
        }

        viewModel.getAudioDevices(AudioSourceFilter.INPUT).observe(this) { devices ->
            adapter.clear()
            adapter.addAll(devices)
        }
    }

    private fun startRecording() {
        job =
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    micLevels().collectLatest { level ->
                        binding.peakMeter.level = level
                    }
                }
            }
        if (AudioEngine.getInstance().startRecording()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun stopRecording() {
        job?.cancel()
        binding.peakMeter.level = Dbfs.MIN
        // TODO: pause recording when app is paused.
        if (AudioEngine.getInstance().stopRecording()) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun micLevels(): Flow<Float> {
        return flow {
            while (currentCoroutineContext().isActive) {
                emit(AudioEngine.getInstance().currentMicLevel())
                delay(33) // approx 30 Hz
            }
        }.flowOn(Dispatchers.IO)
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
