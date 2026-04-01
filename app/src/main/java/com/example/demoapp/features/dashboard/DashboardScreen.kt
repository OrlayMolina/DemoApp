package com.example.demoapp.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val GreenEmerald    = Color(0xFF00897B)
private val GreenEmeraldDark= Color(0xFF00695C)
private val BackgroundGray  = Color(0xFFF5F5F5)
private val CardWhite       = Color(0xFFFFFFFF)
private val TextGray        = Color(0xFF6B6B6B)
private val TextDark        = Color(0xFF1A1A1A)
private val OrangeWarning   = Color(0xFFF57C00)
private val RedDanger       = Color(0xFFD32F2F)
private val BlueInfo        = Color(0xFF1976D2)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onLogout : () -> Unit         = {}
) {
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
                        text     = "Panel de Moderador",
                        fontSize = 12.sp,
                        color    = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text       = "¡Hola, Carlos Admin!",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
                IconButton(
                    onClick  = onLogout,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Logout, "Cerrar sesión", tint = Color.White)
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
                        label     = "Pendientes",
                        value     = viewModel.pendingCount.toString(),
                        icon      = Icons.Default.Pending,
                        iconColor = OrangeWarning
                    )
                    DashStatCard(
                        modifier  = Modifier.weight(1f),
                        label     = "Aprobadas hoy",
                        value     = viewModel.approvedToday.toString(),
                        icon      = Icons.Default.CheckCircle,
                        iconColor = GreenEmerald
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashStatCard(
                        modifier  = Modifier.weight(1f),
                        label     = "Rechazadas hoy",
                        value     = viewModel.rejectedToday.toString(),
                        icon      = Icons.Default.Cancel,
                        iconColor = RedDanger
                    )
                    DashStatCard(
                        modifier  = Modifier.weight(1f),
                        label     = "Usuarios Activos",
                        value     = viewModel.activeUsers.toString(),
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
                        "Actividad Reciente",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextDark
                    )
                    viewModel.recentActivity.forEach { activity ->
                        RecentActivityItem(activity)
                        if (activity != viewModel.recentActivity.last()) {
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
                        Text("🏆", fontSize = 16.sp)
                        Text(
                            "Tu Rendimiento",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        PerformanceStat("${viewModel.reviewsToday}", "Revisiones Hoy")
                        PerformanceStat("${viewModel.precision}%", "Precisión")
                        PerformanceStat("${viewModel.minPerReview}", "Min/ Revisión")
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
            Text(activity.title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
            Text(activity.subtitle, fontSize = 11.sp, color = Color(0xFF6B6B6B))
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