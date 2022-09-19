#include "audio-engine.h"
#include "logging_macros.h"
#include <oboe/Oboe.h>
#include <jni.h>
#include <android/log.h>

namespace {
AudioEngine* engine = nullptr;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_setup(JNIEnv*, jobject) {
  if (engine == nullptr) {
    engine = new AudioEngine();
  }
  LOGI("AudioEngine started");
  return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_startRecording(JNIEnv*,
                                                           jobject,
                                                           jint deviceId) {
  if (engine == nullptr) {
    LOGE(
        "Engine is null, you must call createEngine before calling this "
        "method");
    return JNI_FALSE;
  }
  return engine->StartRecording(deviceId);
}

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_stopRecording(JNIEnv*,
                                                          jobject) {
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
Java_io_github_emaccaull_hotmic_AudioEngine_isRecording(JNIEnv*, jobject) {
  if (engine == nullptr) {
    LOGE(
        "Engine is null, you must call createEngine before calling this "
        "method");
    return JNI_FALSE;
  }
  return engine->IsRecording();
}

JNIEXPORT jfloat JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_currentMicLevel(JNIEnv*, jobject) {
  if (engine == nullptr) {
    LOGE(
        "Engine is null, you must call createEngine before calling this "
        "method");
    return 0;
  }
  return engine->CurrentMicDbFS();
}

JNIEXPORT jboolean JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_shutdown(JNIEnv*, jobject) {
  delete engine;
  engine = nullptr;
  LOGI("AudioEngine stopped");
  return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_io_github_emaccaull_hotmic_AudioEngine_setDefaultStreamParameters(JNIEnv*,
                                                                       jclass,
                                                                       jint sampleRate,
                                                                       jint framesPerBurst) {
  oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
  oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

}
