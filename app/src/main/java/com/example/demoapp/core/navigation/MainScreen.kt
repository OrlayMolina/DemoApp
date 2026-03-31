package com.example.demoapp.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.features.explore.ExploreScreen
import com.example.demoapp.features.map.MapPointsScreen
import com.example.demoapp.features.notifications.NotificationsScreen   // crea esta pantalla
import com.example.demoapp.features.profile.AchievementScreen
import com.example.demoapp.features.profile.EditProfileScreen
import com.example.demoapp.features.profile.ProfileScreen
import com.example.demoapp.features.profile.StatisticsScreen
import com.example.demoapp.features.publish.PublishScreen               // crea esta pantalla

@Composable
fun MainScreen(
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var showStatistics   by remember { mutableStateOf(false) }
    var showMap          by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showEditProfile  by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab   = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    showMap          = false
                    showAchievements = false
                    showStatistics   = false
                    showEditProfile  = false
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                BottomNavTab.HOME -> {
                    if (showMap) {
                        MapPointsScreen(
                            onNavigateBack = { showMap = false }
                        )
                    } else {
                        ExploreScreen(
                            onOpenMap = { showMap = true }
                        )
                    }
                }
                BottomNavTab.PUBLISH       -> PublishScreen(onCancel = { selectedTab = BottomNavTab.HOME })
                BottomNavTab.NOTIFICATIONS -> NotificationsScreen(initialNotifications = Notification.SAMPLE_LIST)
                BottomNavTab.PROFILE -> {
                    when {
                        showAchievements -> AchievementScreen(
                            onNavigateBack = { showAchievements = false }
                        )
                        showStatistics -> StatisticsScreen(
                            publications   = TouristPoint.SAMPLE_LIST,
                            onNavigateBack = { showStatistics = false }
                        )
                        showEditProfile -> EditProfileScreen(
                            onNavigateBack   = { showEditProfile = false },
                            onAccountDeleted = {
                                showEditProfile = false
                                onLogout()
                            }
                        )
                        else -> ProfileScreen(
                            myPublications           = TouristPoint.SAMPLE_LIST,
                            onNavigateToAchievements = { showAchievements = true },
                            onNavigateToStatistics   = { showStatistics   = true },
                            onNavigateToSettings     = { showEditProfile  = true }
                        )
                    }
                }
            }
        }
    }
}