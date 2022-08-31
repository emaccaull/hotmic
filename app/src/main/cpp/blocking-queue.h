//
// Created by Emmanuel MacCaull on 2022-08-31.
//

#ifndef HOT_MIC_BLOCKING_QUEUE_H
#define HOT_MIC_BLOCKING_QUEUE_H

#include <condition_variable>
#include <deque>
#include <mutex>

/**
 * Simple thread-safe queue for producer/consumer communication.
 */
template<typename T>
class BlockingQueue {
  // Implementation from https://stackoverflow.com/a/12805690/58100
 private:
  std::mutex mutex_;
  std::condition_variable condition_;
  std::deque<T> deque_;
  size_t max_queue_size_;

 public:
  BlockingQueue(size_t size) : max_queue_size_(size) {}

  void Push(const T& value) {
    {
      std::unique_lock<std::mutex> lock(mutex_);
      deque_.push_front(value);
      if (deque_.size() > max_queue_size_) {
        deque_.pop_back();
      }
    }
    condition_.notify_one();
  }

  T Poll() {
    std::unique_lock<std::mutex> lock(mutex_);
    condition_.wait(lock, [=]{ return !deque_.empty(); });
    T value(std::move(deque_.back()));
    deque_.pop_back();
    return value;
  }
};


#endif //HOT_MIC_BLOCKING_QUEUE_H
