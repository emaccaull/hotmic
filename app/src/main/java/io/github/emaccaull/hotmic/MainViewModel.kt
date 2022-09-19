package io.github.emaccaull.hotmic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.emaccaull.hotmic.di.IODispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

data class ViewState(
    val deviceDropDownEnabled: Boolean = true,
    val listening: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val audioDeviceRepository: AudioDeviceRepository,
    private val audioEngine: IAudioEngine,
    @IODispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

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

    fun startListening(deviceId: Int): Boolean {
        val state = _viewState.value!!
        val listening = audioEngine.startRecording(deviceId)
        _viewState.value = state.copy(deviceDropDownEnabled = !listening, listening = listening)
        return listening
    }

    fun stopListening(): Boolean {
        val state = _viewState.value!!
        val stopped = audioEngine.stopRecording()
        _viewState.value = state.copy(deviceDropDownEnabled = stopped, listening = !stopped)
        return stopped
    }

    fun pollMicInput(): Flow<Float> = flow {
        while (currentCoroutineContext().isActive) {
            emit(audioEngine.currentMicLevel())
            delay(33) // approx 30 Hz
        }
    }.flowOn(dispatcher)
}
