package io.github.emaccaull.hotmic

import androidx.lifecycle.LiveData

interface AudioDeviceRepository {
    /**
     * The list of connected audio devices.
     */
    val devices: LiveData<Collection<AudioDevice>>
}
