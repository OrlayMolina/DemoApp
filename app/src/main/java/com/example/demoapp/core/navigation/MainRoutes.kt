package com.example.demoapp.core.navigation

import kotlinx.serialization.Serializable

sealed class MainRoutes {

    @Serializable
    data object Home : MainRoutes()

    @Serializable object CreateStep1
    @Serializable object CreateStep2
    @Serializable
    data object Login : MainRoutes()

    @Serializable
    data object Register : MainRoutes()

    @Serializable
    data object UserList : MainRoutes()

    @Serializable
    data class UserDetail(val userId: String) : MainRoutes()

    @Serializable object Explore : MainRoutes()

    @Serializable
    object PasswordRecovery : MainRoutes()

    @Serializable
    object Profile : MainRoutes()

    @Serializable object Main : MainRoutes()

    @Serializable
    data object Moderator : MainRoutes()
}