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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.NotificationType

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val GreenPrimary   = Color(0xFF2E7D5E)
private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val UnreadBg       = Color(0xFFEDF4F0)

// ─── Datos iniciales ──────────────────────────────────────────────────────────

private val INITIAL_NOTIFICATIONS = listOf(
    Notification(
        id               = "1",
        type             = NotificationType.LIKE,
        userName         = "Juan",
        userAvatarUrl    = "https://picsum.photos/seed/juan/100/100",
        publicationTitle = "Mural Increíble en el Centro",
        publicationImage = "https://picsum.photos/seed/mural/100/100",
        date             = "24 feb, 03:30",
        createdAt        = System.currentTimeMillis() - 86400000L * 2,
        isRead           = false,
        relatedEntityId  = "1"
    ),
    Notification(
        id               = "2",
        type             = NotificationType.COMMENT,
        userName         = "Alma",
        userAvatarUrl    = "https://picsum.photos/seed/alma/100/100",
        publicationTitle = "Parque escondido",
        publicationImage = "https://picsum.photos/seed/parque/100/100",
        date             = "25 feb, 10:42",
        createdAt        = System.currentTimeMillis() - 86400000L,
        isRead           = false,
        relatedEntityId  = "2"
    ),
    Notification(
        id               = "3",
        type             = NotificationType.FOLLOWER,
        userName         = "Carlos",
        userAvatarUrl    = "https://picsum.photos/seed/carlos/100/100",
        publicationTitle = null,
        publicationImage = null,
        date             = "22 feb, 07:20",
        createdAt        = System.currentTimeMillis() - 86400000L * 4,
        isRead           = true,
        relatedEntityId  = "user_carlos"
    ),
    Notification(
        id               = "4",
        type             = NotificationType.VERIFIED,
        userName         = "",
        userAvatarUrl    = null,
        publicationTitle = "Vista Panorámica",
        publicationImage = "https://picsum.photos/seed/panoramica/100/100",
        date             = "27 feb, 07:20",
        createdAt        = System.currentTimeMillis() - 3600000L,
        isRead           = true,
        relatedEntityId  = "3"
    )
)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    initialNotifications    : List<Notification>  = Notification.SAMPLE_LIST,
    onNavigateToPublication : ((String) -> Unit)? = null,
    onNavigateToProfile     : ((String) -> Unit)? = null
) {
    // Se reinicia al estado inicial cada vez que se monta el composable
    var notifications by remember { mutableStateOf(initialNotifications) }
    var selectedFilter by remember { mutableStateOf(0) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val unreadCount = notifications.count { !it.isRead }
    val displayed   = when (selectedFilter) {
        1    -> notifications.filter { !it.isRead }
        else -> notifications
    }.sortedByDescending { it.createdAt }

    // ── Diálogo confirmar eliminar todo ────────────────────────────────────
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title   = { Text("Eliminar notificaciones") },
            text    = { Text("¿Eliminar todas las notificaciones?") },
            confirmButton   = {
                TextButton(onClick = {
                    notifications     = emptyList()
                    showDeleteAllDialog = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancelar")
                }
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
                    text       = "Notificaciones",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A1A)
                )
                // Botón eliminar todo
                if (notifications.isNotEmpty()) {
                    IconButton(onClick = { showDeleteAllDialog = true }) {
                        Icon(
                            imageVector        = Icons.Default.Delete,
                            contentDescription = "Eliminar todas",
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
                    label    = "Todas",
                    selected = selectedFilter == 0,
                    onClick  = { selectedFilter = 0 }
                )
                FilterPill(
                    label    = if (unreadCount > 0) "No leídas ($unreadCount)" else "No leídas",
                    selected = selectedFilter == 1,
                    onClick  = { selectedFilter = 1 }
                )
                Spacer(Modifier.weight(1f))
                // Marcar todas como leídas
                if (unreadCount > 0) {
                    TextButton(
                        onClick        = {
                            notifications = notifications.map { it.copy(isRead = true) }
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text     = "Marcar todas como leídas",
                            fontSize = 11.sp,
                            color    = GreenPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Lista ──────────────────────────────────────────────────────
            if (displayed.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin notificaciones", color = TextGray, fontSize = 14.sp)
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
                            onDismiss    = {
                                notifications = notifications.filter { it.id != notif.id }
                            },
                            onClick      = {
                                // Marcar como leída al tocar
                                notifications = notifications.map {
                                    if (it.id == notif.id) it.copy(isRead = true) else it
                                }
                                // Navegar según tipo
                                notif.relatedEntityId?.let { entityId ->
                                    when (notif.type) {
                                        NotificationType.FOLLOWER ->
                                            onNavigateToProfile?.invoke(entityId)
                                        else ->
                                            onNavigateToPublication?.invoke(entityId)
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
        state            = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
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
                    contentDescription = "Eliminar",
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
    val bgColor = if (notification.isRead) CardWhite else UnreadBg

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment   = Alignment.CenterVertically
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
                // Badge del tipo
                Box(
                    modifier = Modifier
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

            // ── Punto no leído ─────────────────────────────────────────────
            if (!notification.isRead) {
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

// ─── Helpers de tipo ─────────────────────────────────────────────────────────

private fun notifTypeLabel(type: NotificationType) = when (type) {
    NotificationType.LIKE     -> "Nuevo like"
    NotificationType.COMMENT  -> "Nuevo comentario"
    NotificationType.FOLLOWER -> "Nuevo seguidor"
    NotificationType.VERIFIED -> "Publicación verificada"
}

private fun notifBody(n: Notification) = when (n.type) {
    NotificationType.LIKE     -> "A ${n.userName} le gustó tu publicación \"${n.publicationTitle}\""
    NotificationType.COMMENT  -> "${n.userName} comentó en tu publicación \"${n.publicationTitle}\""
    NotificationType.FOLLOWER -> "${n.userName} comenzó a seguirte"
    NotificationType.VERIFIED -> "Tu publicación \"${n.publicationTitle}\" ha sido verificada"
}

private fun notifTypeColor(type: NotificationType) = when (type) {
    NotificationType.LIKE     -> Color(0xFFE91E63)
    NotificationType.COMMENT  -> Color(0xFF1976D2)
    NotificationType.FOLLOWER -> Color(0xFF2E7D5E)
    NotificationType.VERIFIED -> Color(0xFF7B1FA2)
}

private fun notifTypeIcon(type: NotificationType): ImageVector = when (type) {
    NotificationType.LIKE     -> Icons.Default.Favorite
    NotificationType.COMMENT  -> Icons.Default.ModeComment
    NotificationType.FOLLOWER -> Icons.Default.Person
    NotificationType.VERIFIED -> Icons.Default.Verified
}