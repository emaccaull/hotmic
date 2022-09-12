package io.github.emaccaull.hotmic

import androidx.lifecycle.Observer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantTaskExecutorExtension::class)
internal class MainViewModelTest {

    private val audioDeviceRepository: AudioDeviceRepository = FakeAudioDeviceRepository(
        AudioDevice(1, "Mic", true),
        AudioDevice(2, "Phone", true)
    )

    @Test
    fun shouldEmitMicLevelsWhenListening() {
        // Given
        val viewModel = MainViewModel(audioDeviceRepository)
        val observer = DeviceObserver()

        // When
        viewModel.getAudioDevices(AudioSourceFilter.ANY).observeForever(observer)

        // Then
        assertThat(observer.latestDevices, hasItem(AudioDevice(1, "Mic", true)))
    }

    class DeviceObserver : Observer<Collection<AudioDevice>> {
        var latestDevices: Collection<AudioDevice>? = null
        override fun onChanged(devices: Collection<AudioDevice>?) {
            latestDevices = devices
        }
    }
}
