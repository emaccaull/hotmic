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
Java_io_github_emaccaull_hotmic_AudioEngine_start(JNIEnv *env, jobject) {
    if (engine == nullptr) {
        engine = new AudioEngine();
    }
    LOGI("AudioEngine started");
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_shutdown(JNIEnv *env, jobject) {
    delete engine;
    engine = nullptr;
    LOGI("AudioEngine stopped");
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
