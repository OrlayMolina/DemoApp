package com.example.demoapp.di

import com.example.demoapp.data.repository.CommentRepositoryImpl
import com.example.demoapp.data.repository.NotificationRepositoryImpl
import com.example.demoapp.data.repository.ProfileRepositoryImpl
import com.example.demoapp.data.repository.TouristPointRepositoryImpl
import com.example.demoapp.data.repository.UserRepositoryImpl
import com.example.demoapp.domain.repository.CommentRepository
import com.example.demoapp.domain.repository.NotificationRepository
import com.example.demoapp.domain.repository.ProfileRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds // Indica a Hilt que esta función vincula una implementación a una interfaz
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository // Vincula UserRepositoryImpl con UserRepository

    @Binds
    @Singleton
    abstract fun bindTouristPointRepository(
        repositoryImpl: TouristPointRepositoryImpl
    ): TouristPointRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        impl: CommentRepositoryImpl
    ): CommentRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository
}