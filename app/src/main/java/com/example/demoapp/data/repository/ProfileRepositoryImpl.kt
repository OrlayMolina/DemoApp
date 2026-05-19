package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.CommentRepository
import com.example.demoapp.domain.repository.FollowRepository
import com.example.demoapp.domain.repository.ProfileRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.example.demoapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val touristPointRepository: TouristPointRepository,
    private val followRepository: FollowRepository,
    private val commentRepository: CommentRepository
) : ProfileRepository {

    override fun observeCurrentUser(): Flow<User?> {
        return userRepository.currentUser
    }

    override fun observeMyPublications(): Flow<List<TouristPoint>> {
        return combine(
            userRepository.currentUser,
            touristPointRepository.touristPoints,
            commentRepository.observeCommentCounts()
        ) { user, points, counts ->
            val currentUserId = user?.id ?: return@combine emptyList()
            points
                .filter { point ->
                    point.authorId == currentUserId ||
                        point.authorId == "user_$currentUserId"
                }
                .map { point -> point.copy(commentCount = counts[point.id] ?: point.commentCount) }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeFollowers(): Flow<Int> {
        return userRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(0) else followRepository.observeFollowersCount(user.id)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeFollowing(): Flow<Int> {
        return userRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(0) else followRepository.observeFollowingCount(user.id)
        }
    }

    override fun updateProfile(
        name: String,
        email: String,
        bio: String,
        profilePictureUrl: String?
    ): Result<Unit> {
        val user = userRepository.currentUser.value
            ?: return Result.failure(IllegalStateException("No se encontro el usuario actual"))

        val updatedUser = user.copy(
            name = name.trim(),
            email = email.trim(),
            bio = bio.trim(),
            profilePictureUrl = profilePictureUrl?.trim().orEmpty().ifBlank { user.profilePictureUrl }
        )

        return userRepository.update(updatedUser)
    }

    override fun deleteCurrentAccount(): Result<Unit> {
        val user = userRepository.currentUser.value
            ?: return Result.failure(IllegalStateException("No se encontro el usuario actual"))
        return userRepository.delete(user.id)
    }
}

