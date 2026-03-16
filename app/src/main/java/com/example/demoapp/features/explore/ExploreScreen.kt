package com.example.demoapp.features.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory
import com.example.demoapp.ui.theme.DemoAppTheme

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val GreenPrimary   = Color(0xFF2E7D5E)
private val BackgroundGray = Color(0xFFF5F5F5)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val VerifiedBlue   = Color(0xFF1DA1F2)

// ─── Modelos de filtro ────────────────────────────────────────────────────────

enum class DistanceFilter(val label: String) {
    KM_1("1 km"), KM_15("15 km"), KM_25("25 km"), ALL("Todos")
}

enum class VerificationFilter(val label: String) {
    VERIFIED("Verificados"), PENDING("Pendientes")
}

data class FilterState(
    val selectedCategories   : Set<TouristPointCategory> = emptySet(),
    val distanceFilter       : DistanceFilter            = DistanceFilter.ALL,
    val verificationFilter   : VerificationFilter?       = null
)

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun categoryColor(category: TouristPointCategory): Color = when (category) {
    TouristPointCategory.NATURE        -> Color(0xFF388E3C)
    TouristPointCategory.GASTRONOMY    -> Color(0xFFE64A19)
    TouristPointCategory.CULTURE       -> Color(0xFF7B1FA2)
    TouristPointCategory.ENTERTAINMENT -> Color(0xFF1976D2)
    else                               -> Color(0xFF455A64)
}

private fun categoryLabel(category: TouristPointCategory): String = when (category) {
    TouristPointCategory.NATURE        -> "Naturaleza"
    TouristPointCategory.GASTRONOMY    -> "Gastronomía"
    TouristPointCategory.CULTURE       -> "Cultura"
    TouristPointCategory.ENTERTAINMENT -> "Arte Urbano"
    else                               -> "Otro"
}

private fun formatTimestamp(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

// ─── Lógica de filtrado ───────────────────────────────────────────────────────

private fun List<TouristPoint>.applyFilters(
    query   : String,
    filters : FilterState
): List<TouristPoint> = filter { point ->

    // 1. Búsqueda por texto
    val matchesQuery = query.isBlank() ||
            point.title.contains(query, ignoreCase = true) ||
            point.description.contains(query, ignoreCase = true)

    // 2. Categoría
    val matchesCategory = filters.selectedCategories.isEmpty() ||
            point.category in filters.selectedCategories

    // 3. Estado de verificación
    val matchesVerification = when (filters.verificationFilter) {
        VerificationFilter.VERIFIED -> point.isVerified
        VerificationFilter.PENDING  -> !point.isVerified
        null                        -> true
    }

    matchesQuery && matchesCategory && matchesVerification
}

// ─── Pantalla Explorar ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    points              : List<TouristPoint> = TouristPoint.SAMPLE_LIST,
    onOpenMap           : () -> Unit         = {}
) {
    var searchQuery      by remember { mutableStateOf("") }
    var selectedTab      by remember { mutableStateOf(0) }
    var showFilterSheet  by remember { mutableStateOf(false) }

    // Estado "en edición" dentro del sheet (se aplica solo al pulsar el botón)
    var appliedFilters   by remember { mutableStateOf(FilterState()) }
    var draftFilters     by remember { mutableStateOf(FilterState()) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filtered = remember(searchQuery, appliedFilters, points) {
        points.applyFilters(searchQuery, appliedFilters)
    }

    // Cuántas categorías activas hay (para badge en botón)
    val activeFilterCount = buildList {
        if (appliedFilters.selectedCategories.isNotEmpty()) add(1)
        if (appliedFilters.distanceFilter != DistanceFilter.ALL) add(1)
        if (appliedFilters.verificationFilter != null) add(1)
    }.size

    Scaffold(
        containerColor = BackgroundGray
    ) { paddingValues ->

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding      = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Top bar ────────────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = "Explorar",
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1A1A1A)
                    )
                    IconButton(onClick = onOpenMap) {
                        Icon(Icons.Default.Map, "Ver mapa", tint = Color(0xFF1A1A1A))
                    }
                }
            }

            // ── Barra de búsqueda + botón filtros ─────────────────────────
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier      = Modifier.weight(1f),
                        placeholder   = { Text("Buscar lugares ...", color = TextGray) },
                        leadingIcon   = { Icon(Icons.Default.Search, null, tint = TextGray) },
                        shape         = RoundedCornerShape(14.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = GreenPrimary,
                            unfocusedBorderColor    = Color(0xFFE0E0E0),
                            unfocusedContainerColor = CardWhite,
                            focusedContainerColor   = CardWhite
                        ),
                        singleLine = true
                    )

                    // ── Botón filtros con badge ────────────────────────────
                    Box(contentAlignment = Alignment.TopEnd) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (activeFilterCount > 0) GreenPrimary.copy(alpha = 0.1f)
                                    else CardWhite
                                )
                                .clickable {
                                    draftFilters = appliedFilters   // copia estado al abrir
                                    showFilterSheet = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Tune,
                                contentDescription = "Filtros",
                                tint               = if (activeFilterCount > 0) GreenPrimary
                                else Color(0xFF1A1A1A)
                            )
                        }
                        // Badge contador
                        if (activeFilterCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(GreenPrimary)
                                    .offset(x = 4.dp, y = (-4).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text     = activeFilterCount.toString(),
                                    color    = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ── Chip resumen de filtros activos ────────────────────────────
            if (activeFilterCount > 0) {
                item {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = "${filtered.size} resultado${if (filtered.size != 1) "s" else ""}",
                            fontSize = 13.sp,
                            color    = TextGray
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = { appliedFilters = FilterState() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Limpiar filtros", fontSize = 12.sp, color = GreenPrimary)
                        }
                    }
                }
            }

            // ── Lista ──────────────────────────────────────────────────────
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint     = TextGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Sin resultados", color = TextGray, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                items(filtered) { point ->
                    TouristPointCard(
                        point    = point,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    // ── Bottom Sheet de filtros ────────────────────────────────────────────────
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest  = { showFilterSheet = false },
            sheetState        = sheetState,
            containerColor    = CardWhite,
            shape             = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle        = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFDDDDDD))
                )
            }
        ) {
            FilterSheetContent(
                draft    = draftFilters,
                onChange = { draftFilters = it },
                onApply  = {
                    appliedFilters  = draftFilters
                    showFilterSheet = false
                },
                onClose  = { showFilterSheet = false }
            )
        }
    }
}

