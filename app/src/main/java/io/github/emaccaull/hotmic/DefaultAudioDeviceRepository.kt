package io.github.emaccaull.hotmic

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Keeps track of available audio devices and updates [devices] as new devices are connected or
 * disconnected to the system.
 */
class DefaultAudioDeviceRepository(
    private val context: Context,
) : AudioDeviceRepository {

    private val _devices = object : MutableLiveData<Collection<AudioDevice>>() {
        override fun onActive() {
            super.onActive()
            connect()
        }
        override fun onInactive() {
            super.onInactive()
            disconnect()
        }
    }
    override val devices: LiveData<Collection<AudioDevice>> get() = _devices

    private var connected = false

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
            val current = _devices.value
            val devices = if (current != null) {
                current + addedDevices.conv()
            } else {
                addedDevices.conv()
            }
            _devices.value = devices
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
            val current = _devices.value
            if (current != null) {
                _devices.value = current - removedDevices.conv()
            }
        }
    }

    private fun connect() {
        if (connected) {
            throw IllegalStateException("Already connected")
        }
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        connected = true
    }

    private fun disconnect() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        connected = false
    }

    private fun Array<AudioDeviceInfo>.conv(): Set<AudioDevice> =
        this
            .sortedBy { info ->
                when (info.type) {
                    AudioDeviceInfo.TYPE_BUILTIN_MIC -> -1
                    else -> info.type
                }
            }
            .mapTo(linkedSetOf()) { info ->
                AudioDevice(
                    info.id,
                    info.friendlyName,
                    info.isSource,
                )
            }
}
