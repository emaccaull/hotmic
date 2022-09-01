//
// Created by Emmanuel MacCaull on 2022-08-08.
//

#include "audio-engine.h"
#include "logging_macros.h"
#include <oboe/Oboe.h>
#include <cmath>

AudioEngine::AudioEngine() {
  if (!current_mic_level_.is_lock_free()) {
    LOGW("std::atomic is not lock free");
  }
}

AudioEngine::~AudioEngine() {
  if (recording_) {
    StopRecording();
  }
}

void AudioEngine::SetRecordingDeviceId(int device_id) {
  recording_device_ = device_id;
}

bool AudioEngine::StartRecording() {
  if (recording_) {
    LOGW("Already recording");
    return true;
  }
  // Open the stream and start recording.
  oboe::AudioStreamBuilder in_builder;
  SetupRecordingStreamParameters(&in_builder);
  oboe::Result result = in_builder.openStream(input_stream_);
  if (result != oboe::Result::OK) {
    LOGE("Failed to open input stream. Error %s", oboe::convertToText(result));
    return false;
  }
  WarnIfNotLowLatency(input_stream_);
//    // Determine maximum size that could possibly be called.
//    int32_t bufferSize = input_stream_->getBufferCapacityInFrames()
//                         * input_stream_->getChannelCount();
  return (recording_ = input_stream_->requestStart() == oboe::Result::OK);
}

void AudioEngine::StopRecording() {
  if (recording_) {
    recording_ = false;
    current_mic_level_ = 0;
    CloseStream(input_stream_);
  }
}

bool AudioEngine::IsRecording() const {
  return recording_;
}

float AudioEngine::BlockingGetNextMicDbFS() {
  float raw = recording_ ? mic_levels_.Poll() : 0.0f;
  return AmplitudeToDbFS(raw);
}

float AudioEngine::CurrentMicDbFS() {
  return AmplitudeToDbFS(current_mic_level_);
}

float AudioEngine::AmplitudeToDbFS(float amplitude) {
  if (amplitude == 0.0f) { // avoid -infinity.
    return -114.0f; // digital noise floor.
  }
  return 20.0f * ::log10(::fabsf(amplitude));
}

/**
 * Sets the stream parameters which are specific to recording.
 *
 * @param builder The recording stream builder
 * @param sampleRate The desired sample rate of the recording stream
 */
oboe::AudioStreamBuilder*
AudioEngine::SetupRecordingStreamParameters(oboe::AudioStreamBuilder* builder,
                                            int32_t sample_rate) {
  // This sample uses blocking read() because we don't specify a callback
  builder->setDataCallback(this)
      ->setErrorCallback(this)
      ->setDeviceId(recording_device_)
      ->setDirection(oboe::Direction::Input)
      ->setSampleRate(sample_rate)
      ->setChannelCount(input_channel_count_);
  return SetupCommonStreamParameters(builder);
}

/**
 * Set the stream parameters which are common to both recording and playback streams.
 *
 * @param builder The playback or recording stream builder
 */
oboe::AudioStreamBuilder*
AudioEngine::SetupCommonStreamParameters(oboe::AudioStreamBuilder* builder) {
  // We request EXCLUSIVE mode since this will give us the lowest possible
  // latency.
  // If EXCLUSIVE mode isn't available the builder will fall back to SHARED
  // mode.
  builder->setFormat(kAudioFormat)
      ->setFormatConversionAllowed(true)
      ->setSharingMode(oboe::SharingMode::Exclusive)
      ->setPerformanceMode(oboe::PerformanceMode::LowLatency);
  return builder;
}

/**
 * Close the stream. AudioStream::close() is a blocking call so the application does not need to add
 * synchronization between onAudioReady() function and the thread calling close().
 *
 * @param stream the stream to close
 */
void AudioEngine::CloseStream(std::shared_ptr<oboe::AudioStream>& stream) {
  if (stream) {
    oboe::Result result = stream->stop();
    if (result != oboe::Result::OK) {
      LOGW("Error stopping stream: %s", oboe::convertToText(result));
    }
    result = stream->close();
    if (result != oboe::Result::OK) {
      LOGE("Error closing stream: %s", oboe::convertToText(result));
    } else {
      LOGI("Successfully closed streams");
    }
    stream.reset();
  }
}

/**
 * Warn in logcat if non-low latency stream is created
 *
 * @param stream: newly created stream
 */
void AudioEngine::WarnIfNotLowLatency(std::shared_ptr<oboe::AudioStream>& stream) {
  if (stream->getPerformanceMode() != oboe::PerformanceMode::LowLatency) {
    LOGW(
        "Stream is NOT low latency. "
        "Check your requested format, sample rate, and channel count.");
  }
}

oboe::DataCallbackResult
AudioEngine::onAudioReady(oboe::AudioStream*, void* audioData, int32_t numFrames) {
  auto* data = reinterpret_cast<float*>(audioData);
  auto* end = data + numFrames;
  double avg = 0;
  for (auto* p = data; p < end; p++) {
    avg += *p;
  }
  avg /= numFrames;

  auto value = float(avg);
  mic_levels_.Push(value);
  current_mic_level_ = value;

  return oboe::DataCallbackResult::Continue;
}

void AudioEngine::onErrorBeforeClose(oboe::AudioStream* oboeStream, oboe::Result error) {
  AudioStreamErrorCallback::onErrorBeforeClose(oboeStream, error);
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream* oboeStream, oboe::Result error) {
  AudioStreamErrorCallback::onErrorAfterClose(oboeStream, error);
}
