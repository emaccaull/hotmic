package io.github.emaccaull.hotmic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.github.emaccaull.hotmic.databinding.ActivityMainBinding

private const val TAG = "HotMic"

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val hasLowLatencyFeature: Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)
        binding.textLowLatency.text =
            getString(R.string.audio_feature_low_latency, hasLowLatencyFeature)

        val hasProFeature: Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)
        binding.textProAudio.text = getString(R.string.audio_feature_pro, hasProFeature)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
//
//            } else {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
//            }
        }


        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                val audioDevices =
                    addedDevices.map { AudioDevice(it.id, it.friendlyName, it.isSource) }
                Log.i(TAG, "Devices added: $audioDevices")
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
                Log.i(TAG, "Devices removed: $removedDevices")
            }
        }, null)


        AudioEngine.getInstance().start()
    }

    override fun onDestroy() {
        AudioEngine.getInstance().shutdown()
        super.onDestroy()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 101
    }
}
