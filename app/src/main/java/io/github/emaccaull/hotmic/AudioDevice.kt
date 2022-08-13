package io.github.emaccaull.hotmic

/**
 * An audio device that can be used for input or output of audio data.
 */
data class AudioDevice(val id: Int, val name: String, val isSource: Boolean)
