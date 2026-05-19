package com.example.demoapp.features.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.demoapp.R
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.NotificationType

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val GreenPrimary   = Color(0xFF2E7D5E)
private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val UnreadBg       = Color(0xFFEDF4F0)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel               : NotificationsViewModel    = hiltViewModel(),
    onNavigateToPublication : ((String) -> Unit)? = null,
    onNavigateToProfile     : ((String) -> Unit)? = null
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val currentUser   by viewModel.currentUser.collectAsStateWithLifecycle()

    var selectedFilter      by remember { mutableStateOf(0) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showAppealDialog    by remember { mutableStateOf(false) }

    // Espera un poco antes de marcar como leidas para que el usuario perciba la
    // transicion visual de "no leida" -> "leida".
    LaunchedEffect(notifications) {
        if (notifications.any { !it.isRead }) {
            kotlinx.coroutines.delay(1800)
            viewModel.markAllAsRead()
        }
    }

    val displayed = when (selectedFilter) {
        1    -> notifications.filter { !it.isRead }
        else -> notifications
    }.sortedByDescending { it.createdAt }
    val pendingCount = notifications.count { !it.isRead }

    // ── Diálogo confirmar eliminar todo ────────────────────────────────────
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title   = { Text(stringResource(R.string.notifications_delete_all_title)) },
            text    = { Text(stringResource(R.string.notifications_delete_all_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAll()
                    showDeleteAllDialog = false
                }) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // ── Diálogo de apelación al moderador (usuario bloqueado) ──────────────
    if (showAppealDialog) {
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val user = currentUser
        BanAppealDialog(
            banReason = user?.banReason.orEmpty(),
            existingAppeal = user?.banAppeal.orEmpty(),
            onDismiss = { showAppealDialog = false },
            onSubmit  = { appeal ->
                val result = viewModel.submitBanAppeal(appeal)
                result.fold(
                    onSuccess = {
                        android.widget.Toast.makeText(ctx, ctx.getString(R.string.ban_appeal_sent), android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        android.widget.Toast.makeText(ctx, it.message ?: "Error", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
                showAppealDialog = false
            }
        )
    }

    Scaffold(containerColor = BackgroundGray) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = stringResource(R.string.notifications_title),
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A1A)
                )
                if (notifications.isNotEmpty()) {
                    IconButton(onClick = { showDeleteAllDialog = true }) {
                        Icon(
                            imageVector        = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.notifications_delete_all_desc),
                            tint               = TextGray
                        )
                    }
                }
            }

            // ── Filtros ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                FilterPill(
                    label    = stringResource(R.string.notifications_filter_all),
                    selected = selectedFilter == 0,
                    onClick  = { selectedFilter = 0 }
                )
                FilterPill(
                    label    = if (pendingCount > 0) {
                        stringResource(R.string.notifications_filter_unread_count, pendingCount)
                    } else {
                        stringResource(R.string.notifications_filter_unread)
                    },
                    selected = selectedFilter == 1,
                    onClick  = { selectedFilter = 1 }
                )
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            // ── Lista ──────────────────────────────────────────────────────
            if (displayed.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.notifications_empty), color = TextGray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = displayed,
                        key   = { it.id }
                    ) { notif ->
                        SwipeToDismissNotification(
                            notification = notif,
                            onDismiss    = { viewModel.delete(notif.id) },
                            onClick      = {
                                when (notif.type) {
                                    NotificationType.BAN -> {
                                        if (currentUser?.id == notif.relatedEntityId) {
                                            showAppealDialog = true
                                        }
                                    }
                                    NotificationType.BAN_APPEAL -> {
                                        notif.relatedEntityId?.let { onNavigateToProfile?.invoke(it) }
                                    }
                                    NotificationType.FOLLOWER -> {
                                        notif.relatedEntityId?.let { onNavigateToProfile?.invoke(it) }
                                    }
                                    else -> {
                                        notif.relatedEntityId?.let { onNavigateToPublication?.invoke(it) }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Swipe to dismiss ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissNotification(
    notification : Notification,
    onDismiss    : () -> Unit,
    onClick      : () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss(); true
            } else false
        },
        positionalThreshold = { it * 0.4f }
    )

    SwipeToDismissBox(
        state                       = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent           = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    Color(0xFFE53935) else Color(0xFFFFCDD2),
                label = "swipe_bg"
            )
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector        = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint               = Color.White,
                    modifier           = Modifier.size(22.dp)
                )
            }
        }
    ) {
        NotificationItem(
            notification = notification,
            onClick      = onClick
        )
    }
}

