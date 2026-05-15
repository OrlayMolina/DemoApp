package com.example.demoapp.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.demoapp.R
import com.example.demoapp.domain.model.TouristPoint

private val GreenPrimary = Color(0xFF2E7D5E)
private val CardWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFF6B6B6B)
private val DividerColor = Color(0xFFE0E0E0)

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

@Composable
fun ProfileScreen(
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToAchievements: (() -> Unit)? = null,
    onNavigateToStatistics: (() -> Unit)? = null,
    onOpenPublication: ((TouristPoint) -> Unit)? = null,
    onEditPublication: ((TouristPoint) -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val myPublications by viewModel.myPublications.collectAsState()

    // Si el ViewModel no expone followers/following, usamos valores por defecto (0)
    // Puedes conectar esto luego con las propiedades reales del ViewModel/Modelo.
    val followers = 0
    val following = 0

    // Estado local para las tabs (0 = Mis publicaciones, 1 = Guardados)
    var selectedTab by remember { mutableStateOf(0) }

    val profileName = user?.name ?: stringResource(R.string.profile_default_user_name)
    val profileBio = user?.bio?.takeIf { !it.isNullOrBlank() } ?: stringResource(R.string.profile_default_bio)
    val publicCount = myPublications.filter { it.isVerified }.size
    val savedCount = user?.savedPublications?.size ?: 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row {
                    IconButton(onClick = { onNavigateToSettings?.invoke() }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.profile_settings_desc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { onLogout?.invoke() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (!user?.profilePictureUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user?.profilePictureUrl,
                                contentDescription = stringResource(R.string.profile_photo_desc),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, GreenPrimary, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(GreenPrimary)
                                    .border(2.dp, GreenPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = safeInitials(profileName),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = profileName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = profileBio,
                                fontSize = 13.sp,
                                color = TextGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = listOfNotNull(user?.city, user?.address).joinToString(" · "),
                                fontSize = 12.sp,
                                color = GreenPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(color = DividerColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = publicCount.toString(),
                            label = stringResource(R.string.profile_stat_publications)
                        )
                        StatItem(
                            value = followers.toString(),
                            label = stringResource(R.string.profile_stat_followers)
                        )
                        StatItem(
                            value = following.toString(),
                            label = stringResource(R.string.profile_stat_following)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { onNavigateToStatistics?.invoke() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.profile_button_statistics), fontSize = 13.sp)
                        }
                        androidx.compose.material3.OutlinedButton(
                            onClick = { onNavigateToAchievements?.invoke() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.profile_button_achievements), fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Tabs simples ─────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardWhite)
                    .padding(4.dp),
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
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) GreenPrimary else TextGray
                        )
                    }
                }
            }

            // Indicador tab activo
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                        .background(if (selectedTab == 0) GreenPrimary else Color.Transparent)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                        .background(if (selectedTab == 1) GreenPrimary else Color.Transparent)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Lista de publicaciones ─────────────────────────────────────
            val verifiedList = myPublications.filter { it.isVerified }
            val displayedList = if (selectedTab == 0) verifiedList.filter { !it.isSaved } else verifiedList.filter { it.isSaved }

            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) stringResource(R.string.profile_empty_publications) else stringResource(R.string.profile_empty_saved),
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayedList.forEach { point ->
                        PublicationItem(
                            point = point,
                            onOpen = { onOpenPublication?.invoke(point) },
                            onEdit = { onEditPublication?.invoke(point) }
                        )
                    }
                }
            }

            if (savedCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.profile_tab_saved) + ": $savedCount",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextGray
        )
    }
}

@Composable
private fun PublicationItem(
    point: TouristPoint,
    onOpen: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = point.photoUrls.firstOrNull(),
                contentDescription = point.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = point.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onEdit) {
                        Text(
                            text = stringResource(R.string.common_edit),
                            fontSize = 12.sp,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = point.description,
                    fontSize = 12.sp,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = GreenPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = stringResource(R.string.profile_publication_likes, point.importantVotes),
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ModeComment,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = stringResource(R.string.profile_publication_comments, point.commentCount),
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }
            }
        }
    }
}

