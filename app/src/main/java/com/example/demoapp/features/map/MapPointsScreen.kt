package com.example.demoapp.features.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.core.component.MapBox
import com.example.demoapp.core.components.DropdownMenu
import com.example.demoapp.domain.model.TouristPointCategory

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val TextDark       = Color(0xFF1A1A1A)
private val GreenPrimary   = Color(0xFF2E7D5E)
private val ChipGray       = Color(0xFFDDE4E1)

// ─── Helpers ──────────────────────────────────────────────────────────────────

internal fun categoryLabel(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.GASTRONOMY    -> "Gastronomía"
    TouristPointCategory.CULTURE       -> "Cultura"
    TouristPointCategory.NATURE        -> "Naturaleza"
    TouristPointCategory.ENTERTAINMENT -> "Entretenimiento"
    TouristPointCategory.HISTORY       -> "Historia"
    else                               -> "Otro"
}

private fun categoryEmoji(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.GASTRONOMY    -> "🍽️"
    TouristPointCategory.CULTURE       -> "🏛️"
    TouristPointCategory.NATURE        -> "🌿"
    TouristPointCategory.ENTERTAINMENT -> "🎭"
    TouristPointCategory.HISTORY       -> "📜"
    else                               -> "📍"
}

// ─── Pantalla principal ───────────────────────────────────────────────────────

@Composable
fun MapPointsScreen(
    viewModel     : MapPointsViewModel = viewModel(),
    onNavigateBack: () -> Unit         = {}
) {
    if (viewModel.showingMap) {
        MapWithPointsScreen(
            viewModel = viewModel,
            onBack    = { viewModel.goBackToSelection() }
        )
    } else {
        CategorySelectionScreen(
            viewModel     = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}

// ─── Pantalla 1: Selección de categoría ──────────────────────────────────────

@Composable
private fun CategorySelectionScreen(
    viewModel     : MapPointsViewModel,
    onNavigateBack: () -> Unit
) {
    val categoryOptions = listOf("Todas") +
            TouristPointCategory.entries.map { categoryLabel(it) }

    var dropdownValue by remember { mutableStateOf("Todas") }

    Scaffold(containerColor = BackgroundGray) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Top bar ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .background(CardWhite)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = TextDark)
                }
                Box(modifier = Modifier.weight(1f)) {
                    DropdownMenu(
                        value         = dropdownValue,
                        label         = "Mapa por puntos de interés",
                        list          = categoryOptions,
                        onValueChange = { selected ->
                            dropdownValue = selected
                            val cat = TouristPointCategory.entries
                                .find { categoryLabel(it) == selected }
                            viewModel.selectCategory(cat)
                        }
                    )
                }
            }

            // ── Contenido central ──────────────────────────────────────────
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text       = "Vista de Mapa",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextDark
                        )
                        Text(
                            text     = "${viewModel.allPoints.size} puntos de interés en el área",
                            fontSize = 13.sp,
                            color    = TextGray
                        )

                        // ── Grid 2x2 de top categorías ─────────────────────
                        val topCats = viewModel.topCategories
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            topCats.chunked(2).forEach { row ->
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    row.forEach { (cat, count) ->
                                        CategoryChip(
                                            category = cat,
                                            count    = count,
                                            modifier = Modifier.weight(1f),
                                            onClick  = { viewModel.selectCategory(cat) }
                                        )
                                    }
                                    // Si la fila tiene solo 1 elemento, rellena el espacio
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        // Botón ver todas
                        OutlinedButton(
                            onClick = { viewModel.selectCategory(null) },
                            shape   = RoundedCornerShape(10.dp),
                            colors  = ButtonDefaults.outlinedButtonColors(
                                contentColor = GreenPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver todas en el mapa", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─── Chip de categoría ────────────────────────────────────────────────────────

@Composable
private fun CategoryChip(
    category : TouristPointCategory,
    count    : Int,
    modifier : Modifier = Modifier,
    onClick  : () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ChipGray)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(categoryEmoji(category), fontSize = 14.sp)
                Text(
                    text     = categoryLabel(category),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color    = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text     = "$count publicación${if (count != 1) "es" else ""}",
                fontSize = 11.sp,
                color    = TextGray
            )
        }
    }
}

// ─── Pantalla 2: Mapa con puntos ──────────────────────────────────────────────

@Composable
private fun MapWithPointsScreen(
    viewModel : MapPointsViewModel,
    onBack    : () -> Unit
) {
    val label = viewModel.selectedCategory
        ?.let { categoryLabel(it) }
        ?: "Todas las categorías"

    Box(modifier = Modifier.fillMaxSize()) {

        // Mapa a pantalla completa
        MapBox(
            modifier             = Modifier.fillMaxSize(),
            points               = viewModel.filteredPoints,
            showMyLocationButton = true,
            activateClick        = false
        )

        // ── Top bar flotante ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite.copy(alpha = 0.95f))
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = TextDark)
            }
            Column {
                Text(
                    text       = label,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark
                )
                Text(
                    text     = "${viewModel.filteredPoints.size} punto${if (viewModel.filteredPoints.size != 1) "s" else ""} en el mapa",
                    fontSize = 12.sp,
                    color    = TextGray
                )
            }
        }
    }
}