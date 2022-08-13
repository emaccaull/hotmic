package io.github.emaccaull.hotmic;

import android.content.Context;
import android.media.AudioManager;

public class AudioEngine {
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

    public native boolean start();

    public native boolean shutdown();

    private static native void setDefaultStreamParameters(int defaultSampleRate, int defaultFramesPerBurst);
}
