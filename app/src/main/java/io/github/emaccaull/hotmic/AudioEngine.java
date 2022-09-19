package io.github.emaccaull.hotmic;

import android.content.Context;
import android.media.AudioManager;

import androidx.annotation.WorkerThread;

public class AudioEngine implements IAudioEngine {
    static {
        System.loadLibrary("audiongn");
    }

    private static volatile AudioEngine INSTANCE;

    private AudioEngine(Context context) {
        AudioManager myAudioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        int defaultSampleRate = Integer.parseInt(sampleRateStr);
        String framesPerBurstStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        int defaultFramesPerBurst = Integer.parseInt(framesPerBurstStr);
        setDefaultStreamParameters(defaultSampleRate, defaultFramesPerBurst);
    }

    public static AudioEngine initialize(Context context) {
        AudioEngine instance = INSTANCE;
        if (instance == null) {
            synchronized (AudioEngine.class) {
                instance = INSTANCE;
                if (instance == null) {
                    INSTANCE = instance = new AudioEngine(context);
                    INSTANCE.setup();
                }
            }
        }
        return instance;
    }

    public static AudioEngine getInstance() {
        AudioEngine instance = INSTANCE;
        if (instance == null) {
            throw new AssertionError("AudioEngine is not initialized");
        }
        return instance;
    }

    @Override
    public void close() {
        synchronized (AudioEngine.class) {
            shutdown();
            INSTANCE = null;
        }
    }

    @Override
    public native boolean startRecording(int deviceId);

    @Override
    public native boolean stopRecording();

    @Override
    public native boolean isRecording();

    /**
     * Read the current level of the mic input in dBFS. Does not block.
     *
     * <p>Some general information about levels is available at
     * <a href="https://ledgernote.com/columns/studio-recording/audio-clipping/">ledgernote.com</a>.
     *
     * @return mic level in dBFS (decibels full scale).
     */
    @Override
    public native float currentMicLevel();

    private native boolean setup();

    private native boolean shutdown();

    private static native void setDefaultStreamParameters(int defaultSampleRate, int defaultFramesPerBurst);
}
