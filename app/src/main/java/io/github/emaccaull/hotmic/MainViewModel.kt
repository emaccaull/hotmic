package io.github.emaccaull.hotmic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val audioDeviceRepository = AudioDeviceRepository(application)

    fun getAudioDevices(audioDeviceType: AudioSourceFilter): LiveData<Set<AudioDevice>> {
        audioDeviceRepository.refresh()
        return audioDeviceRepository.devices.map { devices ->
            devices.filter { d ->
                when (audioDeviceType) {
                    AudioSourceFilter.INPUT -> d.isSource
                    AudioSourceFilter.OUTPUT -> d.isSink
                    AudioSourceFilter.ANY -> true
                }
            }.toSet()
        }
    }

    override fun onCleared() {
        audioDeviceRepository.disconnect()
    }
}
