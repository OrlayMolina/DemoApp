package com.example.demoapp.features.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val BackgroundGray  = Color(0xFFF5F5F5)
private val CardWhite       = Color(0xFFFFFFFF)
private val TextGray        = Color(0xFF6B6B6B)
private val TextDark        = Color(0xFF1A1A1A)
private val DividerColor    = Color(0xFFEEEEEE)
private val DangerRed       = Color(0xFFD32F2F)
private val DangerRedLight  = Color(0xFFFFEBEE)
private val BluePrimary     = Color(0xFF1A73E8)
private val GreenPrimary    = Color(0xFF2E7D5E)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun EditProfileScreen(
    onNavigateBack    : () -> Unit = {},
    onAccountDeleted  : () -> Unit = {},   // navega a Login
    onLogout          : () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    var showDeleteDialog  by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val name = viewModel.name
    val email = viewModel.email
    val bio = viewModel.bio
    val profilePictureUrl = viewModel.profilePictureUrl

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) viewModel.onProfilePictureChange(uri.toString())
    }

    val maxBio = 150

    // ── Diálogo Eliminar Cuenta ────────────────────────────────────────────
    if (showDeleteDialog) {
        DeleteAccountDialog(
            onConfirm = {
                showDeleteDialog = false
                if (viewModel.deleteCurrentAccount()) {
                    onAccountDeleted()
                }
            },
            onDismiss = { showDeleteDialog = false }
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
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = TextDark)
                }
                Text(
                    text       = "Editar Perfil",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )
                Button(
                    onClick        = {
                        val ok = viewModel.saveProfile()
                        Toast.makeText(
                            context,
                            if (ok) "Perfil guardado" else (viewModel.saveMessage ?: "No se pudo guardar"),
                            Toast.LENGTH_SHORT
                        ).show()
                        if (ok) onNavigateBack()
                    },
                    colors         = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape          = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text       = "Guardar",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Spacer(Modifier.height(4.dp))

                // ── Avatar ─────────────────────────────────────────────────
                Box(
                    modifier         = Modifier.align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (profilePictureUrl.isNotBlank()) {
                        AsyncImage(
                            model              = profilePictureUrl,
                            contentDescription = "Foto de perfil",
                            modifier           = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(3.dp, CardWhite, CircleShape)
                        )
                    } else {
                        Box(
                            modifier         = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary)
                                .border(3.dp, CardWhite, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = name.split(" ")
                                    .take(2)
                                    .joinToString("") { it.first().uppercase() },
                                color      = Color.White,
                                fontSize   = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Badge editar foto
                    Box(
                        modifier         = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(BluePrimary)
                            .border(2.dp, CardWhite, CircleShape)
                            .clickable { photoPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Text(
                    text      = "Toca el ícono para cambiar foto",
                    fontSize  = 12.sp,
                    color     = TextGray,
                    modifier  = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )

                // ── Campos ─────────────────────────────────────────────────
                Card(
                    shape  = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Nombre
                        EditField(
                            label = "Nombre",
                            value = name,
                            onValueChange = { viewModel.onNameChange(it) },
                            singleLine = true
                        )

                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Email
                        EditField(
                            label = "Email",
                            value = email,
                            onValueChange = { viewModel.onEmailChange(it) },
                            singleLine = true
                        )

                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Bio
                        EditField(
                            label         = "Bio",
                            value         = bio,
                            onValueChange = { if (it.length <= maxBio) viewModel.onBioChange(it) },
                            singleLine    = false,
                            minLines      = 3
                        )
                        Text(
                            text     = "${bio.length} / $maxBio caracteres",
                            fontSize = 11.sp,
                            color    = if (bio.length >= maxBio) DangerRed else TextGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cargar foto de perfil")
                }

                // ── Privacidad y seguridad ─────────────────────────────────
                Card(
                    shape  = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column {
                        Text(
                            text     = "Privacidad y seguridad",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color    = TextDark,
                            modifier = Modifier.padding(
                                start = 16.dp, end = 16.dp,
                                top   = 14.dp, bottom = 4.dp
                            )
                        )
                        SettingsItem(
                            label   = "Cambiar Contraseña",
                            onClick = { /* TODO */ }
                        )
                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsItem(
                            label   = "Configuración de privacidad",
                            onClick = { /* TODO */ }
                        )
                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsItem(
                            label   = "Notificaciones",
                            onClick = { /* TODO */ }
                        )
                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsItem(
                            label   = "Cerrar sesión",
                            onClick = onLogout
                        )
                    }
                }

                // ── Zona de peligro ────────────────────────────────────────
                Card(
                    shape  = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerRedLight)
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text       = "Zona de Peligro",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = DangerRed
                        )
                        Text(
                            text     = "Acciones irreversibles que afectarán permanentemente tu cuenta",
                            fontSize = 12.sp,
                            color    = DangerRed.copy(alpha = 0.8f)
                        )
                        Button(
                            onClick  = { showDeleteDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape  = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DangerRed
                            )
                        ) {
                            Icon(
                                Icons.Default.DeleteForever,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Eliminar Cuenta",
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── Diálogo eliminar cuenta ──────────────────────────────────────────────────

@Composable
private fun DeleteAccountDialog(
    onConfirm : () -> Unit,
    onDismiss : () -> Unit
) {
    var check1        by remember { mutableStateOf(false) }
    var check2        by remember { mutableStateOf(false) }
    var check3        by remember { mutableStateOf(false) }
    var confirmText   by remember { mutableStateOf("") }

    val canDelete = check1 && check2 && check3 && confirmText == "ELIMINAR"

    val DangerRed      = Color(0xFFD32F2F)
    val TextDark       = Color(0xFF1A1A1A)
    val TextGray       = Color(0xFF6B6B6B)
    val DividerColor   = Color(0xFFEEEEEE)
    val CardWhite      = Color(0xFFFFFFFF)
    val BackgroundGray = Color(0xFFF5F5F5)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardWhite,
        shape            = RoundedCornerShape(20.dp),
        properties       = DialogProperties(              // ← agrega esto
            usePlatformDefaultWidth = false
        ),
        modifier         = Modifier
            .fillMaxWidth(0.92f)                          // ← ancho controlado
            .wrapContentHeight(),
        title            = null,
        text             = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Título
                Text(
                    text       = "Eliminar Cuenta",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = DangerRed
                )

                // Qué se eliminará
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundGray)
                ) {
                    Column(
                        modifier            = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Se eliminará de forma permanente:",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                        listOf(
                            "Todas tus publicaciones y fotos (47 publicaciones)",
                            "Todos tus comentarios e interacciones",
                            "Tus logros y estadísticas",
                            "Tu perfil y toda tu información personal",
                            "Tus conexiones con otros usuarios"
                        ).forEach { item ->
                            Text("• $item", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }

                HorizontalDivider(color = DividerColor)

                // Checkboxes confirmación
                Text(
                    "Antes de continuar confirma que:",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark
                )

                CheckItem(
                    checked  = check1,
                    onChange = { check1 = it },
                    label    = "Entiendo que esta acción es permanente y no se puede deshacer"
                )
                CheckItem(
                    checked  = check2,
                    onChange = { check2 = it },
                    label    = "He guardado o respaldado toda la información que necesito"
                )
                CheckItem(
                    checked  = check3,
                    onChange = { check3 = it },
                    label    = "Acepto que todas mis publicaciones y datos serán eliminados permanentemente"
                )

                // Campo confirmar
                Text(
                    text     = "Para confirmar, escribe ELIMINAR",
                    fontSize = 13.sp,
                    color    = TextGray
                )
                OutlinedTextField(
                    value         = confirmText,
                    onValueChange = { confirmText = it.uppercase() },
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("Escribe ELIMINAR", color = TextGray) },
                    shape         = RoundedCornerShape(10.dp),
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = DangerRed,
                        unfocusedBorderColor = DividerColor
                    )
                )

                // Botón eliminar
                Button(
                    onClick  = onConfirm,
                    enabled  = canDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = DangerRed,
                        disabledContainerColor = DangerRed.copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        "Sí, eliminar mi cuenta permanentemente",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Botón cancelar
                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Text("No, mantener mi cuenta", fontSize = 13.sp, color = TextDark)
                }

                // Contactar soporte
                TextButton(
                    onClick  = { /* TODO */ },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¿Tienes problemas con tu cuenta?", fontSize = 11.sp, color = TextGray)
                        Text("Contactar soporte", fontSize = 12.sp, color = DangerRed)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

// ─── Composables auxiliares ───────────────────────────────────────────────────

@Composable
private fun EditField(
    label         : String,
    value         : String,
    onValueChange : (String) -> Unit,
    singleLine    : Boolean = true,
    minLines      : Int     = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF6B6B6B))
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = singleLine,
            minLines      = minLines,
            shape         = RoundedCornerShape(10.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color(0xFF1A73E8),
                unfocusedBorderColor    = Color(0xFFE0E0E0),
                unfocusedContainerColor = Color(0xFFF8F8F8),
                focusedContainerColor   = Color(0xFFFFFFFF)
            )
        )
    }
}

@Composable
private fun CheckItem(
    checked  : Boolean,
    onChange : (Boolean) -> Unit,
    label    : String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier          = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked         = checked,
            onCheckedChange = onChange,
            modifier        = Modifier.size(20.dp),
            colors          = CheckboxDefaults.colors(
                checkedColor = Color(0xFFD32F2F)
            )
        )
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 12.sp, color = Color(0xFF1A1A1A), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SettingsItem(
    label   : String,
    onClick : () -> Unit
) {
    TextButton(
        onClick        = onClick,
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text     = label,
            fontSize = 14.sp,
            color    = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint     = Color(0xFF6B6B6B),
            modifier = Modifier.size(18.dp)
        )
    }
}