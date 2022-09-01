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
class AudioDeviceRepository(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _devices = MutableLiveData<Set<AudioDevice>>()
    val devices: LiveData<Set<AudioDevice>> = _devices

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

    suspend fun refresh() = withContext(dispatcher) {
        disconnect()
        connect()
    }

    fun connect() {
        if (connected) {
            throw IllegalStateException("Already connected")
        }
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        connected = true
    }

    fun disconnect() {
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
                    info.isSink
                )
            }
}
