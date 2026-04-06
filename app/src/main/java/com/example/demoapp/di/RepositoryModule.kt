package com.example.demoapp.di

import com.example.demoapp.data.repository.TouristPointRepositoryImpl
import com.example.demoapp.data.repository.UserRepositoryImpl
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

}