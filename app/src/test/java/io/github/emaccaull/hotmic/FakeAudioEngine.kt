package io.github.emaccaull.hotmic

class FakeAudioEngine(val micLevels: List<Float>) : IAudioEngine {

    private var recording = false
    private var micLevelsIndex = 0

    override fun close() {
        recording = false
    }

    override fun startRecording(): Boolean {
        recording = true
        return true
    }

    override fun stopRecording(): Boolean {
        recording = false
        return true
    }

    override fun isRecording(): Boolean {
        return recording
    }

    override fun currentMicLevel(): Float {
        return if (micLevelsIndex >= micLevels.size) 0.0f else micLevels[micLevelsIndex++]
    }

    override fun setRecordingDeviceId(recordingDeviceId: Int): Boolean {
        return true
    }
}
