package io.github.emaccaull.hotmic

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AudioEngine.initialize(this)
    }
}
