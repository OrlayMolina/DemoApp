package com.example.demoapp.features.users.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.demoapp.R

@Composable
fun UserDetailScreen(
    padding: PaddingValues,
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isOwnProfile by viewModel.isOwnProfile.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadUserById(userId)
    }

    if (user == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.users_loading))
        }
        return
    }

    val currentUser = user!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentUser.profilePictureUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.profile_photo_desc),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Text(
            text = currentUser.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FollowStat(
                count = followersCount,
                label = stringResource(R.string.profile_stat_followers)
            )
            FollowStat(
                count = followingCount,
                label = stringResource(R.string.profile_stat_following)
            )
        }

        if (!isOwnProfile) {
            if (isFollowing) {
                OutlinedButton(
                    onClick = { viewModel.toggleFollow() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.user_detail_unfollow))
                }
            } else {
                Button(
                    onClick = { viewModel.toggleFollow() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text(stringResource(R.string.user_detail_follow))
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.users_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = stringResource(R.string.users_info_id, currentUser.id),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = stringResource(R.string.users_info_name, currentUser.name),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = stringResource(R.string.users_info_email, currentUser.email),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Button(onClick = onNavigateBack) {
            Text(stringResource(R.string.common_back))
        }
    }
}

@Composable
private fun FollowStat(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
    Spacer(modifier = Modifier.width(0.dp))
}
