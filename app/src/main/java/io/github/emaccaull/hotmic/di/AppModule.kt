package io.github.emaccaull.hotmic.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.emaccaull.hotmic.AudioDeviceRepository
import io.github.emaccaull.hotmic.DefaultAudioDeviceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideAudioDeviceRepository(@ApplicationContext context: Context): AudioDeviceRepository {
        return DefaultAudioDeviceRepository(context)
    }
}
