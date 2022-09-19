//
// Created by Emmanuel MacCaull on 2022-08-08.
//

#ifndef HOT_MIC_AUDIO_ENGINE_H
#define HOT_MIC_AUDIO_ENGINE_H

#include "ringbuffer.h"
#include <oboe/Oboe.h>

class AudioEngine: oboe::AudioStreamCallback {
 public:
  ~AudioEngine();

  bool StartRecording(int32_t recording_device_id);

  void StopRecording();

  bool IsRecording() const;

  float CurrentMicDbFS();

  oboe::DataCallbackResult
  onAudioReady(oboe::AudioStream* oboeStream, void* audioData, int32_t numFrames) override;

  void onErrorBeforeClose(oboe::AudioStream* oboeStream, oboe::Result error) override;

  void onErrorAfterClose(oboe::AudioStream* oboeStream, oboe::Result error) override;

 private:
  static constexpr oboe::AudioFormat kAudioFormat = oboe::AudioFormat::Float;
  static constexpr int32_t kPreferredSampleRateHz = 44100;

  /// The last 300 milliseconds of squared mic samples.
  RingBuffer<double>* frame_buffer_ = nullptr;

  /// Record mono. Stereo isn't required for the analysis.
  const int32_t input_channel_count_ = oboe::ChannelCount::Mono;
  int32_t recording_device_ = oboe::kUnspecified;
  bool recording_ = false;

  std::shared_ptr<oboe::AudioStream> input_stream_;

  oboe::AudioStreamBuilder*
  SetupRecordingStreamParameters(oboe::AudioStreamBuilder* builder,
                                 int32_t recording_device,
                                 int32_t sample_rate = kPreferredSampleRateHz);

  static oboe::AudioStreamBuilder*
  SetupCommonStreamParameters(oboe::AudioStreamBuilder* builder);

  static void CloseStream(std::shared_ptr<oboe::AudioStream>& stream);

  static void WarnIfNotLowLatency(std::shared_ptr<oboe::AudioStream>& stream);

  static float AmplitudeToDbFS(float amplitude);
};

#endif //HOT_MIC_AUDIO_ENGINE_H
