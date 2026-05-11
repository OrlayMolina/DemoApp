package com.example.demoapp.di

import android.content.Context
import com.example.demoapp.core.utils.ImageLabeler
import com.example.demoapp.data.repository.AiRepositoryImpl
import com.example.demoapp.domain.repository.AiRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    companion object {
        @Provides
        @Singleton
        fun provideImageLabeler(@ApplicationContext context: Context): ImageLabeler =
            ImageLabeler(context)
    }
}
