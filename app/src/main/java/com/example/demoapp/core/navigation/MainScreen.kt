package com.example.demoapp.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.features.create.CreatePointViewModel
import com.example.demoapp.features.detail.TouristPointDetailScreen
import com.example.demoapp.features.explore.ExploreScreen
import com.example.demoapp.features.map.MapPointsScreen
import com.example.demoapp.features.notifications.NotificationsScreen
import com.example.demoapp.features.profile.AchievementScreen
import com.example.demoapp.features.profile.EditProfileScreen
import com.example.demoapp.features.profile.ProfileScreen
import com.example.demoapp.features.profile.StatisticsScreen
import com.example.demoapp.features.publish.CreatePointStep1Screen
import com.example.demoapp.features.publish.CreatePointStep2Screen

@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {}

) {
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var showStatistics   by remember { mutableStateOf(false) }
    var showMap          by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showEditProfile  by remember { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<TouristPoint?>(null) }
    var pointToEdit  by remember { mutableStateOf<TouristPoint?>(null) }

    // --- NUEVAS VARIABLES PARA EL FLUJO DE PASOS ---
    var currentPublishStep by remember { mutableStateOf(1) }
    val createViewModel: CreatePointViewModel = hiltViewModel()
    val publishedPoints by createViewModel.touristPoints.collectAsState()
    // -----------------------------------------------

    LaunchedEffect(pointToEdit?.id) {
        pointToEdit?.let { point ->
            createViewModel.startEditing(point)
            currentPublishStep = 1
        }
    }

    if (selectedPoint != null) {
        TouristPointDetailScreen(
            point          = selectedPoint!!,
            isModerator    = false,
            onNavigateBack = { selectedPoint = null }
        )
        return
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab   = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    pointToEdit      = null
                    showMap          = false
                    showAchievements = false
                    showStatistics   = false
                    showEditProfile  = false

                    // Resetear el flujo de creación al cambiar de pestaña
                    if (it != BottomNavTab.PUBLISH) {
                        currentPublishStep = 1
                        createViewModel.reset()
                    }
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
                            points = publishedPoints,
                            onNavigateBack = { showMap = false }
                        )
                    } else {
                        ExploreScreen(
                            points = publishedPoints,
                            onOpenMap = { showMap = true },
                            onOpenDetail = { point -> selectedPoint = point }
                        )
                    }
                }

                BottomNavTab.PUBLISH -> {
                    // FLUJO DE CREACIÓN/EDICIÓN DE 2 PASOS
                    if (currentPublishStep == 1) {
                        CreatePointStep1Screen(
                            photoUrl = createViewModel.selectedPhotoUrls.firstOrNull(),
                            title = createViewModel.title.value,
                            category = createViewModel.selectedCategory,
                            description = createViewModel.description.value,
                            isEditing = pointToEdit != null,
                            onPhotoUrl = { url ->
                                createViewModel.selectedPhotoUrls.clear()
                                createViewModel.addPhoto(url)
                            },
                            onTitle = { createViewModel.title.onChange(it) },
                            onCategory = { categoryEnum ->
                                createViewModel.onCategoryChange(categoryEnum)
                            },
                            onDescription = { createViewModel.description.onChange(it) },
                            onNext = {
                                if (createViewModel.validateStep1()) {
                                    currentPublishStep = 2
                                }
                            },
                            onCancel = {
                                selectedTab = BottomNavTab.HOME
                                pointToEdit = null
                                createViewModel.reset()
                            }
                        )
                    } else {
                        // Paso 2: Mapa y Publicación
                        CreatePointStep2Screen(
                            latitude    = createViewModel.latitudeInput,
                            longitude   = createViewModel.longitudeInput,
                            address     = createViewModel.address,
                            isEditing   = pointToEdit != null,
                            onLatitude  = { createViewModel.onLatitudeChange(it) },
                            onLongitude = { createViewModel.onLongitudeChange(it) },
                            onAddress   = { createViewModel.onAddressChange(it) },
                            onBack      = { currentPublishStep = 1 },
                            onPublish   = {
                                val success = createViewModel.submitPoint()
                                if (success) {
                                    selectedTab = BottomNavTab.HOME
                                    pointToEdit = null
                                    currentPublishStep = 1
                                    createViewModel.reset()
                                }
                                success
                            },
                            onSaveDraft = {
                                selectedTab = BottomNavTab.HOME
                                pointToEdit = null
                                currentPublishStep = 1
                                createViewModel.reset()
                            }
                        )
                    }
                }

                BottomNavTab.NOTIFICATIONS -> NotificationsScreen()
                BottomNavTab.PROFILE -> {
                    when {
                        showAchievements -> AchievementScreen(
                            onNavigateBack = { showAchievements = false }
                        )
                        showStatistics -> StatisticsScreen(
                            publications   = publishedPoints,
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
                            onNavigateToAchievements = { showAchievements = true },
                            onNavigateToStatistics   = { showStatistics   = true },
                            onNavigateToSettings     = { showEditProfile  = true },
                            onOpenPublication        = { point -> selectedPoint = point },
                            onEditPublication        = {
                                    point ->
                                pointToEdit  = point
                                selectedTab  = BottomNavTab.PUBLISH
                            }
                        )
                    }
                }
            }
        }
    }
}