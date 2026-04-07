package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.ProfileRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.example.demoapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val touristPointRepository: TouristPointRepository
) : ProfileRepository {

    override fun observeCurrentUser(): Flow<User?> {
        return userRepository.currentUser
    }

    override fun observeMyPublications(): Flow<List<TouristPoint>> {
        return combine(userRepository.currentUser, touristPointRepository.touristPoints) { user, points ->
            val currentUserId = user?.id ?: return@combine emptyList()
            points.filter { point ->
                point.authorId == currentUserId ||
                    point.authorId == "user_$currentUserId"
            }
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

