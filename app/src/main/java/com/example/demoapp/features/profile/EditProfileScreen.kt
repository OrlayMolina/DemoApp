package com.example.demoapp.features.profile

import android.widget.Toast
import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.ui.res.stringResource
import com.example.demoapp.R

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

private fun createTempImageUri(context: Context): android.net.Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.cacheDir
    val image = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image)
}

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun EditProfileScreen(
    onNavigateBack    : () -> Unit = {},
    onAccountDeleted  : () -> Unit = {},   // navega a Login
    onLogout          : () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    var showDeleteDialog  by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val name = viewModel.name
    val email = viewModel.email
    val bio = viewModel.bio
    val profilePictureUrl = viewModel.profilePictureUrl
    val darkModeEnabled = viewModel.darkModeEnabled
    val isUploadingPhoto = viewModel.isUploadingPhoto
    val photoUploadError = viewModel.photoUploadError

    LaunchedEffect(photoUploadError) {
        photoUploadError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    var showPhotoDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) viewModel.uploadProfilePicture(context, uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.uploadProfilePicture(context, android.net.Uri.parse(tempCameraUri!!))
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri.toString()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, context.getString(R.string.permission_camera_denied), Toast.LENGTH_SHORT).show()
        }
    }

    val maxBio = 150
    val colorScheme = MaterialTheme.colorScheme

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

    Scaffold(containerColor = colorScheme.background) { padding ->
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
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.common_back), tint = colorScheme.onBackground)
                }
                Text(
                    text       = stringResource(R.string.profile_edit_title),
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colorScheme.onBackground
                )
                Button(
                    onClick        = {
                        val ok = viewModel.saveProfile()
                        Toast.makeText(
                            context,
                            if (ok) context.getString(R.string.profile_edit_saved)
                            else (viewModel.saveMessage ?: context.getString(R.string.profile_edit_save_failed)),
                            Toast.LENGTH_SHORT
                        ).show()
                        if (ok) onNavigateBack()
                    },
                    colors         = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape          = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text       = stringResource(R.string.profile_edit_save),
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
                            contentDescription = stringResource(R.string.profile_photo_desc),
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
                                text       = safeInitials(name),
                                color      = Color.White,
                                fontSize   = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isUploadingPhoto) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
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
                                            .clickable(enabled = !isUploadingPhoto) { showPhotoDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                                    // Dialog para elegir cámara o galería
                                    if (showPhotoDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showPhotoDialog = false },
                                            title = { Text(stringResource(R.string.profile_edit_change_photo_hint)) },
                                            text = {
                                                Column {
                                                    TextButton(onClick = {
                                                        showPhotoDialog = false
                                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                    }) { Text(stringResource(R.string.profile_take_photo)) }
                                                    TextButton(onClick = {
                                                        showPhotoDialog = false
                                                        photoPicker.launch("image/*")
                                                    }) { Text(stringResource(R.string.profile_choose_from_gallery)) }
                                                }
                                            },
                                            confirmButton = {},
                                            dismissButton = {}
                                        )
                                    }
                }

                Text(
                    text      = stringResource(R.string.profile_edit_change_photo_hint),
                    fontSize  = 12.sp,
                    color     = TextGray,
                    modifier  = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )

                // ── Campos ─────────────────────────────────────────────────
                Card(
                    shape  = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Nombre
                        EditField(
                            label = stringResource(R.string.profile_edit_field_name),
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
                            label = stringResource(R.string.login_email_label),
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
                            label         = stringResource(R.string.profile_edit_field_bio),
                            value         = bio,
                            onValueChange = { if (it.length <= maxBio) viewModel.onBioChange(it) },
                            singleLine    = false,
                            minLines      = 3
                        )
                        Text(
                            text     = stringResource(R.string.profile_edit_bio_counter, bio.length, maxBio),
                            fontSize = 11.sp,
                            color    = if (bio.length >= maxBio) DangerRed else TextGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }


                // ── Privacidad y seguridad ─────────────────────────────────
                Card(
                    shape  = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
                ) {
                    Column {
                        Text(
                            text     = stringResource(R.string.profile_edit_privacy_security),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color    = colorScheme.onSurface,
                            modifier = Modifier.padding(
                                start = 16.dp, end = 16.dp,
                                top   = 14.dp, bottom = 4.dp
                            )
                        )
                        SettingsItem(
                            label   = stringResource(R.string.profile_edit_change_password),
                            onClick = { /* TODO */ }
                        )
                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsItem(
                            label   = stringResource(R.string.profile_edit_privacy_settings),
                            onClick = { /* TODO */ }
                        )
                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsSwitchItem(
                            label = stringResource(R.string.profile_edit_dark_theme),
                            checked = darkModeEnabled,
                            onCheckedChange = viewModel::onDarkModeEnabledChange
                        )
                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsItem(
                            label   = stringResource(R.string.profile_edit_notifications),
                            onClick = { /* TODO */ }
                        )
                        HorizontalDivider(
                            color    = DividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsItem(
                            label   = stringResource(R.string.profile_edit_logout),
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
                            text       = stringResource(R.string.profile_edit_danger_zone_title),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = DangerRed
                        )
                        Text(
                            text     = stringResource(R.string.profile_edit_danger_zone_desc),
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
                                stringResource(R.string.profile_edit_delete_account),
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

    val deleteKeyword = stringResource(R.string.profile_edit_delete_keyword)
    val canDelete = check1 && check2 && check3 && confirmText == deleteKeyword

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
                    text       = stringResource(R.string.profile_edit_delete_dialog_title),
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
                            stringResource(R.string.profile_edit_delete_dialog_permanent_title),
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                        listOf(
                            stringResource(R.string.profile_edit_delete_item_1),
                            stringResource(R.string.profile_edit_delete_item_2),
                            stringResource(R.string.profile_edit_delete_item_3),
                            stringResource(R.string.profile_edit_delete_item_4),
                            stringResource(R.string.profile_edit_delete_item_5)
                        ).forEach { item ->
                            Text("• $item", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }

                HorizontalDivider(color = DividerColor)

                // Checkboxes confirmación
                Text(
                    stringResource(R.string.profile_edit_delete_confirm_title),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextDark
                )

                CheckItem(
                    checked  = check1,
                    onChange = { check1 = it },
                    label    = stringResource(R.string.profile_edit_delete_check_1)
                )
                CheckItem(
                    checked  = check2,
                    onChange = { check2 = it },
                    label    = stringResource(R.string.profile_edit_delete_check_2)
                )
                CheckItem(
                    checked  = check3,
                    onChange = { check3 = it },
                    label    = stringResource(R.string.profile_edit_delete_check_3)
                )

                // Campo confirmar
                Text(
                    text     = stringResource(R.string.profile_edit_delete_type_to_confirm, deleteKeyword),
                    fontSize = 13.sp,
                    color    = TextGray
                )
                OutlinedTextField(
                    value         = confirmText,
                    onValueChange = { confirmText = it.uppercase() },
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text(stringResource(R.string.profile_edit_delete_placeholder, deleteKeyword), color = TextGray) },
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
                        stringResource(R.string.profile_edit_delete_confirm_button),
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
                    Text(stringResource(R.string.profile_edit_delete_cancel_button), fontSize = 13.sp, color = TextDark)
                }

                // Contactar soporte
                TextButton(
                    onClick  = { /* TODO */ },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.profile_edit_support_question), fontSize = 11.sp, color = TextGray)
                        Text(stringResource(R.string.profile_edit_support_action), fontSize = 12.sp, color = DangerRed)
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
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
