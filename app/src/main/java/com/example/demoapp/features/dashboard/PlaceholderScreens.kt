package com.example.demoapp.features.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ReportsPlaceholderScreen() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text("Reportes — próximamente", color = Color(0xFF6B6B6B))
    }
}

@Composable
fun UsersPlaceholderScreen() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text("Usuarios — próximamente", color = Color(0xFF6B6B6B))
    }
}