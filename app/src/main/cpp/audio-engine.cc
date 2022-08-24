//
// Created by Emmanuel MacCaull on 2022-08-08.
//

#include "audio-engine.h"
#include "logging_macros.h"
#include <oboe/Oboe.h>

AudioEngine::~AudioEngine() {
    if (recording_) {
        StopRecording();
    }
}

void AudioEngine::SetRecordingDeviceId(int device_id) {
    recording_device_ = device_id;
}

bool AudioEngine::StartRecording() {
    recording_ = true;
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
    return input_stream_->requestStart() == oboe::Result::OK;
}

void AudioEngine::StopRecording() {
    recording_ = false;
    CloseStream(input_stream_);
}

bool AudioEngine::IsRecording() const {
    return recording_;
}

/**
 * Sets the stream parameters which are specific to recording,
 * including the sample rate which is determined from the
 * playback stream.
 *
 * @param builder The recording stream builder
 * @param sampleRate The desired sample rate of the recording stream
 */
oboe::AudioStreamBuilder *
AudioEngine::SetupRecordingStreamParameters(oboe::AudioStreamBuilder *builder) const {
    // This sample uses blocking read() because we don't specify a callback
    builder->setDeviceId(recording_device_)
            ->setDirection(oboe::Direction::Input)
            ->setSampleRate(kPreferredSampleRateHz)
            ->setChannelCount(input_channel_count_);
    return SetupCommonStreamParameters(builder);
}

/**
 * Set the stream parameters which are common to both recording and playback
 * streams.
 * @param builder The playback or recording stream builder
 */
oboe::AudioStreamBuilder *AudioEngine::SetupCommonStreamParameters(
        oboe::AudioStreamBuilder *builder) {
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
 * Close the stream. AudioStream::close() is a blocking call so
 * the application does not need to add synchronization between
 * onAudioReady() function and the thread calling close().
 * [the closing thread is the UI thread in this sample].
 * @param stream the stream to close
 */
void AudioEngine::CloseStream(std::shared_ptr<oboe::AudioStream> &stream) {
    if (stream) {
        oboe::Result result = stream->stop();
        if (result != oboe::Result::OK) {
            LOGW("Error stopping stream: %s", oboe::convertToText(result));
        }
        result = stream->close();
        if (result != oboe::Result::OK) {
            LOGE("Error closing stream: %s", oboe::convertToText(result));
        } else {
            LOGW("Successfully closed streams");
        }
        stream.reset();
    }
}

/**
 * Warn in logcat if non-low latency stream is created
 *
 * @param stream: newly created stream
 */
void AudioEngine::WarnIfNotLowLatency(std::shared_ptr<oboe::AudioStream> &stream) {
    if (stream->getPerformanceMode() != oboe::PerformanceMode::LowLatency) {
        LOGW(
            "Stream is NOT low latency. "
            "Check your requested format, sample rate, and channel count.");
    }
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
