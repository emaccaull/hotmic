package io.github.emaccaull.hotmic

import androidx.lifecycle.Observer

class ItemObserver<T> : Observer<T> {
    var latest: T? = null
    override fun onChanged(t: T) {
        latest = t
    }
}
