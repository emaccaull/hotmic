#include "audio-engine.h"
#include "logging_macros.h"
#include <oboe/Oboe.h>
#include <jni.h>
#include <android/log.h>

namespace {
    AudioEngine *engine = nullptr;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_setup(JNIEnv *env, jobject) {
    if (engine == nullptr) {
        engine = new AudioEngine();
    }
    LOGI("AudioEngine started");
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_startRecording(JNIEnv *env, jobject) {
    if (engine == nullptr) {
        LOGE(
            "Engine is null, you must call createEngine before calling this "
            "method");
        return JNI_FALSE;
    }
    return engine->StartRecording();
}

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_stopRecording(JNIEnv *env, jobject) {
    if (engine == nullptr) {
        LOGE(
            "Engine is null, you must call createEngine before calling this "
            "method");
        return JNI_FALSE;
    }
    engine->StopRecording();
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_isRecording(JNIEnv *env, jobject) {
    if (engine == nullptr) {
        LOGE(
            "Engine is null, you must call createEngine before calling this "
            "method");
        return JNI_FALSE;
    }
    return engine->IsRecording();
}

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_shutdown(JNIEnv *env, jobject) {
    delete engine;
    engine = nullptr;
    LOGI("AudioEngine stopped");
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_setRecordingDeviceId(JNIEnv *env, jobject,
                                                                 int device_id) {
    if (engine == nullptr) {
        LOGE(
            "Engine is null, you must call createEngine before calling this "
            "method");
        return JNI_FALSE;
    }
    engine->SetRecordingDeviceId(device_id);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_setDefaultStreamParameters(JNIEnv *env,
                                                                       jclass type,
                                                                       jint sampleRate,
                                                                       jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

}
