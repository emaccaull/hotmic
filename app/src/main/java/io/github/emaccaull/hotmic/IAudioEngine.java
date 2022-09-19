package io.github.emaccaull.hotmic;

public interface IAudioEngine extends AutoCloseable {
    @Override void close();

    boolean startRecording();

    boolean stopRecording();

    boolean isRecording();

    float currentMicLevel();

    boolean setRecordingDeviceId(int recordingDeviceId);
}
