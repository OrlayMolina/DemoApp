package com.example.demoapp.features.publish

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import coil3.compose.AsyncImage
import com.example.demoapp.R
import com.example.demoapp.domain.model.TouristPointCategory

// ─── Paleta compartida (internal para el paquete publish) ────────────────────

internal val BluePrimary    = Color(0xFF1A73E8)
internal val BackgroundGray = Color(0xFFF5F5F5)
internal val CardWhite      = Color(0xFFFFFFFF)
internal val TextGray       = Color(0xFF6B6B6B)
internal val DividerColor   = Color(0xFFE0E0E0)
internal val TextDark       = Color(0xFF1A1A1A)

internal fun categoryLabel(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.NATURE        -> "Naturaleza"
    TouristPointCategory.GASTRONOMY    -> "Gastronomía"
    TouristPointCategory.CULTURE       -> "Cultura"
    TouristPointCategory.ENTERTAINMENT -> "Arte Urbano"
    else                               -> "Otro"
}

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePointStep1Screen(
    photoUrls     : List<String>,
    title         : String,
    category      : TouristPointCategory?,
    description   : String,
    isEditing     : Boolean = false,
    onAddPhoto    : (String) -> Unit,
    onRemovePhoto : (String) -> Unit,
    onTitle       : (String) -> Unit,
    onCategory    : (TouristPointCategory) -> Unit,
    onDescription : (String) -> Unit,
    onNext        : () -> Unit,
    onCancel      : () -> Unit
) {
    var showCategoryMenu by remember { mutableStateOf(false) }

    // Permite seleccionar varias fotos de una vez
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            onAddPhoto(uri.toString())
        }
    }

    val isFormValid = title.isNotBlank() && category != null

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
                    text       = if (isEditing) stringResource(R.string.create_edit_publication)
                    else stringResource(R.string.create_new_publication),
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )
                Text(
                    text     = stringResource(R.string.common_cancel),
                    fontSize = 14.sp,
                    color    = TextGray,
                    modifier = Modifier.clickable { onCancel() }
                )
            }

            // ── Progreso ───────────────────────────────────────────────────
            LinearProgressIndicator(
                progress   = { 0.5f },
                modifier   = Modifier.fillMaxWidth(),
                color      = BluePrimary,
                trackColor = DividerColor
            )
            Text(
                text     = if (isEditing) stringResource(R.string.create_step1_edit_info)
                else stringResource(R.string.create_step1_basic_info),
                fontSize = 12.sp,
                color    = TextGray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // ── Formulario ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        // Fotos
                        Text(
                            stringResource(R.string.create_photos_label),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF0F0F0))
                                        .border(1.dp, DividerColor, RoundedCornerShape(10.dp))
                                        .clickable { galleryLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AddAPhoto,
                                            null,
                                            tint = TextGray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(stringResource(R.string.common_add), fontSize = 10.sp, color = TextGray)
                                    }
                                }
                            }

                            items(photoUrls, key = { it }) { photoUrl ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.create_remove_photo_desc),
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.55f))
                                            .clickable { onRemovePhoto(photoUrl) }
                                            .padding(1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            stringResource(R.string.create_max_photos_hint),
                            fontSize = 12.sp,
                            color    = TextGray
                        )

                        HorizontalDivider(color = DividerColor)

                        // Título
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                stringResource(R.string.create_title_required),
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextDark
                            )
                            OutlinedTextField(
                                value         = title,
                                onValueChange = onTitle,
                                modifier      = Modifier.fillMaxWidth(),
                                placeholder   = { Text(stringResource(R.string.create_title_placeholder), color = TextGray) },
                                shape         = RoundedCornerShape(10.dp),
                                colors        = publishFieldColors(),
                                singleLine    = true
                            )
                        }

                        // Categoría
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                stringResource(R.string.create_category_required),
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextDark
                            )
                            ExposedDropdownMenuBox(
                                expanded         = showCategoryMenu,
                                onExpandedChange = { showCategoryMenu = it }
                            ) {
                                OutlinedTextField(
                                    value         = category?.let { categoryLabel(it) } ?: "",
                                    onValueChange = {},
                                    readOnly      = true,
                                    modifier      = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    placeholder   = { Text(stringResource(R.string.create_category_placeholder), color = TextGray) },
                                    trailingIcon  = {
                                        Icon(Icons.Default.KeyboardArrowDown, null, tint = TextGray)
                                    },
                                    shape  = RoundedCornerShape(10.dp),
                                    colors = publishFieldColors()
                                )
                                ExposedDropdownMenu(
                                    expanded         = showCategoryMenu,
                                    onDismissRequest = { showCategoryMenu = false }
                                ) {
                                    TouristPointCategory.entries.forEach { cat ->
                                        DropdownMenuItem(
                                            text    = { Text(categoryLabel(cat)) },
                                            onClick = {
                                                onCategory(cat)
                                                showCategoryMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Descripción
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                stringResource(R.string.create_description_label),
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextDark
                            )
                            OutlinedTextField(
                                value         = description,
                                onValueChange = onDescription,
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                placeholder   = { Text(stringResource(R.string.create_description_placeholder), color = TextGray) },
                                shape         = RoundedCornerShape(10.dp),
                                colors        = publishFieldColors()
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // ── Botón siguiente ────────────────────────────────────────────
            Button(
                onClick  = onNext,
                enabled  = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(50.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = TextDark,
                    disabledContainerColor = TextDark.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    stringResource(R.string.create_next_location),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
internal fun publishFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = BluePrimary,
    unfocusedBorderColor    = DividerColor,
    unfocusedContainerColor = Color(0xFFF8F8F8),
    focusedContainerColor   = CardWhite
)