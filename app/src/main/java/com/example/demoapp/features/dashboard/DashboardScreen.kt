package com.example.demoapp.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demoapp.R

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val GreenEmerald    = Color(0xFF00897B)
private val GreenEmeraldDark= Color(0xFF00695C)
private val BackgroundGray  = Color(0xFFF5F5F5)
private val CardWhite       = Color(0xFFFFFFFF)
private val TextDark        = Color(0xFF1A1A1A)
private val OrangeWarning   = Color(0xFFF57C00)
private val RedDanger       = Color(0xFFD32F2F)
private val BlueInfo        = Color(0xFF1976D2)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onLogout : () -> Unit         = {}
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(containerColor = BackgroundGray) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Banner ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(GreenEmerald, GreenEmeraldDark)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text     = stringResource(R.string.dashboard_panel_title),
                        fontSize = 12.sp,
                        color    = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text       = stringResource(R.string.dashboard_greeting),
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
                IconButton(
                    onClick  = onLogout,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, stringResource(R.string.profile_edit_logout), tint = Color.White)
                }
            }

            // ── Cards resumen ──────────────────────────────────────────────
            Column(
                modifier            = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashStatCard(
                        modifier  = Modifier.weight(1f),
                        label     = stringResource(R.string.dashboard_pending),
                        value     = uiState.pendingCount.toString(),
                        icon      = Icons.Default.Pending,
                        iconColor = OrangeWarning
                    )
                    DashStatCard(
                        modifier  = Modifier.weight(1f),
                        label     = stringResource(R.string.dashboard_approved_today),
                        value     = uiState.approvedToday.toString(),
                        icon      = Icons.Default.CheckCircle,
                        iconColor = GreenEmerald
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashStatCard(
                        modifier  = Modifier.weight(1f),
                        label     = stringResource(R.string.dashboard_rejected_today),
                        value     = uiState.rejectedToday.toString(),
                        icon      = Icons.Default.Cancel,
                        iconColor = RedDanger
                    )
                    DashStatCard(
                        modifier  = Modifier.weight(1f),
                        label     = stringResource(R.string.dashboard_active_users),
                        value     = uiState.activeUsers.toString(),
                        icon      = Icons.Default.Group,
                        iconColor = BlueInfo
                    )
                }
            }

            // ── Actividad reciente ─────────────────────────────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        stringResource(R.string.dashboard_recent_activity),
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextDark
                    )
                    uiState.recentActivity.forEach { activity ->
                        RecentActivityItem(activity)
                        if (activity != uiState.recentActivity.last()) {
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                        }
                    }
                }
            }

            // ── Tu rendimiento ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(GreenEmerald, Color(0xFF26A69A))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            stringResource(R.string.dashboard_performance),
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        PerformanceStat("${uiState.reviewsToday}", stringResource(R.string.dashboard_reviews_today))
                        PerformanceStat("${uiState.precision}%", stringResource(R.string.dashboard_precision))
                        PerformanceStat("${uiState.minPerReview}", stringResource(R.string.dashboard_min_per_review))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DashStatCard(
    modifier  : Modifier,
    label     : String,
    value     : String,
    icon      : ImageVector,
    iconColor : Color
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Text(label, fontSize = 11.sp, color = Color(0xFF6B6B6B))
            }
            Text(
                text       = value,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF1A1A1A)
            )
        }
    }
}

@Composable
private fun RecentActivityItem(activity: RecentActivity) {
    val title = stringResource(
        id = when (activity.type) {
            ActivityType.APPROVED -> R.string.dashboard_activity_title_approved
            ActivityType.REJECTED -> R.string.dashboard_activity_title_rejected
            ActivityType.REPORTED -> R.string.dashboard_activity_title_reported
        }
    )
    val time = stringResource(
        id = when (activity.timeSlot) {
            ActivityTimeSlot.MIN_5 -> R.string.dashboard_activity_time_5m
            ActivityTimeSlot.MIN_12 -> R.string.dashboard_activity_time_12m
            ActivityTimeSlot.MIN_30 -> R.string.dashboard_activity_time_30m
            ActivityTimeSlot.HOUR_1 -> R.string.dashboard_activity_time_1h
        }
    )
    val subtitle = stringResource(
        R.string.dashboard_activity_subtitle,
        activity.userName.ifBlank { stringResource(R.string.dashboard_unknown_user) },
        time
    )

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(8.dp)
                .background(
                    color  = when (activity.type) {
                        ActivityType.APPROVED -> GreenEmerald
                        ActivityType.REJECTED -> RedDanger
                        ActivityType.REPORTED -> OrangeWarning
                    },
                    shape  = androidx.compose.foundation.shape.CircleShape
                )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF6B6B6B))
        }
    }
}

@Composable
private fun PerformanceStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
    }
}