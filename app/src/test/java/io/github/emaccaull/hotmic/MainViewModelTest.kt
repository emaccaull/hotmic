package io.github.emaccaull.hotmic

import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class)
internal class MainViewModelTest {

    private val audioDeviceRepository: AudioDeviceRepository = FakeAudioDeviceRepository(
        AudioDevice(1, "Mic", true),
        AudioDevice(2, "Speaker", false)
    )

    private val viewModel = MainViewModel(audioDeviceRepository)

    @Test
    fun shouldListAudioDevices() {
        // Given
        val observer = DeviceObserver()

        // When
        viewModel.getAudioDevices(AudioSourceFilter.ANY).observeForever(observer)

        // Then
        assertThat(observer.latestDevices, hasItem(AudioDevice(1, "Mic", true)))
        assertThat(observer.latestDevices, hasItem(AudioDevice(2, "Speaker", false)))
    }

    @Test
    fun shouldEmitMicLevelsWhenListening() = runTest {
//        viewModel.startListening()

    }

    class DeviceObserver : Observer<Collection<AudioDevice>> {
        var latestDevices: Collection<AudioDevice>? = null
        override fun onChanged(devices: Collection<AudioDevice>?) {
            latestDevices = devices
        }
    }
}
