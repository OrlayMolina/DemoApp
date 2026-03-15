package com.example.demoapp.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.data.OnboardingFeature
private val CardBg        = Color(0xFFF9F9F9)

@Composable
fun FeatureCard(feature: OnboardingFeature) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                tint = feature.iconTint,
                modifier = Modifier.size(52.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feature.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = feature.description,
                fontSize = 13.sp,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}