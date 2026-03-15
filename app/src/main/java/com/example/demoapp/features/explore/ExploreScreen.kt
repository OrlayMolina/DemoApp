package com.example.demoapp.features.explore

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

// ─── Colores por categoría ────────────────────────────────────────────────────

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

// ─── Pantalla Explorar ────────────────────────────────────────────────────────

@Composable
fun ExploreScreen(
    points: List<TouristPoint>      = TouristPoint.SAMPLE_LIST,
    onNavigateToHome: () -> Unit    = {},
    onNavigateToPublish: () -> Unit = {},
    onNavigateToNotifs: () -> Unit  = {},
    onNavigateToProfile: () -> Unit = {},
    onOpenMap: () -> Unit           = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    val filtered = points.filter {
        searchQuery.isBlank() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = BackgroundGray,
        // ── Bottom Navigation ──────────────────────────────────────────────
        bottomBar = {
            NavigationBar(
                containerColor = CardWhite,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0; onNavigateToHome() },
                    icon     = {
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Home
                            else Icons.Outlined.Home,
                            contentDescription = "Inicio"
                        )
                    },
                    label  = { Text("Inicio", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor        = GreenPrimary.copy(alpha = 0.12f),
                        selectedIconColor     = GreenPrimary,
                        selectedTextColor     = GreenPrimary,
                        unselectedIconColor   = TextGray,
                        unselectedTextColor   = TextGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1; onNavigateToPublish() },
                    icon     = {
                        Icon(
                            if (selectedTab == 1) Icons.Filled.AddCircle
                            else Icons.Outlined.AddCircle,
                            contentDescription = "Publicar"
                        )
                    },
                    label  = { Text("Publicar", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor        = GreenPrimary.copy(alpha = 0.12f),
                        selectedIconColor     = GreenPrimary,
                        selectedTextColor     = GreenPrimary,
                        unselectedIconColor   = TextGray,
                        unselectedTextColor   = TextGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick  = { selectedTab = 2; onNavigateToNotifs() },
                    icon     = {
                        Icon(
                            if (selectedTab == 2) Icons.Filled.Notifications
                            else Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones"
                        )
                    },
                    label  = { Text("Notificaciones", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor        = GreenPrimary.copy(alpha = 0.12f),
                        selectedIconColor     = GreenPrimary,
                        selectedTextColor     = GreenPrimary,
                        unselectedIconColor   = TextGray,
                        unselectedTextColor   = TextGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick  = { selectedTab = 3; onNavigateToProfile() },
                    icon     = {
                        Icon(
                            if (selectedTab == 3) Icons.Filled.Person
                            else Icons.Outlined.Person,
                            contentDescription = "Perfil"
                        )
                    },
                    label  = { Text("Perfil", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor        = GreenPrimary.copy(alpha = 0.12f),
                        selectedIconColor     = GreenPrimary,
                        selectedTextColor     = GreenPrimary,
                        unselectedIconColor   = TextGray,
                        unselectedTextColor   = TextGray
                    )
                )
            }
        }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = "Explorar",
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1A1A1A)
                    )
                    IconButton(onClick = onOpenMap) {
                        Icon(
                            imageVector        = Icons.Default.Map,
                            contentDescription = "Ver mapa",
                            tint               = Color(0xFF1A1A1A)
                        )
                    }
                }
            }

            // ── Barra de búsqueda + filtros ────────────────────────────────
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
                        leadingIcon   = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
                        },
                        shape  = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GreenPrimary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = CardWhite,
                            focusedContainerColor   = CardWhite
                        ),
                        singleLine = true
                    )

                    // Botón filtros
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Tune,
                            contentDescription = "Filtros",
                            tint               = Color(0xFF1A1A1A)
                        )
                    }
                }
            }

            // ── Lista de tarjetas ──────────────────────────────────────────
            items(filtered) { point ->
                TouristPointCard(
                    point    = point,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ─── Tarjeta de punto turístico ───────────────────────────────────────────────

@Composable
fun TouristPointCard(
    point: TouristPoint,
    modifier: Modifier = Modifier
) {
    var liked by remember { mutableStateOf(false) }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {

            // ── Imagen + badge de categoría ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model             = point.photoUrls.firstOrNull(),
                    contentDescription = point.title,
                    contentScale      = ContentScale.Crop,
                    modifier          = Modifier.fillMaxSize()
                )

                // Badge categoría
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(categoryColor(point.category))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text      = categoryLabel(point.category),
                        fontSize  = 11.sp,
                        color     = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Contenido ──────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                // Título + verificado
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
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector        = Icons.Default.CheckCircle,
                            contentDescription = "Verificado",
                            tint               = VerifiedBlue,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }

                // Descripción
                Text(
                    text     = point.description,
                    fontSize = 13.sp,
                    color    = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Coordenadas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint               = TextGray,
                        modifier           = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text     = "${"%.4f".format(point.latitude)}, ${"%.4f".format(point.longitude)}",
                        fontSize = 12.sp,
                        color    = TextGray
                    )
                }

                Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                // Autor + fecha + acciones
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Avatar + autor + fecha
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Person,
                                contentDescription = null,
                                tint               = GreenPrimary,
                                modifier           = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text     = "Usuario • ${formatTimestamp(point.createdAt)}",
                            fontSize = 11.sp,
                            color    = TextGray
                        )
                    }

                    // Likes + fotos + compartir
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Me importa
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (liked) Icons.Filled.Favorite
                                else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Me importa",
                                tint     = if (liked) Color(0xFFE91E63) else TextGray,
                                modifier = Modifier
                                    .size(16.dp)
                            )
                            Text(
                                text     = "${point.importantVotes + if (liked) 1 else 0}",
                                fontSize = 11.sp,
                                color    = TextGray
                            )
                        }

                        // Fotos
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.PhotoLibrary,
                                contentDescription = "Fotos",
                                tint               = TextGray,
                                modifier           = Modifier.size(16.dp)
                            )
                            Text(
                                text     = "${point.photoUrls.size}",
                                fontSize = 11.sp,
                                color    = TextGray
                            )
                        }

                        // Compartir
                        Icon(
                            imageVector        = Icons.Outlined.Share,
                            contentDescription = "Compartir",
                            tint               = TextGray,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatTimestamp(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExploreScreenPreview() {
    DemoAppTheme {
        ExploreScreen()
    }
}