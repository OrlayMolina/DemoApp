package com.example.demoapp.features.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.demoapp.domain.model.Comment
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory
import com.example.demoapp.features.comments.CommentsScreen
import java.text.SimpleDateFormat
import java.util.*

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val GreenPrimary   = Color(0xFF2E7D5E)
private val GreenEmerald   = Color(0xFF00897B)
private val BackgroundGray = Color(0xFFF5F5F5)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val TextDark       = Color(0xFF1A1A1A)
private val DangerRed      = Color(0xFFD32F2F)
private val VerifiedBlue   = Color(0xFF1DA1F2)

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun categoryLabel(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.NATURE        -> "Naturaleza"
    TouristPointCategory.GASTRONOMY    -> "Gastronomía"
    TouristPointCategory.CULTURE       -> "Cultura"
    TouristPointCategory.ENTERTAINMENT -> "Arte Urbano"
    TouristPointCategory.HISTORY       -> "Historia"
    else                               -> "Otro"
}

private fun categoryColor(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.NATURE        -> Color(0xFF388E3C)
    TouristPointCategory.GASTRONOMY    -> Color(0xFFE64A19)
    TouristPointCategory.CULTURE       -> Color(0xFF7B1FA2)
    TouristPointCategory.ENTERTAINMENT -> Color(0xFF1976D2)
    TouristPointCategory.HISTORY       -> Color(0xFF5D4037)
    else                               -> Color(0xFF455A64)
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es")).format(Date(millis))

private fun formatShortDate(millis: Long): String =
    SimpleDateFormat("d/M/yyyy", Locale("es")).format(Date(millis))

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun TouristPointDetailScreen(
    point          : TouristPoint,
    isModerator    : Boolean       = false,
    onNavigateBack : () -> Unit    = {},
    onApproved     : () -> Unit    = {},
    onRejected     : () -> Unit    = {},
    viewModel      : TouristPointDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(point) { viewModel.loadPoint(point) }

    var showRejectDialog   by remember { mutableStateOf(false) }
    var showAllComments    by remember { mutableStateOf(false) }
    var showCommentsScreen by remember { mutableStateOf(false) }

    if (showCommentsScreen) {
        CommentsScreen(
            pointId = point.id,
            pointTitle = point.title,
            onNavigateBack = { showCommentsScreen = false }
        )
        return
    }

    // ── Modal rechazo ──────────────────────────────────────────────────────
    if (showRejectDialog) {
        RejectDialog(
            onConfirm = { reason ->
                val rejected = viewModel.rejectPoint(reason)
                showRejectDialog = false
                if (rejected) onRejected()
            },
            onDismiss = { showRejectDialog = false }
        )
    }

    Scaffold(
        containerColor = BackgroundGray,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Imagen + header flotante ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AsyncImage(
                    model              = point.photoUrls.firstOrNull(),
                    contentDescription = point.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )

                // Gradiente superior para los botones
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )

                // Botones superiores
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                    if (!isModerator) {
                        IconButton(onClick = { /* TODO: compartir */ }) {
                            Icon(Icons.Default.Share, "Compartir", tint = Color.White)
                        }
                    }
                }

                // Badge categoría
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 52.dp, end = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(categoryColor(point.category))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        categoryLabel(point.category),
                        fontSize   = 11.sp,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Contenido ──────────────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .background(CardWhite)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Badge categoría + título
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(categoryColor(point.category).copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        categoryLabel(point.category),
                        fontSize = 11.sp,
                        color    = categoryColor(point.category),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text       = point.title,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDark,
                        modifier   = Modifier.weight(1f)
                    )
                    if (point.isVerified) {
                        Icon(
                            Icons.Default.CheckCircle,
                            "Verificado",
                            tint     = VerifiedBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text     = point.description,
                    fontSize = 14.sp,
                    color    = TextGray,
                    lineHeight = 20.sp
                )

                // Ubicación
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = TextGray, modifier = Modifier.size(16.dp))
                    Text(
                        "${"%.4f".format(point.latitude)}, ${"%.4f".format(point.longitude)}",
                        fontSize = 13.sp,
                        color    = TextGray
                    )
                }

                // Fecha
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = TextGray, modifier = Modifier.size(16.dp))
                    Text(formatDate(point.createdAt), fontSize = 13.sp, color = TextGray)
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Autor + seguir
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("MG", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("María García", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            Text("• ${formatShortDate(point.createdAt)}", fontSize = 11.sp, color = TextGray)
                        }
                    }

                    if (!isModerator) {
                        Button(
                            onClick  = { viewModel.toggleFollow() },
                            shape    = RoundedCornerShape(20.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.isFollowing) Color(0xFFE0E0E0)
                                else GreenPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier       = Modifier.height(32.dp)
                        ) {
                            Text(
                                if (viewModel.isFollowing) "Siguiendo" else "Seguir",
                                fontSize = 12.sp,
                                color    = if (viewModel.isFollowing) TextDark else Color.White
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Likes + comentarios
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier              = Modifier.clickable { viewModel.toggleLike() }
                    ) {
                        val likeColor by animateColorAsState(
                            if (viewModel.isLiked) Color(0xFFE91E63) else TextGray,
                            label = "like_color"
                        )
                        Icon(
                            if (viewModel.isLiked) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
                            null,
                            tint     = likeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "${point.importantVotes + if (viewModel.isLiked) 1 else 0}",
                            fontSize = 14.sp,
                            color    = TextGray
                        )
                    }
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { showCommentsScreen = true }
                    ) {
                        Icon(Icons.Outlined.ModeComment, null, tint = TextGray, modifier = Modifier.size(20.dp))
                        Text("${point.commentCount}", fontSize = 14.sp, color = TextGray)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Sección moderador: info autor + criterios ──────────────────
            if (isModerator) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // Info del autor
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape  = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite)
                    ) {
                        Column(
                            modifier            = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, null, tint = GreenEmerald, modifier = Modifier.size(16.dp))
                                Text("Información del Autor", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            }
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier         = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(GreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("MG", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("María García", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                    Text("maria@example.com", fontSize = 12.sp, color = TextGray)
                                }
                                Spacer(Modifier.weight(1f))
                                Text("47 publicaciones", fontSize = 11.sp, color = TextGray)
                            }
                        }
                    }

                    // Criterios de revisión
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape  = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite)
                    ) {
                        Column(
                            modifier            = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Criterios de Revisión",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextDark
                            )
                            listOf(
                                "El contenido es apropiado y no viola las normas",
                                "Las imágenes son de buena calidad",
                                "La ubicación es precisa",
                                "La descripción es clara y útil"
                            ).forEach { label ->
                                Row(
                                    verticalAlignment     = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 5.dp)
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(GreenEmerald)
                                    )
                                    Text(label, fontSize = 13.sp, color = TextDark)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }

             // ── Comentarios ────────────────────────────────────────────────
             if (viewModel.comments.isNotEmpty()) {
                 Column(
                     modifier            = Modifier
                         .fillMaxWidth()
                         .background(CardWhite)
                         .padding(16.dp),
                     verticalArrangement = Arrangement.spacedBy(12.dp)
                 ) {
                     Text("Comentarios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)

                     val visibleComments = if (showAllComments) viewModel.comments
                     else viewModel.comments.take(2)

                     visibleComments.forEach { comment ->
                         CommentItem(comment = comment)
                     }

                     if (viewModel.comments.size > 2 && !showAllComments) {
                         TextButton(
                             onClick  = { showAllComments = true },
                             modifier = Modifier.align(Alignment.CenterHorizontally)
                         ) {
                             Text(
                                 "Ver todos los comentarios (${viewModel.comments.size})",
                                 color    = GreenPrimary,
                                 fontSize = 13.sp
                             )
                         }
                     }
                 }
             }

            // ── Botones moderador AL FINAL del scroll ──────────────────
            if (isModerator) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .background(CardWhite)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = { showRejectDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DangerRed
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(DangerRed, DangerRed))
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rechazar", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick  = {
                            val approved = viewModel.approvePoint()
                            if (approved) onApproved()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenEmerald
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Aprobar", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Item de comentario ───────────────────────────────────────────────────────

@Composable
private fun CommentItem(comment: Comment) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D5E)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = comment.authorName.split(" ").take(2).joinToString("") { it.first().uppercase() },
                color = Color.White,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(comment.authorName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                Text(formatShortDate(comment.createdAt), fontSize = 11.sp, color = Color(0xFF6B6B6B))
            }
            Text(comment.text, fontSize = 13.sp, color = Color(0xFF1A1A1A))
        }
    }
}

