package com.example.demoapp.core.navigation

import kotlinx.serialization.Serializable

sealed class MainRoutes {

    @Serializable
    data object UserList : MainRoutes() // Ruta para la lista de usuarios

    @Serializable
    data class UserDetail(val userId: String) : MainRoutes()
    @Serializable
    data object Home : MainRoutes()

    @Serializable
    data object Login : MainRoutes()

    @Serializable
    data object Register : MainRoutes()

}