// ─── Item de notificación ─────────────────────────────────────────────────────

@Composable
private fun NotificationItem(
    notification : Notification,
    onClick      : () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (notification.isRead) CardWhite else UnreadBg,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
        label = "notification_bg"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {

            // ── Avatar / icono de tipo ─────────────────────────────────────
            Box(contentAlignment = Alignment.BottomEnd) {
                if (notification.userAvatarUrl != null) {
                    AsyncImage(
                        model              = notification.userAvatarUrl,
                        contentDescription = notification.userName,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                    )
                } else {
                    Box(
                        modifier         = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Verified,
                            contentDescription = null,
                            tint               = GreenPrimary,
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                }
                Box(
                    modifier         = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(notifTypeColor(notification.type)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = notifTypeIcon(notification.type),
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(11.dp)
                    )
                }
            }

            // ── Texto ──────────────────────────────────────────────────────
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text       = notifTypeLabel(notification.type),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF1A1A1A)
                )
                Text(
                    text     = notifBody(notification),
                    fontSize = 12.sp,
                    color    = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text     = notification.date,
                    fontSize = 11.sp,
                    color    = TextGray.copy(alpha = 0.7f)
                )
            }

            // ── Thumbnail publicación ──────────────────────────────────────
            if (notification.publicationImage != null) {
                AsyncImage(
                    model              = notification.publicationImage,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            // ── Punto no leido ─────────────────────────────────────────────
            androidx.compose.animation.AnimatedVisibility(visible = !notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary)
                )
            }
        }
    }
}

// ─── Pill de filtro ───────────────────────────────────────────────────────────

@Composable
private fun FilterPill(
    label    : String,
    selected : Boolean,
    onClick  : () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFF1A1A1A) else CardWhite)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text       = label,
            fontSize   = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (selected) Color.White else TextGray
        )
    }
}

// ─── Helpers de tipo ──────────────────────────────────────────────────────────

@Composable
private fun notifTypeLabel(type: NotificationType) = when (type) {
    NotificationType.LIKE              -> stringResource(R.string.notifications_type_like)
    NotificationType.COMMENT           -> stringResource(R.string.notifications_type_comment)
    NotificationType.FOLLOWER          -> stringResource(R.string.notifications_type_follower)
    NotificationType.VERIFIED          -> stringResource(R.string.notifications_type_verified)
    NotificationType.NEW_PUBLICATION   -> stringResource(R.string.notifications_type_new_publication)
    NotificationType.REJECTED          -> stringResource(R.string.notifications_type_rejected)
    NotificationType.REVIEW_REMINDER   -> stringResource(R.string.notifications_type_review_reminder)
    NotificationType.ACHIEVEMENT       -> stringResource(R.string.notifications_type_achievement)
    NotificationType.BAN               -> stringResource(R.string.notifications_type_ban)
    NotificationType.BAN_APPEAL        -> stringResource(R.string.notifications_type_ban_appeal)
    NotificationType.BAN_LIFTED        -> stringResource(R.string.notifications_type_ban_lifted)
}