// ─── Modal de rechazo ─────────────────────────────────────────────────────────

@Composable
private fun RejectDialog(
    onConfirm : (String) -> Unit,
    onDismiss : () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    val commonReasons = listOf(
        "Contenido inapropiado", "Ubicación incorrecta",
        "Imagen de baja calidad", "Spam", "Información incompleta"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Motivo del Rechazo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF6B6B6B))
                    }
                }

                Text(
                    "Por favor indica por qué se rechaza esta publicación. El usuario recibirá esta información.",
                    fontSize = 13.sp,
                    color    = Color(0xFF6B6B6B)
                )

                OutlinedTextField(
                    value         = reason,
                    onValueChange = { reason = it },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Motivo *") },
                    placeholder   = { Text("Ej: La imagen no es clara, la ubicación es incorrecta, contenido inapropiado...", color = Color(0xFF6B6B6B)) },
                    shape         = RoundedCornerShape(10.dp),
                    minLines      = 2,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = DangerRed,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                // Razones comunes
                Text("Razones comunes:", fontSize = 12.sp, color = Color(0xFF6B6B6B))
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                ) {
                    commonReasons.forEach { r ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF0F0F0))
                                .clickable { reason = r }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(r, fontSize = 12.sp, color = Color(0xFF1A1A1A))
                        }
                    }
                }

                Button(
                    onClick  = { if (reason.isNotBlank()) onConfirm(reason) },
                    enabled  = reason.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = DangerRed,
                        disabledContainerColor = DangerRed.copy(alpha = 0.35f)
                    )
                ) {
                    Text("Confirmar Rechazo", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar", color = Color(0xFF1A1A1A))
                }
            }
        }
    }
}