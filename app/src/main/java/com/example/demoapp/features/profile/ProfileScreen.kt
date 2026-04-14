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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demoapp.R
import com.example.demoapp.domain.model.TouristPoint
import coil3.compose.AsyncImage


private val GreenPrimary   = Color(0xFF2E7D5E)
private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val DividerColor   = Color(0xFFE0E0E0)

private fun safeInitials(value: String): String {
    val initials = value
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
    return initials.ifBlank { "?" }
}

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
    onLogout: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser by viewModel.user.collectAsStateWithLifecycle()
    val myPointsFromRepo by viewModel.myPublications.collectAsStateWithLifecycle()
    val followers by viewModel.followers.collectAsStateWithLifecycle(0)
    val following by viewModel.following.collectAsStateWithLifecycle(0)
    val colorScheme = MaterialTheme.colorScheme
    val defaultUserName = stringResource(R.string.profile_default_user_name)
    val joinDateText = stringResource(R.string.profile_join_status_active)
    val defaultBio = stringResource(R.string.profile_default_bio)
    val memberSinceText = stringResource(R.string.profile_member_since)

    val profileUser = ProfileUser(
        name = currentUser?.name ?: defaultUserName,
        joinDate = joinDateText,
        bio = currentUser?.bio?.ifBlank { defaultBio } ?: defaultBio,
        memberSince = memberSinceText,
        publications = myPointsFromRepo.filter { it.isVerified }.size,
        followers = followers,
        following = following
    )

    Scaffold(
        containerColor = colorScheme.background
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
                        text       = stringResource(R.string.profile_title),
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = colorScheme.onBackground
                    )
                    IconButton(onClick = { onNavigateToSettings?.invoke() }) {
                        Icon(
                            imageVector        = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.profile_settings_desc),
                            tint               = colorScheme.onBackground
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
                    colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
                                    contentDescription = stringResource(R.string.profile_photo_desc),
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
                                        text       = safeInitials(profileUser.name),
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
                            StatItem(value = profileUser.publications.toString(), label = stringResource(R.string.profile_stat_publications))
                            VerticalDivider(
                                modifier = Modifier.height(36.dp),
                                color    = DividerColor
                            )
                            StatItem(value = profileUser.followers.toString(), label = stringResource(R.string.profile_stat_followers))
                            VerticalDivider(
                                modifier = Modifier.height(36.dp),
                                color    = DividerColor
                            )
                            StatItem(value = profileUser.following.toString(), label = stringResource(R.string.profile_stat_following))
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
                            ) { Text(stringResource(R.string.profile_button_statistics), fontSize = 13.sp) }

                            OutlinedButton(
                                onClick  = { onNavigateToAchievements?.invoke() },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF1A1A1A)
                                )
                            ) { Text(stringResource(R.string.profile_button_achievements), fontSize = 13.sp) }
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
                    listOf(
                        stringResource(R.string.profile_tab_my_publications),
                        stringResource(R.string.profile_tab_saved)
                    ).forEachIndexed { index, title ->
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
             val verifiedList = sourceList.filter { it.isVerified }
             val list = if (selectedTab == 0) verifiedList else emptyList()

             if (list.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = if (selectedTab == 0) stringResource(R.string.profile_empty_publications)
                            else stringResource(R.string.profile_empty_saved),
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
                            text     = stringResource(R.string.common_edit),
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
                        Text(
                            stringResource(R.string.profile_publication_likes, point.importantVotes),
                            fontSize = 11.sp,
                            color = TextGray
                        )
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
                        Text(
                            stringResource(R.string.profile_publication_comments, point.commentCount),
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }
            }
        }
    }
}