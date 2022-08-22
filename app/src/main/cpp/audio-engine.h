//
// Created by Emmanuel MacCaull on 2022-08-08.
//

#ifndef HOT_MIC_AUDIO_ENGINE_H
#define HOT_MIC_AUDIO_ENGINE_H

#include <oboe/Oboe.h>

class AudioEngine : oboe::AudioStreamCallback {
public:
    ~AudioEngine();

    void SetRecordingDeviceId(int device_id);

    void StartRecording();

    void StopRecording();

    bool IsRecording() const;

    /// oboe::AudioStreamDataCallback interface implementation
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *oboeStream,
                                          void *audioData, int32_t numFrames) override;

    /// oboe::AudioStreamErrorCallback interface implementation
    void onErrorBeforeClose(oboe::AudioStream *oboeStream, oboe::Result error) override;

    /// oboe::AudioStreamErrorCallback interface implementation
    void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) override;

private:
    int32_t recording_device_ = oboe::kUnspecified;
    bool recording_ = false;
};

#endif //HOT_MIC_AUDIO_ENGINE_H
