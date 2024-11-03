package com.algan.composedeneme.di
import com.algan.composedeneme.data.remote.VideoService
import com.algan.composedeneme.data.remote.VideoServiceImpl
import com.algan.composedeneme.data.repository.VideoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideVideoService(): VideoService = VideoServiceImpl()

    @Singleton
    @Provides
    fun provideVideoRepository(videoService: VideoService): VideoRepository {
        return VideoRepository(videoService)
    }
}