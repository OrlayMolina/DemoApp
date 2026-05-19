package com.example.demoapp.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.demoapp.domain.model.User
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

private val GreenEmerald    = Color(0xFF00897B)

@Composable
fun ModeratorScreen(
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ModeratorTab.DASHBOARD) }
    var selectedPoint  by remember { mutableStateOf<TouristPoint?>(null) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            ModeratorBottomNavBar(
                selectedTab   = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    selectedPoint = null
                    selectedUserId = null
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                selectedUserId != null -> ModeratorUserDetailScreen(
                    userId         = selectedUserId!!,
                    onNavigateBack = { selectedUserId = null }
                )
                selectedPoint != null -> TouristPointDetailScreen(
                    point          = selectedPoint!!,
                    isModerator    = true,
                    onNavigateBack = { selectedPoint = null },
                    onApproved     = { selectedPoint = null },
                    onRejected     = { selectedPoint = null },
                    onOpenAuthor   = { authorId ->
                        selectedPoint = null
                        selectedUserId = authorId
                    }
                )
                else -> when (selectedTab) {
                    ModeratorTab.DASHBOARD -> DashboardScreen(onLogout = onLogout)
                    ModeratorTab.REVIEW    -> ReviewQueueScreen(
                        onNavigateToDetail = { selectedPoint = it }
                    )
                    ModeratorTab.HISTORY   -> HistoryScreen()
                    ModeratorTab.REPORTS   -> ReportsScreen(
                        onNavigateToDetail = { selectedPoint = it }
                    )
                    ModeratorTab.USERS     -> UsersScreen(
                        onNavigateToUser = { selectedUserId = it }
                    )
                }
            }
        }
    }
}

// ─── Pantalla de Reportes ─────────────────────────────────────────────────

@Composable
fun ReportsScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToDetail: (TouristPoint) -> Unit = {}
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetail(point) },
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
                                    onClick = { onNavigateToDetail(point) },
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
                                    onClick = { onNavigateToDetail(point) },
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
    viewModel: UserListViewModel = hiltViewModel(),
    onNavigateToUser: (String) -> Unit = {}
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
    val relations by viewModel.relations.collectAsStateWithLifecycle()
    val createdUsers = users.filter { user ->
        user.role != com.example.demoapp.domain.model.UserRole.ADMIN &&
            user.email.trim().lowercase() !in seedEmails
    }
    var banDialogUser by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenEmerald)
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
                    val followersCount = relations.count { it.followingId == user.id }
                    val followingCount = relations.count { it.followerId == user.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToUser(user.id) },
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        user.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                    if (user.isBanned) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                stringResource(R.string.moderator_user_status_banned),
                                                fontSize = 10.sp,
                                                color = Color(0xFFD32F2F),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    if (user.isBanned && user.banAppeal.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = Color(0xFFF57C00).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                stringResource(R.string.moderator_appeal_pending),
                                                fontSize = 10.sp,
                                                color = Color(0xFFF57C00),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
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
                                        stringResource(R.string.moderator_followers_count, followersCount),
                                        fontSize = 11.sp,
                                        color = Color(0xFF00897B)
                                    )
                                    Text(
                                        stringResource(R.string.moderator_following_count, followingCount),
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D5E)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    if (user.isBanned) onNavigateToUser(user.id)
                                    else banDialogUser = user
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Block,
                                    contentDescription = stringResource(R.string.moderator_block_user_desc),
                                    tint = if (user.isBanned) Color(0xFF9E9E9E) else Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    banDialogUser?.let { target ->
        BanReasonDialog(
            user      = target,
            onDismiss = { banDialogUser = null },
            onConfirm = { reason ->
                viewModel.banUser(target.id, reason)
                banDialogUser = null
            }
        )
    }
}

@Composable
private fun BanReasonDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.moderator_ban_dialog_title, user.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.moderator_ban_dialog_message),
                    fontSize = 13.sp,
                    color = Color(0xFF6B6B6B)
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.moderator_ban_reason_label)) },
                    placeholder = { Text(stringResource(R.string.moderator_ban_reason_placeholder)) },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank()
            ) {
                Text(
                    text = stringResource(R.string.moderator_ban_confirm),
                    color = Color(0xFFD32F2F)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}