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

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<MainViewModel>()
    private var job: Job? = null

    // FIXME: need to attribute the app image asset
    //   In the credits, (say hamburger, then about), need to
    //   - list open source licenses
    //   - state Microphone image assets "designed by Freepik from Flaticon"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AudioEngine.initialize(this)

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
            adapter.apply {
                setNotifyOnChange(false)
                clear()
                addAll(devices)
                notifyDataSetChanged()
            }
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

    companion object {
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 101
    }
}
