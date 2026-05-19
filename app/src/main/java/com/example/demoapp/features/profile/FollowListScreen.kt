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
import coil3.compose.AsyncImage
import com.example.demoapp.R
import com.example.demoapp.domain.model.User

enum class FollowListMode { FOLLOWERS, FOLLOWING }

private val GreenPrimary = Color(0xFF2E7D5E)
private val TextDark     = Color(0xFF1A1A1A)
private val TextGray     = Color(0xFF6B6B6B)
private val CardWhite    = Color(0xFFFFFFFF)

private fun safeInitials(value: String): String {
    val initials = value.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
    return initials.ifBlank { "?" }
}

@Composable
fun FollowListScreen(
    userId         : String,
    mode           : FollowListMode,
    onNavigateBack : () -> Unit,
    onOpenUser     : (String) -> Unit,
    viewModel      : FollowListViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val relations by viewModel.relations.collectAsStateWithLifecycle()

    val targetIds = when (mode) {
        FollowListMode.FOLLOWERS -> relations.filter { it.followingId == userId }.map { it.followerId }
        FollowListMode.FOLLOWING -> relations.filter { it.followerId == userId }.map { it.followingId }
    }.toSet()
    val targets = users.filter { it.id in targetIds }

    Scaffold(containerColor = Color(0xFFF5F5F5)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = TextDark
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        if (mode == FollowListMode.FOLLOWERS) R.string.profile_stat_followers
                        else R.string.profile_stat_following
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = targets.size.toString(),
                    fontSize = 14.sp,
                    color = TextGray,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            if (targets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            if (mode == FollowListMode.FOLLOWERS) R.string.follow_list_no_followers
                            else R.string.follow_list_no_following
                        ),
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(targets, key = { it.id }) { user ->
                        FollowListItem(
                            user = user,
                            onClick = { onOpenUser(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowListItem(
    user    : User,
    onClick : () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (user.profilePictureUrl.isNotBlank()) {
                AsyncImage(
                    model = user.profilePictureUrl,
                    contentDescription = user.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, GreenPrimary, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = safeInitials(user.name),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.bio.isNotBlank()) {
                    Text(
                        text = user.bio,
                        fontSize = 12.sp,
                        color = TextGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
