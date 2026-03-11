package com.example.demoapp.features.users.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UserDetailScreen(
    userId: String // Recibe el ID del usuario como parámetro
){
    // Un Box es un contenedor simple que ubica a sus hijos uno encima del otro.
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Text(text = "User Detail Screen $userId")
    }
}