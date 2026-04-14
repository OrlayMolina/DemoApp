package com.example.demoapp.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.R
import com.example.demoapp.core.navigation.ModeratorBottomNavBar
import com.example.demoapp.core.navigation.ModeratorTab
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.features.detail.TouristPointDetailScreen
import com.example.demoapp.features.history.HistoryScreen
import com.example.demoapp.features.review.ReviewQueueScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.People
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demoapp.features.users.list.UserListViewModel

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
            when (selectedTab) {
                ModeratorTab.DASHBOARD -> DashboardScreen(onLogout = onLogout)
                ModeratorTab.REVIEW    -> ReviewQueueScreen(
                    onNavigateToDetail = { selectedPoint = it }
                )
                ModeratorTab.HISTORY   -> HistoryScreen()
                ModeratorTab.REPORTS   -> ReportsScreen()
                ModeratorTab.USERS     -> UsersScreen()
            }
        }
    }
}

// ─── Pantalla de Reportes ─────────────────────────────────────────────────

@Composable
fun ReportsScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E7D5E))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = stringResource(R.string.moderator_reports),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.moderator_reports),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        val reportedPoints = uiState.allPoints
            .filter { it.isReported }

        if (reportedPoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.moderator_no_reports),
                    fontSize = 16.sp,
                    color = Color(0xFF6B6B6B)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reportedPoints) { point ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                point.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.moderator_report_reason, point.reportReason ?: stringResource(R.string.common_not_specified)),
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E7D5E)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(stringResource(R.string.moderator_review), fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(stringResource(R.string.moderator_discard), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Pantalla de Usuarios ─────────────────────────────────────────────────

@Composable
fun UsersScreen(
    viewModel: UserListViewModel = hiltViewModel()
) {
    val seedEmails = remember {
        setOf(
            "juan@email.com",
            "maria@email.com",
            "carlos@email.com",
            "admin@redexplora.com"
        )
    }
    val users by viewModel.users.collectAsStateWithLifecycle()
    val createdUsers = users.filter { user ->
        user.role != com.example.demoapp.domain.model.UserRole.ADMIN &&
            user.email.trim().lowercase() !in seedEmails
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A73E8))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.People,
                    contentDescription = stringResource(R.string.users_title),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.moderator_user_management),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (createdUsers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.moderator_no_users),
                    fontSize = 15.sp,
                    color = Color(0xFF6B6B6B)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(createdUsers, key = { it.id }) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    user.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    user.email,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B6B6B)
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        stringResource(R.string.moderator_followers_count, user.followers),
                                        fontSize = 11.sp,
                                        color = Color(0xFF00897B)
                                    )
                                    Text(
                                        stringResource(R.string.moderator_following_count, user.following),
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D5E)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Block,
                                    contentDescription = stringResource(R.string.moderator_block_user_desc),
                                    tint = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}