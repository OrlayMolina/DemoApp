package com.example.demoapp.di

import android.content.Context
import com.example.demoapp.core.utils.ResourceProvider
import com.example.demoapp.core.utils.ResourceProviderImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideResourceProvider(
        @ApplicationContext context: Context
    ): ResourceProvider = ResourceProviderImpl(context)
}