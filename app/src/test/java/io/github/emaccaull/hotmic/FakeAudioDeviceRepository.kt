package io.github.emaccaull.hotmic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FakeAudioDeviceRepository(audioDevices: Collection<AudioDevice>?) : AudioDeviceRepository {
    constructor(vararg audioDevices: AudioDevice) : this(listOf(*audioDevices))
    override val devices: LiveData<Collection<AudioDevice>> = MutableLiveData(audioDevices)
}
