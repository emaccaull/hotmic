//
// Copyright (c) 2022 Emmanuel MacCaull.
//

#ifndef RINGBUFFER_H_
#define RINGBUFFER_H_

#include <cassert>
#include <cstddef>

template<typename T>
class RingBuffer {
 public:
  RingBuffer(size_t size) : capacity_(size) {
    assert(size > 0);
    buffer_ = new T[capacity_];
  }

  ~RingBuffer() {
    delete[] buffer_;
  }

  size_t Size() const { return size_; }
  size_t Capacity() const { return capacity_; }

  // Insert a new element into the buffer, possibly overwriting the oldest
  // existing value.
  void PushBack(const T& e) {
    buffer_[write_pos_] = e;
    write_pos_ = IncrementIndex(write_pos_);
    if (size_ < capacity_) {
      size_++;
    }
    num_added_++;
  }

  T PopBack() {
    assert(size_ != 0);
    write_pos_ = DecrementIndex(write_pos_);
    T value(buffer_[write_pos_]);
    buffer_[write_pos_] = T(); // Destroy last element (needs testing).
    --num_added_;
    if (num_added_ < size_) {
      size_ = num_added_;
    }
    return value;
  }

  // Returns true if all elements in this ring buffer have a value.
  bool IsFilled() const {
    return size_ == capacity_;
  }

  // Arbitrary access to buffer elements. Behavior is undefined when accessing
  // an element beyond `size`.
  T& operator[](size_t index) {
    return buffer_[index];
  }

  const T& operator[](size_t index) const {
    return buffer_[index];
  }

 private:
  T* buffer_;
  size_t capacity_;
  size_t write_pos_ = 0;
  size_t num_added_ = 0;
  size_t size_ = 0;

  size_t IncrementIndex(size_t index) {
    return (index + 1) % capacity_;
  }

  size_t DecrementIndex(size_t index) {
    if (index == 0) {
      return capacity_ - 1;
    }
    return (index - 1);
  }
};

#endif //RINGBUFFER_H_
