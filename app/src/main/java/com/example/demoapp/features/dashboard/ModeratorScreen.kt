package com.example.demoapp.features.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.demoapp.core.navigation.ModeratorBottomNavBar
import com.example.demoapp.core.navigation.ModeratorTab
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.features.detail.TouristPointDetailScreen
import com.example.demoapp.features.history.HistoryScreen
import com.example.demoapp.features.review.ReviewQueueScreen

@Composable
fun ModeratorScreen(
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ModeratorTab.DASHBOARD) }
    var selectedPoint  by remember { mutableStateOf<TouristPoint?>(null) }

    if (selectedPoint != null) {
        TouristPointDetailScreen(
            point          = selectedPoint!!,
            isModerator    = true,
            onNavigateBack = { selectedPoint = null },
            onApproved     = { selectedPoint = null },
            onRejected     = { selectedPoint = null }
        )
        return
    }

    Scaffold(
        bottomBar = {
            ModeratorBottomNavBar(
                selectedTab   = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    selectedPoint = null
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedPoint != null) {
                TouristPointDetailScreen(
                    point          = selectedPoint!!,
                    isModerator    = true,
                    onNavigateBack = { selectedPoint = null },
                    onApproved     = { selectedPoint = null },
                    onRejected     = { selectedPoint = null }
                )
            } else {
                when (selectedTab) {
                    ModeratorTab.DASHBOARD -> DashboardScreen(onLogout = onLogout)
                    ModeratorTab.REVIEW    -> ReviewQueueScreen(
                        onNavigateToDetail = { selectedPoint = it }
                    )
                    ModeratorTab.HISTORY   -> HistoryScreen()
                    ModeratorTab.REPORTS   -> ReportsPlaceholderScreen()
                    ModeratorTab.USERS     -> UsersPlaceholderScreen()
                }
            }
        }
    }
}