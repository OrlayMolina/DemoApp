package com.example.demoapp.features.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.domain.model.ReviewAction
import com.example.demoapp.domain.model.ReviewHistory
import com.example.demoapp.domain.model.TouristPointCategory
import java.text.SimpleDateFormat
import java.util.*

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val GreenEmerald    = Color(0xFF00897B)
private val GreenEmeraldDark= Color(0xFF00695C)
private val BackgroundGray  = Color(0xFFF5F5F5)
private val CardWhite       = Color(0xFFFFFFFF)
private val TextGray        = Color(0xFF6B6B6B)
private val TextDark        = Color(0xFF1A1A1A)
private val ApprovedGreen   = Color(0xFF2E7D3F)
private val RejectedRed     = Color(0xFFD32F2F)

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun categoryLabel(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.NATURE        -> "Naturaleza"
    TouristPointCategory.GASTRONOMY    -> "Gastronomía"
    TouristPointCategory.CULTURE       -> "Cultura"
    TouristPointCategory.ENTERTAINMENT -> "Arte Urbano"
    TouristPointCategory.HISTORY       -> "Historia"
    else                               -> "Otro"
}

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("d MMM, HH:mm", Locale("es")).format(Date(millis))

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel()
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Todas (${viewModel.totalCount})",
        "Aprobadas (${viewModel.approvedCount})",
        "Rechazadas (${viewModel.rejectedCount})"
    )

    val tabFilters = listOf(
        HistoryFilter.ALL,
        HistoryFilter.APPROVED,
        HistoryFilter.REJECTED
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {

        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(CardWhite)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = "Historial",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = TextDark
            )
            Box {
                IconButton(onClick = { showFilterMenu = true }) {
                    Icon(
                        Icons.Default.FilterList,
                        "Filtrar",
                        tint = if (viewModel.activeFilter != HistoryFilter.ALL)
                            GreenEmerald else TextDark
                    )
                }
                DropdownMenu(
                    expanded         = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    listOf(
                        HistoryFilter.ALL      to "Todos",
                        HistoryFilter.APPROVED to "Aprobadas",
                        HistoryFilter.REJECTED to "Rechazadas"
                    ).forEach { (filter, label) ->
                        DropdownMenuItem(
                            text  = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    if (viewModel.activeFilter == filter) {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            tint     = GreenEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Spacer(Modifier.size(16.dp))
                                    }
                                    Text(label)
                                }
                            },
                            onClick = {
                                viewModel.onFilterChange(filter)
                                showFilterMenu = false
                            }
                        )
                    }
                }
            }
        }

        // ── Barra de búsqueda ──────────────────────────────────────────────
        OutlinedTextField(
            value         = viewModel.searchQuery,
            onValueChange = { viewModel.onSearchChange(it) },
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            placeholder   = { Text("Buscar en historial...", color = TextGray) },
            leadingIcon   = { Icon(Icons.Default.Search, null, tint = TextGray) },
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = GreenEmerald,
                unfocusedBorderColor    = Color(0xFFE0E0E0),
                unfocusedContainerColor = CardWhite,
                focusedContainerColor   = CardWhite
            )
        )

        // ── Tabs ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = viewModel.activeFilter == tabFilters[index]
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.onFilterChange(tabFilters[index]) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = title,
                        fontSize   = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (isSelected) GreenEmerald else TextGray
                    )
                }
                if (index < tabs.lastIndex) {
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

        // Indicador tab activo
        Row(modifier = Modifier.fillMaxWidth()) {
            tabFilters.forEach { filter ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(
                            if (viewModel.activeFilter == filter) GreenEmerald
                            else Color.Transparent
                        )
                )
            }
        }

        // ── Lista ──────────────────────────────────────────────────────────
        if (viewModel.filteredHistory.isEmpty()) {
            Box(
                modifier         = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        null,
                        tint     = TextGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Sin resultados", color = TextGray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier       = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    top    = 12.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.filteredHistory, key = { it.id }) { item ->
                    HistoryItem(item = item)
                }
            }
        }

        // ── Resumen fijo abajo ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(GreenEmerald, GreenEmeraldDark)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryItem(
                    value = viewModel.totalCount.toString(),
                    label = "Total"
                )
                SummaryItem(
                    value = viewModel.approvedCount.toString(),
                    label = "Aprobadas"
                )
                SummaryItem(
                    value = viewModel.rejectedCount.toString(),
                    label = "Rechazadas"
                )
            }
        }
    }
}

// ─── Item del historial ───────────────────────────────────────────────────────

@Composable
private fun HistoryItem(item: ReviewHistory) {
    val isApproved = item.action == ReviewAction.APPROVED

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment   = Alignment.CenterVertically
        ) {

            // ── Ícono acción ───────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isApproved) ApprovedGreen.copy(alpha = 0.1f)
                        else RejectedRed.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isApproved) Icons.Default.CheckCircle
                    else Icons.Default.Cancel,
                    contentDescription = null,
                    tint     = if (isApproved) ApprovedGreen else RejectedRed,
                    modifier = Modifier.size(22.dp)
                )
            }

            // ── Contenido ──────────────────────────────────────────────────
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text       = item.pointTitle,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                // Razón de rechazo si existe
                if (!isApproved && item.rejectionReason != null) {
                    Text(
                        text     = "Razón: ${item.rejectionReason}",
                        fontSize = 12.sp,
                        color    = RejectedRed
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text     = item.reviewedBy,
                        fontSize = 11.sp,
                        color    = TextGray
                    )
                    Text("·", fontSize = 11.sp, color = TextGray)
                    Text(
                        text     = formatDateTime(item.reviewedAt),
                        fontSize = 11.sp,
                        color    = TextGray
                    )
                }
            }

            // ── Badge categoría ────────────────────────────────────────────
            Text(
                text     = categoryLabel(item.category),
                fontSize = 10.sp,
                color    = TextGray,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

// ─── Resumen ──────────────────────────────────────────────────────────────────

@Composable
private fun SummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White
        )
        Text(
            text     = label,
            fontSize = 11.sp,
            color    = Color.White.copy(alpha = 0.85f)
        )
    }
}