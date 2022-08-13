cmake_minimum_required(VERSION 3.22.1)
project(HotMic)

add_library(
        audiongn
        # Need a .so to link to JVM at runtime.
        SHARED
        src/main/cpp/audio-engine.h src/main/cpp/audio-engine.cc
        src/main/cpp/jni-bridge.cc
        src/main/cpp/logging_macros.h
)

find_package(oboe REQUIRED CONFIG)

# Audio Engine
include_directories(src/main/cpp/include/)
target_link_libraries(audiongn log oboe::oboe)
