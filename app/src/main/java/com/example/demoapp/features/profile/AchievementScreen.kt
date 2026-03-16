package com.example.demoapp.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.domain.model.Achievement
import com.example.demoapp.domain.model.AchievementIcon

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val GreenPrimary   = Color(0xFF2E7D5E)
private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val TextDark       = Color(0xFF1A1A1A)
private val GoldColor      = Color(0xFFFF9800)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun AchievementScreen(
    achievements : List<Achievement> = Achievement.SAMPLE_LIST,
    onNavigateBack: () -> Unit       = {}
) {
    val unlocked = achievements.filter { it.isUnlocked }
    val locked   = achievements.filter { !it.isUnlocked }

    Scaffold(containerColor = BackgroundGray) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = TextDark)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text       = "Logros y Badges",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }

            // ── Banner resumen ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFB300), Color(0xFFFF6F00))
                        )
                    )
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(48.dp)
                    )
                    Text(
                        text       = "${unlocked.size} Logros",
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        text     = "${locked.size} más por desbloquear",
                        fontSize = 14.sp,
                        color    = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Desbloqueados ──────────────────────────────────────────────
            if (unlocked.isNotEmpty()) {
                Text(
                    text     = "Desbloqueados (${unlocked.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color    = TextDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(10.dp))
                unlocked.forEach { achievement ->
                    AchievementItem(
                        achievement = achievement,
                        modifier    = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Por desbloquear ────────────────────────────────────────────
            if (locked.isNotEmpty()) {
                Text(
                    text       = "Por Desbloquear (${locked.size})",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark,
                    modifier   = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(10.dp))
                locked.forEach { achievement ->
                    AchievementItem(
                        achievement = achievement,
                        modifier    = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Tip ────────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 18.sp)
                        Text(
                            text       = "Cómo ganar más logros",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                    }
                    listOf(
                        "Publica lugares únicos e interesantes",
                        "Interactúa con la comunidad",
                        "Sube fotos de calidad",
                        "Explora diferentes categorías"
                    ).forEach { tip ->
                        Text(
                            text     = "• $tip",
                            fontSize = 13.sp,
                            color    = TextGray
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Item de logro ────────────────────────────────────────────────────────────

@Composable
private fun AchievementItem(
    achievement : Achievement,
    modifier    : Modifier = Modifier
) {
    val isUnlocked = achievement.isUnlocked

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment   = Alignment.CenterVertically
        ) {

            // ── Ícono ──────────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) GoldColor.copy(alpha = 0.12f)
                        else Color(0xFFF0F0F0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = achievementIcon(achievement.icon),
                    contentDescription = null,
                    tint               = if (isUnlocked) GoldColor else Color(0xFFBBBBBB),
                    modifier           = Modifier.size(28.dp)
                )
            }

            // ── Contenido ──────────────────────────────────────────────────
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text       = achievement.title,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark
                )
                Text(
                    text     = achievement.description,
                    fontSize = 12.sp,
                    color    = TextGray
                )

                if (isUnlocked) {
                    // Fecha de desbloqueo
                    Text(
                        text     = "Desbloqueado el ${achievement.unlockedDate}",
                        fontSize = 11.sp,
                        color    = GreenPrimary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    // Barra de progreso
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = "Progreso",
                            fontSize = 11.sp,
                            color    = TextGray
                        )
                        Text(
                            text     = "${achievement.progress} / ${achievement.goal}",
                            fontSize = 11.sp,
                            color    = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LinearProgressIndicator(
                        progress   = { achievement.progress.toFloat() / achievement.goal },
                        modifier   = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color      = GreenPrimary,
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

// ─── Íconos por tipo ──────────────────────────────────────────────────────────

private fun achievementIcon(icon: AchievementIcon): ImageVector = when (icon) {
    AchievementIcon.STAR      -> Icons.Default.Star
    AchievementIcon.CAMERA    -> Icons.Default.CameraAlt
    AchievementIcon.HEART     -> Icons.Default.Favorite
    AchievementIcon.TROPHY    -> Icons.Default.EmojiEvents
    AchievementIcon.COMMUNITY -> Icons.Default.Groups
    AchievementIcon.CHECK     -> Icons.Default.Verified
}