package com.example.demoapp.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingFeature(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val description: String
)