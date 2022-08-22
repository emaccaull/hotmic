//
// Created by Emmanuel MacCaull on 2022-08-08.
//

#include "audio-engine.h"
#include <oboe/Oboe.h>

AudioEngine::~AudioEngine() {
    if (recording_) {
        StopRecording();
    }
}

void AudioEngine::SetRecordingDeviceId(int device_id) {
    recording_device_ = device_id;
}

void AudioEngine::StartRecording() {
    recording_ = true;
}

void AudioEngine::StopRecording() {
    recording_ = false;
}

bool AudioEngine::IsRecording() const {
    return recording_;
}

oboe::DataCallbackResult
AudioEngine::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) {
    return oboe::DataCallbackResult::Continue;
}

void AudioEngine::onErrorBeforeClose(oboe::AudioStream *oboeStream, oboe::Result error) {
    AudioStreamErrorCallback::onErrorBeforeClose(oboeStream, error);
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) {
    AudioStreamErrorCallback::onErrorAfterClose(oboeStream, error);
}