// ─── Contenido del sheet de filtros ──────────────────────────────────────────

@Composable
private fun FilterSheetContent(
    draft    : FilterState,
    onChange : (FilterState) -> Unit,
    onApply  : () -> Unit,
    onClose  : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = "Filtros",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF1A1A1A)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF1A1A1A))
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))

        // ── Categoría ──────────────────────────────────────────────────────
        FilterSection(title = "Categoría") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TouristPointCategory.entries.forEach { category ->
                    val selected = category in draft.selectedCategories
                    FilterChip(
                        selected = selected,
                        onClick  = {
                            val updated = if (selected)
                                draft.selectedCategories - category
                            else
                                draft.selectedCategories + category
                            onChange(draft.copy(selectedCategories = updated))
                        },
                        label    = { Text(categoryLabel(category), fontSize = 13.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor    = GreenPrimary,
                            selectedLabelColor        = Color.White,
                            containerColor            = Color(0xFFF0F0F0),
                            labelColor                = Color(0xFF1A1A1A)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled          = true,
                            selected         = selected,
                            borderColor      = Color(0xFFDDDDDD),
                            selectedBorderColor = GreenPrimary
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))

        // ── Lugares cercanos ───────────────────────────────────────────────
        FilterSection(title = "Lugares cercanos a ti") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DistanceFilter.entries.forEach { option ->
                    val selected = draft.distanceFilter == option
                    FilterChip(
                        selected = selected,
                        onClick  = { onChange(draft.copy(distanceFilter = option)) },
                        label    = { Text(option.label, fontSize = 13.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor     = Color.White,
                            containerColor         = Color(0xFFF0F0F0),
                            labelColor             = Color(0xFF1A1A1A)
                        ),
                        border   = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = selected,
                            borderColor         = Color(0xFFDDDDDD),
                            selectedBorderColor = GreenPrimary
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))

        // ── Estado de verificación ─────────────────────────────────────────
        FilterSection(title = "Estado de verificación") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VerificationFilter.entries.forEach { option ->
                    val selected = draft.verificationFilter == option
                    FilterChip(
                        selected = selected,
                        onClick  = {
                            // Toggle: si ya está seleccionado, deselecciona
                            onChange(
                                draft.copy(
                                    verificationFilter = if (selected) null else option
                                )
                            )
                        },
                        label  = { Text(option.label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor     = Color.White,
                            containerColor         = Color(0xFFF0F0F0),
                            labelColor             = Color(0xFF1A1A1A)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = selected,
                            borderColor         = Color(0xFFDDDDDD),
                            selectedBorderColor = GreenPrimary
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── Botón aplicar ──────────────────────────────────────────────────
        Button(
            onClick  = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Text(
                text       = "Aplicar Filtros",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── Sección con título ───────────────────────────────────────────────────────

@Composable
private fun FilterSection(
    title   : String,
    content : @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text       = title,
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color(0xFF1A1A1A)
        )
        content()
    }
}

// ─── Colores barra de navegación ─────────────────────────────────────────────

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    indicatorColor      = GreenPrimary.copy(alpha = 0.12f),
    selectedIconColor   = GreenPrimary,
    selectedTextColor   = GreenPrimary,
    unselectedIconColor = TextGray,
    unselectedTextColor = TextGray
)

// ─── Tarjeta de punto turístico ───────────────────────────────────────────────

@Composable
fun TouristPointCard(
    point    : TouristPoint,
    modifier : Modifier = Modifier
) {
    var liked by remember { mutableStateOf(false) }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model              = point.photoUrls.firstOrNull(),
                    contentDescription = point.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(categoryColor(point.category))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(categoryLabel(point.category), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Column(
                modifier            = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = point.title,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1A1A1A),
                        modifier   = Modifier.weight(1f),
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    if (point.isVerified) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.CheckCircle, "Verificado", tint = VerifiedBlue, modifier = Modifier.size(18.dp))
                    }
                }

                Text(point.description, fontSize = 13.sp, color = TextGray, maxLines = 2, overflow = TextOverflow.Ellipsis)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${"%.4f".format(point.latitude)}, ${"%.4f".format(point.longitude)}", fontSize = 12.sp, color = TextGray)
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier         = Modifier.size(28.dp).clip(CircleShape).background(GreenPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                        }
                        Text("Usuario • ${formatTimestamp(point.createdAt)}", fontSize = 11.sp, color = TextGray)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(
                                if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                "Me importa",
                                tint     = if (liked) Color(0xFFE91E63) else TextGray,
                                modifier = Modifier.size(16.dp).clickable { liked = !liked }
                            )
                            Text("${point.importantVotes + if (liked) 1 else 0}", fontSize = 11.sp, color = TextGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Outlined.PhotoLibrary, "Fotos", tint = TextGray, modifier = Modifier.size(16.dp))
                            Text("${point.photoUrls.size}", fontSize = 11.sp, color = TextGray)
                        }
                        Icon(Icons.Outlined.Share, "Compartir", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExploreScreenPreview() {
    DemoAppTheme { ExploreScreen() }
}