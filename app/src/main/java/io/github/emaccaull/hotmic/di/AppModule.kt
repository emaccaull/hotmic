package io.github.emaccaull.hotmic.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.emaccaull.hotmic.AudioDeviceRepository
import io.github.emaccaull.hotmic.AudioEngine
import io.github.emaccaull.hotmic.DefaultAudioDeviceRepository
import io.github.emaccaull.hotmic.IAudioEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class IODispatcher

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideAudioDeviceRepository(@ApplicationContext context: Context): AudioDeviceRepository {
        return DefaultAudioDeviceRepository(context)
    }

    @Provides
    @IODispatcher
    fun provideIoDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    @Provides
    fun provideAudioEngine(): IAudioEngine {
        return AudioEngine.getInstance()
    }
}
