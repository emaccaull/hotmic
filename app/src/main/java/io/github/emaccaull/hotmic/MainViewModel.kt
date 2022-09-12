package io.github.emaccaull.hotmic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ViewState(
    val deviceDropDownEnabled: Boolean = true,
    val listening: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(private val audioDeviceRepository: AudioDeviceRepository) :
    ViewModel() {

    private val _viewState = MutableLiveData(ViewState())
    val viewState: LiveData<ViewState> get() = _viewState

    fun getAudioDevices(audioDeviceType: AudioSourceFilter): LiveData<Set<AudioDevice>> {
        return audioDeviceRepository.devices.map { devices ->
            devices.filter { d ->
                when (audioDeviceType) {
                    AudioSourceFilter.INPUT -> d.isSource
                    AudioSourceFilter.OUTPUT -> !d.isSource
                    AudioSourceFilter.ANY -> true
                }
            }.toSet()
        }
    }
}
