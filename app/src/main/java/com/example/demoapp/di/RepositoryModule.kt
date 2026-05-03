package com.example.demoapp.di

import com.example.demoapp.data.repository.AchievementRepositoryImpl
import com.example.demoapp.data.repository.CommentRepositoryImpl
import com.example.demoapp.data.repository.FollowRepositoryImpl
import com.example.demoapp.data.repository.LikeRepositoryImpl
import com.example.demoapp.data.repository.NotificationRepositoryImpl
import com.example.demoapp.data.repository.PostReportRepositoryImpl
import com.example.demoapp.data.repository.ReviewHistoryRepositoryImpl
import com.example.demoapp.data.repository.ProfileRepositoryImpl
import com.example.demoapp.data.repository.TouristPointRepositoryImpl
import com.example.demoapp.data.repository.UserRepositoryImpl
import com.example.demoapp.domain.repository.AchievementRepository
import com.example.demoapp.domain.repository.CommentRepository
import com.example.demoapp.domain.repository.FollowRepository
import com.example.demoapp.domain.repository.LikeRepository
import com.example.demoapp.domain.repository.NotificationRepository
import com.example.demoapp.domain.repository.PostReportRepository
import com.example.demoapp.domain.repository.ProfileRepository
import com.example.demoapp.domain.repository.ReviewHistoryRepository
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
    abstract fun bindReviewHistoryRepository(
        impl: ReviewHistoryRepositoryImpl
    ): ReviewHistoryRepository

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

    @Binds
    @Singleton
    abstract fun bindFollowRepository(
        impl: FollowRepositoryImpl
    ): FollowRepository

    @Binds
    @Singleton
    abstract fun bindPostReportRepository(
        impl: PostReportRepositoryImpl
    ): PostReportRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(
        impl: AchievementRepositoryImpl
    ): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindLikeRepository(
        impl: LikeRepositoryImpl
    ): LikeRepository
}