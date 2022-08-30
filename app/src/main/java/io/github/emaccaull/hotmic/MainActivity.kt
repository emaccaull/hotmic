package io.github.emaccaull.hotmic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.github.emaccaull.hotmic.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AudioEngine.initialize(this)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        logFeatures()

        binding.audioDeviceSpinner.isEnabled = !AudioEngine.getInstance().isRecording

        binding.recordButton.setOnClickListener {
            if (AudioEngine.getInstance().isRecording) {
                AudioEngine.getInstance().stopRecording()
            } else {
                AudioEngine.getInstance().startRecording()
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

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getAudioDevices(AudioSourceFilter.INPUT).observe(this) { devices ->
            adapter.clear()
            adapter.addAll(devices)
        }
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
