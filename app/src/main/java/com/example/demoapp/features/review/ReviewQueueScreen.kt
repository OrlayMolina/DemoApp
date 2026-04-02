package com.example.demoapp.features.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.demoapp.domain.model.TouristPoint
import java.text.SimpleDateFormat
import java.util.*

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val GreenEmerald   = Color(0xFF00897B)
private val BackgroundGray = Color(0xFFF5F5F5)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val TextDark       = Color(0xFF1A1A1A)
private val OrangeWarning  = Color(0xFFF57C00)
private val NewBadgeColor  = Color(0xFFFFF9C4)
private val NewBadgeText   = Color(0xFF795548)

@Composable
fun ReviewQueueScreen(
    viewModel: ReviewQueueViewModel = viewModel(),
    onNavigateToDetail: (TouristPoint) -> Unit = {}
) {
    var selectedTab        by remember { mutableStateOf(0) }
    var showFilterDropdown by remember { mutableStateOf(false) }

    val currentList = if (selectedTab == 0)
        viewModel.pendingPoints else viewModel.reportedPoints

    Scaffold(containerColor = BackgroundGray) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .background(CardWhite)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Cola de Revisión",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )

                // ── Botón filtro ───────────────────────────────────────────
                Box {
                    IconButton(onClick = { showFilterDropdown = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            "Filtrar",
                            tint = if (viewModel.selectedDateFilter != DateFilter.ALL)
                                GreenEmerald else TextDark
                        )
                    }
                    DropdownMenu(
                        expanded         = showFilterDropdown,
                        onDismissRequest = { showFilterDropdown = false }
                    ) {
                        DateFilter.entries.forEach { filter ->
                            DropdownMenuItem(
                                text  = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        if (viewModel.selectedDateFilter == filter) {
                                            Icon(
                                                Icons.Default.Check,
                                                null,
                                                tint     = GreenEmerald,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Spacer(Modifier.size(16.dp))
                                        }
                                        Text(filter.label)
                                    }
                                },
                                onClick = {
                                    viewModel.setDateFilter(filter)
                                    showFilterDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Tabs ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite)
            ) {
                listOf(
                    "Nuevas (${viewModel.pendingPoints.size})",
                    "Reportadas (${viewModel.reportedPoints.size})"
                ).forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = title,
                            fontSize   = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold
                            else FontWeight.Normal,
                            color      = if (selectedTab == index) GreenEmerald else TextGray
                        )
                    }
                    if (index == 0) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(44.dp)
                                .align(Alignment.CenterVertically)
                                .background(Color(0xFFEEEEEE))
                        )
                    }
                }
            }

            // Indicador de tab activo
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(0, 1).forEach { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                if (selectedTab == index) GreenEmerald
                                else Color.Transparent
                            )
                            .clickable { selectedTab = index }
                    )
                }
            }

            // ── Lista ──────────────────────────────────────────────────────
            if (currentList.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint     = GreenEmerald,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Todo al día",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                        Text(
                            "No hay publicaciones pendientes",
                            fontSize = 13.sp,
                            color    = TextGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentList, key = { it.id }) { point ->
                        ReviewItem(
                            point      = point,
                            isReported = selectedTab == 1,
                            onClick    = { onNavigateToDetail(point) }
                        )
                    }
                }
            }
        }
    }
}

// ─── Item de revisión ─────────────────────────────────────────────────────────

@Composable
private fun ReviewItem(
    point      : TouristPoint,
    isReported : Boolean,
    onClick    : () -> Unit = {}
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            // Imagen
            AsyncImage(
                model              = point.photoUrls.firstOrNull(),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE0E0E0))
            )

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Badge Nuevo / Reportado
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isReported) Color(0xFFFFEBEE)
                            else NewBadgeColor
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text     = if (isReported) "Reportado" else "Nuevo",
                        fontSize = 10.sp,
                        color    = if (isReported) Color(0xFFD32F2F) else NewBadgeText,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text       = point.title,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                Text(
                    text     = if (isReported) (point.reportReason ?: point.description)
                    else point.description,
                    fontSize = 12.sp,
                    color    = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Fecha + autor
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint     = TextGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text     = "${formatDate(point.createdAt)}, ${formatTime(point.createdAt)}",
                        fontSize = 11.sp,
                        color    = TextGray
                    )
                }
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM", Locale("es")).format(Date(millis))

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

private fun Modifier.clickableTab(onClick: () -> Unit): Modifier {
    return this
        .padding(0.dp)
        .clickable(onClick = onClick)
}