package com.example.demoapp.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.outlined.Settings
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demoapp.domain.model.TouristPoint
import coil3.compose.AsyncImage


private val GreenPrimary   = Color(0xFF2E7D5E)
private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val DividerColor   = Color(0xFFE0E0E0)

data class ProfileUser(
    val name        : String,
    val joinDate    : String,
    val bio         : String,
    val memberSince : String,
    val publications: Int,
    val followers   : Int,
    val following   : Int
)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    myPublications: List<TouristPoint> = emptyList(),
    onEditPublication: ((TouristPoint) -> Unit)? = null,
    onOpenPublication: ((TouristPoint) -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToAchievements : (() -> Unit)? = null,
    onNavigateToStatistics   : (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser by viewModel.user.collectAsStateWithLifecycle()
    val myPointsFromRepo by viewModel.myPublications.collectAsStateWithLifecycle()

    val profileUser = ProfileUser(
        name = currentUser?.name ?: "Usuario",
        joinDate = "Activo",
        bio = currentUser?.bio?.ifBlank { "Sin biografia" } ?: "Sin biografia",
        memberSince = "Miembro de la comunidad",
        publications = myPointsFromRepo.size,
        followers = 0,
        following = 0
    )

    Scaffold(
        containerColor = BackgroundGray
    ) { padding ->

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding      = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Top bar ────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Perfil",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1A1A1A)
                    )
                    IconButton(onClick = { onNavigateToSettings?.invoke() }) {
                        Icon(
                            imageVector        = Icons.Outlined.Settings,
                            contentDescription = "Ajustes",
                            tint               = Color(0xFF1A1A1A)
                        )
                    }
                }
            }

            // ── Info usuario ───────────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // Avatar + nombre
                        Row(
                            verticalAlignment   = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (!currentUser?.profilePictureUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model              = currentUser?.profilePictureUrl,
                                    contentDescription = "Foto de perfil",
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, GreenPrimary, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier         = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(GreenPrimary)
                                        .border(2.dp, GreenPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = profileUser.name
                                            .split(" ")
                                            .take(2)
                                            .joinToString("") { it.first().uppercase() },
                                        color      = Color.White,
                                        fontSize   = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text       = profileUser.name,
                                        fontSize   = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = Color(0xFF1A1A1A)
                                    )
                                    Text(
                                        text     = profileUser.joinDate,
                                        fontSize = 11.sp,
                                        color    = TextGray
                                    )
                                }
                                Text(
                                    text     = profileUser.bio,
                                    fontSize = 13.sp,
                                    color    = TextGray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text     = profileUser.memberSince,
                                    fontSize = 12.sp,
                                    color    = GreenPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        HorizontalDivider(color = DividerColor)

                        // Estadísticas
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(value = profileUser.publications.toString(), label = "Publicaciones")
                            VerticalDivider(
                                modifier = Modifier.height(36.dp),
                                color    = DividerColor
                            )
                            StatItem(value = profileUser.followers.toString(), label = "Seguidores")
                            VerticalDivider(
                                modifier = Modifier.height(36.dp),
                                color    = DividerColor
                            )
                            StatItem(value = profileUser.following.toString(), label = "Siguiendo")
                        }

                        HorizontalDivider(color = DividerColor)

                        // Botones
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick  = { onNavigateToStatistics?.invoke() },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(10.dp),
                                border   = ButtonDefaults.outlinedButtonBorder.copy(
                                    // usa el color del borde
                                ),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF1A1A1A)
                                )
                            ) { Text("Estadísticas", fontSize = 13.sp) }

                            OutlinedButton(
                                onClick  = { onNavigateToAchievements?.invoke() },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF1A1A1A)
                                )
                            ) { Text("Logros", fontSize = 13.sp) }
                        }
                    }
                }
            }

            // ── Tabs ───────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardWhite),
                ) {
                    listOf("Mis publicaciones", "Guardados").forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = index }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = title,
                                fontSize   = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold
                                else FontWeight.Normal,
                                color      = if (selectedTab == index) GreenPrimary else TextGray
                            )
                        }
                        if (index == 0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .align(Alignment.CenterVertically)
                                    .background(DividerColor)
                            )
                        }
                    }
                }
                // Indicador tab activo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(
                                if (selectedTab == 0) GreenPrimary else Color.Transparent
                            )
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(
                                if (selectedTab == 1) GreenPrimary else Color.Transparent
                            )
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Lista de publicaciones ─────────────────────────────────────
            val sourceList = if (myPointsFromRepo.isNotEmpty()) myPointsFromRepo else myPublications
            val list = if (selectedTab == 0) sourceList else emptyList()

            if (list.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = if (selectedTab == 0) "No tienes publicaciones aún"
                            else "No tienes guardados aún",
                            color    = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(list) { point ->
                    PublicationItem(
                        point    = point,
                        onOpen   = { onOpenPublication?.invoke(point) },
                        onEdit   = { onEditPublication?.invoke(point) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ─── Composables auxiliares ───────────────────────────────────────────────────

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF1A1A1A)
        )
        Text(
            text     = label,
            fontSize = 11.sp,
            color    = TextGray
        )
    }
}

@Composable
private fun PublicationItem(
    point    : TouristPoint,
    onOpen   : () -> Unit,
    onEdit   : () -> Unit,
    modifier : Modifier = Modifier
) {
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .clickable { onOpen() },
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
                contentDescription = point.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            // Contenido
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = point.title,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF1A1A1A),
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick      = onEdit,
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        Text(
                            text     = "Editar",
                            fontSize = 12.sp,
                            color    = GreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text     = point.description,
                    fontSize = 12.sp,
                    color    = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Likes y comentarios
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Favorite,
                            contentDescription = null,
                            tint               = GreenPrimary.copy(alpha = 0.7f),
                            modifier           = Modifier.size(12.dp)
                        )
                        Text("${point.importantVotes} likes", fontSize = 11.sp, color = TextGray)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.ModeComment,
                            contentDescription = null,
                            tint               = TextGray,
                            modifier           = Modifier.size(12.dp)
                        )
                        Text("${point.commentCount} comentarios", fontSize = 11.sp, color = TextGray)
                    }
                }
            }
        }
    }
}