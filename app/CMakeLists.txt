cmake_minimum_required(VERSION 3.22.1)
project(HotMic)

find_package(oboe REQUIRED CONFIG)

add_library(
    audiolib
    # Need a .so to link to JVM at runtime.
    SHARED
    src/main/cpp/audio-engine.h src/main/cpp/audio-engine.cc
)

target_link_libraries(audiolib oboe OpenSLES)

include_directories(src/main/cpp/include/)
