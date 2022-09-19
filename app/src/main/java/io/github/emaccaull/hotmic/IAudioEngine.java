package io.github.emaccaull.hotmic;

public interface IAudioEngine extends AutoCloseable {
    @Override void close();

    /**
     * Start recording on {@code deviceId}. If already recording from a different device, the
     * recording is stopped and then started for {@code deviceId}.
     */
    boolean startRecording(int deviceId);

    boolean stopRecording();

    boolean isRecording();

    float currentMicLevel();
}
