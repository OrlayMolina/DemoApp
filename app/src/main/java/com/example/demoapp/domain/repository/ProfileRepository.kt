package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeCurrentUser(): Flow<User?>
    fun observeMyPublications(): Flow<List<TouristPoint>>
    fun observeFollowers(): Flow<Int>
    fun observeFollowing(): Flow<Int>

    fun updateProfile(
        name: String,
        email: String,
        bio: String,
        profilePictureUrl: String? = null
    ): Result<Unit>
    fun deleteCurrentAccount(): Result<Unit>
}