@Composable
private fun notifBody(n: Notification) = when (n.type) {
    NotificationType.LIKE ->
        stringResource(R.string.notifications_body_like, n.userName, n.publicationTitle.orEmpty())
    NotificationType.COMMENT ->
        stringResource(R.string.notifications_body_comment, n.userName, n.publicationTitle.orEmpty())
    NotificationType.FOLLOWER ->
        stringResource(R.string.notifications_body_follower, n.userName)
    NotificationType.VERIFIED ->
        stringResource(R.string.notifications_body_verified, n.publicationTitle.orEmpty())
    NotificationType.NEW_PUBLICATION ->
        stringResource(R.string.notifications_body_new_publication, n.userName, n.publicationTitle.orEmpty())
    NotificationType.REJECTED ->
        stringResource(R.string.notifications_body_rejected, n.publicationTitle.orEmpty())
    NotificationType.REVIEW_REMINDER ->
        stringResource(R.string.notifications_body_review_reminder)
    NotificationType.ACHIEVEMENT ->
        stringResource(R.string.notifications_body_achievement, n.publicationTitle.orEmpty())
    NotificationType.BAN ->
        stringResource(R.string.notifications_body_ban, n.publicationTitle.orEmpty())
    NotificationType.BAN_APPEAL ->
        stringResource(R.string.notifications_body_ban_appeal, n.userName)
    NotificationType.BAN_LIFTED ->
        stringResource(R.string.notifications_body_ban_lifted)
}

private fun notifTypeColor(type: NotificationType) = when (type) {
    NotificationType.LIKE              -> Color(0xFFE91E63)
    NotificationType.COMMENT           -> Color(0xFF1976D2)
    NotificationType.FOLLOWER          -> Color(0xFF2E7D5E)
    NotificationType.VERIFIED          -> Color(0xFF7B1FA2)
    NotificationType.NEW_PUBLICATION   -> Color(0xFF00897B)
    NotificationType.REJECTED          -> Color(0xFFD32F2F)
    NotificationType.REVIEW_REMINDER   -> Color(0xFFF57C00)
    NotificationType.ACHIEVEMENT       -> Color(0xFFFFB300)
    NotificationType.BAN               -> Color(0xFFD32F2F)
    NotificationType.BAN_APPEAL        -> Color(0xFF6A1B9A)
    NotificationType.BAN_LIFTED        -> Color(0xFF2E7D5E)
}

private fun notifTypeIcon(type: NotificationType): ImageVector = when (type) {
    NotificationType.LIKE              -> Icons.Default.Favorite
    NotificationType.COMMENT           -> Icons.Default.ModeComment
    NotificationType.FOLLOWER          -> Icons.Default.Person
    NotificationType.VERIFIED          -> Icons.Default.Verified
    NotificationType.NEW_PUBLICATION   -> Icons.Default.PostAdd
    NotificationType.REJECTED          -> Icons.Default.Cancel
    NotificationType.REVIEW_REMINDER   -> Icons.Default.NotificationsActive
    NotificationType.ACHIEVEMENT       -> Icons.Default.EmojiEvents
    NotificationType.BAN               -> Icons.Default.Block
    NotificationType.BAN_APPEAL        -> Icons.Default.Forum
    NotificationType.BAN_LIFTED        -> Icons.Default.LockOpen
}

@Composable
private fun BanAppealDialog(
    banReason     : String,
    existingAppeal: String,
    onDismiss     : () -> Unit,
    onSubmit      : (String) -> Unit
) {
    var appeal by remember { mutableStateOf(existingAppeal) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ban_appeal_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.ban_appeal_reason_label),
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = banReason.ifBlank { stringResource(R.string.moderator_ban_no_reason) },
                    fontSize = 13.sp,
                    color = Color(0xFFD32F2F)
                )
                if (existingAppeal.isNotBlank()) {
                    Text(
                        stringResource(R.string.ban_appeal_already_sent),
                        fontSize = 12.sp,
                        color = Color(0xFFF57C00)
                    )
                }
                OutlinedTextField(
                    value = appeal,
                    onValueChange = { appeal = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.ban_appeal_response_label)) },
                    placeholder = { Text(stringResource(R.string.ban_appeal_response_placeholder)) },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(appeal) },
                enabled = appeal.isNotBlank()
            ) {
                Text(stringResource(R.string.ban_appeal_send), color = GreenPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}