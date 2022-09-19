//
// Created by Emmanuel MacCaull on 2022-08-08.
//

#include "audio-engine.h"
#include "logging_macros.h"
#include <oboe/Oboe.h>
#include <cmath>

AudioEngine::~AudioEngine() {
  StopRecording();
}

inline int FramesInMillis(int32_t sample_rate, long milliseconds) {
  // E.g., 44100 frame per second = 44.1 frame per milli:
  // 44.1 * 150 = 6,615
  return int(double(sample_rate) / 1000.0 * milliseconds);
}

bool AudioEngine::StartRecording(int32_t recording_device_id) {
  if (recording_) {
    if (recording_device_ == recording_device_id) {
      LOGW("Already recording from %d", recording_device_id);
      return true;
    } else {
      StopRecording();
    }
  }
  recording_device_ = recording_device_id;
  // Open the stream and start recording.
  oboe::AudioStreamBuilder in_builder;
  SetupRecordingStreamParameters(&in_builder, recording_device_id);
  oboe::Result result = in_builder.openStream(input_stream_);
  if (result != oboe::Result::OK) {
    LOGE("Failed to open input stream. Error %s", oboe::convertToText(result));
    return false;
  }
  WarnIfNotLowLatency(input_stream_);
  recording_ = input_stream_->requestStart() == oboe::Result::OK;
  if (recording_) {
    frame_buffer_ = new RingBuffer<double>(FramesInMillis(input_stream_->getSampleRate(), 300));
  }
  return recording_;
}

void AudioEngine::StopRecording() {
  if (recording_) {
    recording_ = false;
    delete frame_buffer_;
    frame_buffer_ = nullptr;
    CloseStream(input_stream_);
  }
}

bool AudioEngine::IsRecording() const {
  return recording_;
}

float AudioEngine::CurrentMicDbFS() {
  float amplitude = 0;
  if (frame_buffer_) {
    // Calculate Root Mean Squared (RMS) of audio signal.
    // https://en.wikipedia.org/wiki/DBFS#RMS_levels
    //
    // NOTE: this reading the buffer runs on a different thread than writing the buffer. Access is
    // not currently thread-safe.
    double sum_squares = 0;
    size_t len = frame_buffer_->Size();
    for (size_t i = 0; i < len; ++i) {
      sum_squares += (*frame_buffer_)[i];
    }
    amplitude = float(::sqrt(sum_squares / len));
  }
  return AmplitudeToDbFS(amplitude);
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
                                            int32_t recording_device,
                                            int32_t sample_rate) {
  builder->setDataCallback(this)
      ->setErrorCallback(this)
      ->setDeviceId(recording_device)
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
  assert(frame_buffer_ != nullptr);
  auto* data = reinterpret_cast<float*>(audioData);
  auto* end = data + numFrames;
  for (auto* p = data; p < end; p++) {
    double value = *p;
    frame_buffer_->PushBack(value * value);
  }
  return oboe::DataCallbackResult::Continue;
}

void AudioEngine::onErrorBeforeClose(oboe::AudioStream* oboeStream, oboe::Result error) {
  AudioStreamErrorCallback::onErrorBeforeClose(oboeStream, error);
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream* oboeStream, oboe::Result error) {
  AudioStreamErrorCallback::onErrorAfterClose(oboeStream, error);
}
