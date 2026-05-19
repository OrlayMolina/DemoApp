package com.example.demoapp.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.demoapp.R
import com.example.demoapp.domain.model.User
import com.example.demoapp.features.users.list.UserListViewModel

private val GreenPrimary = Color(0xFF2E7D5E)
private val DangerRed    = Color(0xFFD32F2F)
private val OrangeAccent = Color(0xFFF57C00)
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
fun ModeratorUserDetailScreen(
    userId         : String,
    onNavigateBack : () -> Unit,
    viewModel      : UserListViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val relations by viewModel.relations.collectAsStateWithLifecycle()
    val user = users.firstOrNull { it.id == userId }
    val followersCount = relations.count { it.followingId == userId }
    val followingCount = relations.count { it.followerId == userId }

    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var showBanDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
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
                        contentDescription = stringResource(R.string.moderator_back_to_users),
                        tint = TextDark
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.moderator_user_detail_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
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
                        if (user.profilePictureUrl.isNotBlank()) {
                            AsyncImage(
                                model = user.profilePictureUrl,
                                contentDescription = user.name,
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
                                    .background(GreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = safeInitials(user.name),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = user.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = user.email,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                            if (user.bio.isNotBlank()) {
                                Text(
                                    text = user.bio,
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBlock(value = followersCount.toString(), label = stringResource(R.string.profile_stat_followers))
                        StatBlock(value = followingCount.toString(), label = stringResource(R.string.profile_stat_following))
                        StatBlock(value = user.points.toString(),    label = "Puntos")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Estado de restriccion
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (user.isBanned) DangerRed.copy(alpha = 0.05f) else GreenPrimary.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (user.isBanned) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (user.isBanned) DangerRed else GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (user.isBanned)
                                stringResource(R.string.moderator_user_status_banned)
                            else
                                stringResource(R.string.moderator_user_status_active),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (user.isBanned) DangerRed else GreenPrimary
                        )
                    }
                    if (user.isBanned) {
                        Text(
                            text = stringResource(R.string.moderator_ban_history_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Text(
                            text = stringResource(
                                R.string.moderator_ban_history_reason,
                                user.banReason.ifBlank { stringResource(R.string.moderator_ban_no_reason) }
                            ),
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }
            }

            // Seccion de apelacion
            if (user.isBanned) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Forum,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                stringResource(R.string.moderator_appeal_section_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }
                        if (user.banAppeal.isBlank()) {
                            Text(
                                stringResource(R.string.moderator_appeal_empty),
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        } else {
                            Text(
                                text = user.banAppeal,
                                fontSize = 13.sp,
                                color = TextDark
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { /* sin cambios; el usuario sigue bloqueado */ },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.moderator_appeal_keep),
                                        fontSize = 12.sp,
                                        color = DangerRed
                                    )
                                }
                                Button(
                                    onClick = { viewModel.unbanUser(user.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                                ) {
                                    Text(
                                        stringResource(R.string.moderator_appeal_accept),
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Acciones principales
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (user.isBanned) {
                    Button(
                        onClick = { viewModel.unbanUser(user.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.moderator_unban_user), fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = { showBanDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Icon(Icons.Default.Block, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.moderator_ban_user), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showBanDialog) {
        ModeratorBanDialog(
            user = user,
            onDismiss = { showBanDialog = false },
            onConfirm = { reason ->
                viewModel.banUser(user.id, reason)
                showBanDialog = false
            }
        )
    }
}

@Composable
private fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text(label, fontSize = 11.sp, color = TextGray)
    }
}

@Composable
private fun ModeratorBanDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.moderator_ban_dialog_title, user.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.moderator_ban_dialog_message),
                    fontSize = 13.sp,
                    color = TextGray
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.moderator_ban_reason_label)) },
                    placeholder = { Text(stringResource(R.string.moderator_ban_reason_placeholder)) },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank()
            ) {
                Text(
                    text = stringResource(R.string.moderator_ban_confirm),
                    color = DangerRed
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
