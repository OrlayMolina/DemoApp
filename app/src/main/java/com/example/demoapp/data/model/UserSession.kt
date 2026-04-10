package com.example.demoapp.data.model

import com.example.demoapp.domain.model.UserRole

data class UserSession(
    val userId: String,
    val role: UserRole
)

