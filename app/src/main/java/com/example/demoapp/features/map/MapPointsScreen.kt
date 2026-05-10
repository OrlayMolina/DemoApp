package com.example.demoapp.features.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.demoapp.R
import com.example.demoapp.core.component.MapBox
import com.example.demoapp.core.components.DropdownMenu
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val TextDark       = Color(0xFF1A1A1A)
private val GreenPrimary   = Color(0xFF2E7D5E)
private val ChipGray       = Color(0xFFDDE4E1)

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
internal fun categoryLabel(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.GASTRONOMY    -> stringResource(R.string.map_points_cat_gastronomy)
    TouristPointCategory.CULTURE       -> stringResource(R.string.map_points_cat_culture)
    TouristPointCategory.NATURE        -> stringResource(R.string.map_points_cat_nature)
    TouristPointCategory.ENTERTAINMENT -> stringResource(R.string.map_points_cat_entertainment)
    TouristPointCategory.HISTORY       -> stringResource(R.string.map_points_cat_history)
    else                               -> stringResource(R.string.map_points_cat_other)
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
    points        : List<TouristPoint> = TouristPoint.SAMPLE_LIST,
    viewModel     : MapPointsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit         = {}
) {
    LaunchedEffect(points) {
        viewModel.setPoints(points)
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelectionScreen(
    viewModel     : MapPointsViewModel,
    onNavigateBack: () -> Unit
) {
    val allLabel = stringResource(R.string.map_points_all)
    val localizedCategories = TouristPointCategory.entries.map { category ->
        category to categoryLabel(category)
    }
    val categoryOptions = listOf(allLabel) + localizedCategories.map { it.second }

    var dropdownValue by remember { mutableStateOf(allLabel) }
    val aiQuery by viewModel.aiQuery.collectAsState()
    val aiState by viewModel.aiResults.collectAsState()

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
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.common_back), tint = TextDark)
                }
                Box(modifier = Modifier.weight(1f)) {
                    DropdownMenu(
                        value         = dropdownValue,
                        label         = stringResource(R.string.map_points_dropdown_label),
                        list          = categoryOptions,
                        onValueChange = { selected ->
                            dropdownValue = selected
                            val cat = localizedCategories.find { it.second == selected }?.first
                            viewModel.selectCategory(cat)
                        }
                    )
                }
            }

            // ── Buscador IA ────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text       = "Busqueda con IA",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                    }
                    OutlinedTextField(
                        value         = aiQuery,
                        onValueChange = { viewModel.onAiQueryChange(it) },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("Ej: lugares para llevar ninos al aire libre", color = TextGray, fontSize = 13.sp) },
                        leadingIcon   = { Icon(Icons.Default.Search, null, tint = TextGray) },
                        trailingIcon  = {
                            if (aiQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onAiQueryChange("") }) {
                                    Icon(Icons.Default.Close, null, tint = TextGray)
                                }
                            }
                        },
                        singleLine = true,
                        shape      = RoundedCornerShape(10.dp)
                    )
                    AiResultsSection(
                        state    = aiState,
                        onOpenOnMap = { viewModel.openAiResultsOnMap() }
                    )
                }
            }

            // ── Contenido central ──────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                            text       = stringResource(R.string.map_points_title),
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextDark
                        )
                        Text(
                            text     = stringResource(R.string.map_points_count_area, viewModel.allPoints.size),
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
                            Text(stringResource(R.string.map_points_view_all), fontSize = 13.sp)
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
                text     = stringResource(R.string.map_points_publications_count, count),
                fontSize = 11.sp,
                color    = TextGray
            )
        }
    }
}

// ─── Resultados IA en pantalla de seleccion ──────────────────────────────────

@Composable
private fun AiResultsSection(
    state       : MapPointsViewModel.AiSearchState,
    onOpenOnMap : () -> Unit
) {
    when (state) {
        MapPointsViewModel.AiSearchState.Idle -> {
            Text(
                text     = "Escribe lo que buscas y la IA encontrara los puntos mas afines.",
                fontSize = 12.sp,
                color    = TextGray
            )
        }
        MapPointsViewModel.AiSearchState.Loading -> {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = GreenPrimary
                )
                Text("Buscando con IA...", fontSize = 12.sp, color = TextGray)
            }
        }
        is MapPointsViewModel.AiSearchState.Error -> {
            Text(
                text     = "No se pudo consultar la IA: ${state.message}",
                fontSize = 12.sp,
                color    = Color(0xFFB00020)
            )
        }
        is MapPointsViewModel.AiSearchState.Success -> {
            if (state.points.isEmpty()) {
                Text(
                    text     = "Sin coincidencias relevantes. Prueba con otra frase.",
                    fontSize = 12.sp,
                    color    = TextGray
                )
            } else {
                Text(
                    text       = "${state.points.size} resultado(s)",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark
                )
                LazyColumn(
                    modifier            = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.points, key = { it.id }) { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ChipGray)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(categoryEmoji(point.category), fontSize = 14.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = point.title,
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = TextDark,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis
                                )
                                Text(
                                    text     = categoryLabel(point.category),
                                    fontSize = 11.sp,
                                    color    = TextGray
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = onOpenOnMap,
                    modifier = Modifier.fillMaxWidth(),
                    shape   = RoundedCornerShape(10.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Ver en mapa", fontSize = 13.sp)
                }
            }
        }
    }
}

// ─── Pantalla 2: Mapa con puntos ──────────────────────────────────────────────

@Composable
private fun MapWithPointsScreen(
    viewModel : MapPointsViewModel,
    onBack    : () -> Unit
) {
    val label = if (viewModel.aiSearchActive) {
        "Resultados IA"
    } else {
        viewModel.selectedCategory
            ?.let { categoryLabel(it) }
            ?: stringResource(R.string.map_points_all_categories)
    }

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
                Icon(Icons.Default.ArrowBack, stringResource(R.string.common_back), tint = TextDark)
            }
            Column {
                Text(
                    text       = label,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark
                )
                Text(
                    text     = stringResource(R.string.map_points_count_on_map, viewModel.filteredPoints.size),
                    fontSize = 12.sp,
                    color    = TextGray
                )
            }
        }
    }
}