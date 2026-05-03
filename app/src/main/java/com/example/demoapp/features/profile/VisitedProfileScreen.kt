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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil3.compose.AsyncImage
import com.example.demoapp.R
import com.example.demoapp.domain.model.TouristPoint

private val GreenPrimary   = Color(0xFF2E7D5E)
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

@Composable
fun VisitedProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit = {},
    onOpenPublication: (TouristPoint) -> Unit = {},
    viewModel: VisitedProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) { viewModel.load(userId) }
    val user by viewModel.user.collectAsState()
    val publications by viewModel.publications.collectAsState()
    val followers by viewModel.followersCount.collectAsState()
    val following by viewModel.followingCount.collectAsState()
    val isOwnProfile by viewModel.isOwnProfile.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val visitedUser = user!!
    val defaultBio = stringResource(R.string.profile_default_bio)
    val memberSinceText = stringResource(R.string.profile_member_since)
    val joinDateText = stringResource(R.string.profile_join_status_active)

    Scaffold(containerColor = colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // Top bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = colorScheme.onBackground
                        )
                    }
                    Text(
                        text = stringResource(R.string.profile_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(Modifier.width(48.dp))
                }
            }

            // Profile card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
                            if (visitedUser.profilePictureUrl.isNotBlank()) {
                                AsyncImage(
                                    model = visitedUser.profilePictureUrl,
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
                                        text = safeInitials(visitedUser.name),
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = visitedUser.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                    Text(
                                        text = joinDateText,
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                                Text(
                                    text = visitedUser.bio.ifBlank { defaultBio },
                                    fontSize = 13.sp,
                                    color = TextGray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = memberSinceText,
                                    fontSize = 12.sp,
                                    color = GreenPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        HorizontalDivider(color = DividerColor)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            VisitedStatItem(
                                value = publications.size.toString(),
                                label = stringResource(R.string.profile_stat_publications)
                            )
                            VerticalDivider(modifier = Modifier.height(36.dp), color = DividerColor)
                            VisitedStatItem(
                                value = followers.toString(),
                                label = stringResource(R.string.profile_stat_followers)
                            )
                            VerticalDivider(modifier = Modifier.height(36.dp), color = DividerColor)
                            VisitedStatItem(
                                value = following.toString(),
                                label = stringResource(R.string.profile_stat_following)
                            )
                        }

                        if (!isOwnProfile) {
                            HorizontalDivider(color = DividerColor)
                            if (isFollowing) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleFollow() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(stringResource(R.string.user_detail_unfollow))
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.toggleFollow() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                                ) {
                                    Text(
                                        text = stringResource(R.string.user_detail_follow),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Single tab header (publications only)
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardWhite)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.profile_tab_my_publications),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenPrimary
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(GreenPrimary)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (publications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.profile_empty_publications),
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(publications) { point ->
                    VisitedPublicationItem(
                        point = point,
                        onOpen = { onOpenPublication(point) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitedStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextGray
        )
    }
}

@Composable
private fun VisitedPublicationItem(
    point: TouristPoint,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                Text(
                    text = point.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = GreenPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            stringResource(R.string.profile_publication_likes, point.importantVotes),
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ModeComment,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(12.dp)
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
