package com.example.demoapp.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.features.create.CreatePointViewModel
import com.example.demoapp.features.detail.TouristPointDetailScreen
import com.example.demoapp.features.explore.ExploreScreen
import com.example.demoapp.features.explore.FeedViewModel
import com.example.demoapp.features.map.MapPointsScreen
import com.example.demoapp.features.notifications.NotificationsScreen
import com.example.demoapp.features.profile.AchievementScreen
import com.example.demoapp.features.profile.EditProfileScreen
import com.example.demoapp.features.profile.FollowListMode
import com.example.demoapp.features.profile.FollowListScreen
import com.example.demoapp.features.profile.ProfileScreen
import com.example.demoapp.features.profile.ProfileViewModel
import com.example.demoapp.features.profile.StatisticsScreen
import com.example.demoapp.features.publish.CreatePointStep1Screen
import com.example.demoapp.features.publish.CreatePointStep2Screen
import com.example.demoapp.features.profile.VisitedProfileScreen

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
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var followListMode by remember { mutableStateOf<FollowListMode?>(null) }

    // --- NUEVAS VARIABLES PARA EL FLUJO DE PASOS ---
    var currentPublishStep by remember { mutableStateOf(1) }
    val createViewModel: CreatePointViewModel = hiltViewModel()
    val context = LocalContext.current
    val publishedPoints by createViewModel.touristPoints.collectAsState()
    val feedViewModel: FeedViewModel = hiltViewModel()
    // El feed solo muestra publicaciones verificadas de usuarios que sigues (o tuyas).
    val verifiedFeedPoints by feedViewModel.feed.collectAsState()
    val likedPostIds by feedViewModel.likedIds.collectAsState()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val myPublications by profileViewModel.myPublications.collectAsState()
    val currentUser by profileViewModel.user.collectAsState()
    // -----------------------------------------------

    LaunchedEffect(pointToEdit?.id) {
        pointToEdit?.let { point ->
            createViewModel.startEditing(point)
            currentPublishStep = 1
        }
    }

    followListMode?.let { mode ->
        val viewerId = currentUser?.id
        if (viewerId != null) {
            FollowListScreen(
                userId = viewerId,
                mode = mode,
                onNavigateBack = { followListMode = null },
                onOpenUser = { userId ->
                    followListMode = null
                    selectedUserId = userId
                }
            )
            return
        }
    }

    selectedUserId?.let { userId ->
        VisitedProfileScreen(
            userId = userId,
            onNavigateBack = { selectedUserId = null },
            onOpenPublication = { point ->
                selectedUserId = null
                selectedPoint = point
            }
        )
        return
    }

    if (selectedPoint != null) {
        TouristPointDetailScreen(
            point          = selectedPoint!!,
            isModerator    = false,
            onNavigateBack = { selectedPoint = null },
            onOpenAuthor   = { authorId ->
                selectedPoint = null
                selectedUserId = authorId
            }
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
                            points = verifiedFeedPoints,
                            onNavigateBack = { showMap = false }
                        )
                    } else {
                        ExploreScreen(
                            points = verifiedFeedPoints,
                            likedIds = likedPostIds,
                            currentUserId = currentUser?.id.orEmpty(),
                            onOpenMap = { showMap = true },
                            onOpenDetail = { point -> selectedPoint = point },
                            onToggleLike = { point -> feedViewModel.toggleLike(point.id) }
                        )
                    }
                }

                BottomNavTab.PUBLISH -> {
                    // FLUJO DE CREACIÓN/EDICIÓN DE 2 PASOS
                    if (currentPublishStep == 1) {
                        CreatePointStep1Screen(
                            photoUrls = createViewModel.selectedPhotoUrls,
                            title = createViewModel.title.value,
                            category = createViewModel.selectedCategory,
                            description = createViewModel.description.value,
                            isEditing = pointToEdit != null,
                            aiSuggestion = createViewModel.aiSuggestion,
                            aiCooldownSeconds = createViewModel.aiCooldownSeconds,
                            acceptedTags = createViewModel.acceptedAiTags,
                            onAddPhoto = { strUri -> createViewModel.uploadImageFromUri(context, Uri.parse(strUri)) },
                            onRemovePhoto = { url -> createViewModel.removePhoto(url) },
                            onTitle = { createViewModel.title.onChange(it) },
                            onCategory = { categoryEnum ->
                                createViewModel.onCategoryChange(categoryEnum)
                            },
                            onDescription = { createViewModel.description.onChange(it) },
                            onAiAssist = { createViewModel.runAiAssist() },
                            onToggleTag = { tag -> createViewModel.toggleAiTag(tag) },
                            onApplyAiDescription = { createViewModel.applyAiDescription() },
                            onDismissAi = { createViewModel.dismissAiSuggestion() },
                            onNext = {
                                if (createViewModel.validateStep1()) {
                                    currentPublishStep = 2
                                }
                            },
                            onSave = if (pointToEdit != null) {
                                {
                                    val success = createViewModel.submitPoint()
                                    if (success) {
                                        selectedTab = BottomNavTab.HOME
                                        pointToEdit = null
                                        currentPublishStep = 1
                                        createViewModel.reset()
                                        null
                                    } else {
                                        (createViewModel.createResult as? com.example.demoapp.core.utils.RequestResult.Error)?.message
                                            ?: "No se pudo guardar los cambios"
                                    }
                                }
                            } else null,
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
                                    null
                                } else {
                                    (createViewModel.createResult as? com.example.demoapp.core.utils.RequestResult.Error)?.message
                                        ?: "No se pudo publicar"
                                }
                            },
                            onSaveDraft = {
                                val success = createViewModel.submitAsDraft()
                                if (success) {
                                    selectedTab = BottomNavTab.HOME
                                    pointToEdit = null
                                    currentPublishStep = 1
                                    createViewModel.reset()
                                }
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
                            publications   = myPublications,
                            onNavigateBack = { showStatistics = false }
                        )
                        showEditProfile -> EditProfileScreen(
                            onNavigateBack   = { showEditProfile = false },
                            onAccountDeleted = {
                                showEditProfile = false
                                onLogout()
                            },
                            onLogout = {
                                showEditProfile = false
                                onLogout()
                            },
                            onNavigateToNotifications = {
                                selectedTab = BottomNavTab.NOTIFICATIONS
                                showEditProfile = false
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
                            },
                            onShowFollowers          = { followListMode = FollowListMode.FOLLOWERS },
                            onShowFollowing          = { followListMode = FollowListMode.FOLLOWING },
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}