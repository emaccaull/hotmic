package io.github.emaccaull.hotmic

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class)
internal class MainViewModelTest {

    private val audioDeviceRepository: AudioDeviceRepository = FakeAudioDeviceRepository(
        AudioDevice(1, "Mic", true),
        AudioDevice(2, "Speaker", false)
    )

    private val audioEngine: IAudioEngine = FakeAudioEngine(listOf(0.3f, 0.2f))

    private val viewModel = MainViewModel(audioDeviceRepository, audioEngine)

    @Test
    fun shouldListAudioDevices() {
        // Given
        val observer = ItemObserver<Collection<AudioDevice>>()

        // When
        viewModel.getAudioDevices(AudioSourceFilter.ANY).observeForever(observer)

        // Then
        assertThat(observer.latest, hasItem(AudioDevice(1, "Mic", true)))
        assertThat(observer.latest, hasItem(AudioDevice(2, "Speaker", false)))
    }

    @Test
    fun shouldEmitMicLevelsWhenListening() = runTest {
        val observer = ItemObserver<ViewState>()
        viewModel.viewState.observeForever(observer)

        viewModel.startListening(1)

        val inputLevel = viewModel.pollMicInput().take(1).last()
        assertThat(inputLevel, `is`(0.3f))

        assertThat(observer.latest?.listening, `is`(true))
    }
}
