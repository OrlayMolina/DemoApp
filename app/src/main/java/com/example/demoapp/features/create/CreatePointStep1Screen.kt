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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
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
import com.example.demoapp.features.create.CreatePointViewModel

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
    aiSuggestion  : CreatePointViewModel.AiSuggestionState = CreatePointViewModel.AiSuggestionState.Idle,
    acceptedTags  : List<String> = emptyList(),
    onAddPhoto    : (String) -> Unit,
    onRemovePhoto : (String) -> Unit,
    onTitle       : (String) -> Unit,
    onCategory    : (TouristPointCategory) -> Unit,
    onDescription : (String) -> Unit,
    onAiAssist    : () -> Unit = {},
    onToggleTag   : (String) -> Unit = {},
    onApplyAiDescription: () -> Unit = {},
    onDismissAi   : () -> Unit = {},
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

                            AiAssistSection(
                                state               = aiSuggestion,
                                acceptedTags        = acceptedTags,
                                onAssist            = onAiAssist,
                                onToggleTag         = onToggleTag,
                                onApplyDescription  = onApplyAiDescription,
                                onDismiss           = onDismissAi
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

// ─── Asistente IA ────────────────────────────────────────────────────────────

@Composable
private fun AiAssistSection(
    state              : CreatePointViewModel.AiSuggestionState,
    acceptedTags       : List<String>,
    onAssist           : () -> Unit,
    onToggleTag        : (String) -> Unit,
    onApplyDescription : () -> Unit,
    onDismiss          : () -> Unit
) {
    val accent = Color(0xFF2E7D5E)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = onAssist,
            shape   = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            colors  = ButtonDefaults.outlinedButtonColors(contentColor = accent)
        ) {
            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Mejorar con IA", fontSize = 13.sp)
        }

        when (state) {
            CreatePointViewModel.AiSuggestionState.Idle -> Unit
            CreatePointViewModel.AiSuggestionState.Loading -> {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = accent
                    )
                    Text("Analizando con IA...", fontSize = 12.sp, color = TextGray)
                }
            }
            is CreatePointViewModel.AiSuggestionState.Error -> {
                Text(
                    text     = "IA: ${state.message}",
                    fontSize = 12.sp,
                    color    = Color(0xFFB00020)
                )
            }
            is CreatePointViewModel.AiSuggestionState.Ready -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEAF4EE))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = accent, modifier = Modifier.size(16.dp))
                        Text("Sugerencias IA", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, null, tint = TextGray, modifier = Modifier.size(14.dp))
                        }
                    }

                    if (state.enrichment.tags.isNotEmpty()) {
                        Text("Tags sugeridos:", fontSize = 12.sp, color = TextGray)
                        FlowRowTags(
                            tags         = state.enrichment.tags,
                            acceptedTags = acceptedTags,
                            onToggle     = onToggleTag,
                            accent       = accent
                        )
                    }

                    state.enrichment.improvedDescription?.let { suggested ->
                        Text("Descripcion sugerida:", fontSize = 12.sp, color = TextGray)
                        Text(
                            text     = suggested,
                            fontSize = 12.sp,
                            color    = TextDark
                        )
                        TextButton(
                            onClick = onApplyDescription,
                            colors  = ButtonDefaults.textButtonColors(contentColor = accent)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Usar esta descripcion", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowTags(
    tags         : List<String>,
    acceptedTags : List<String>,
    onToggle     : (String) -> Unit,
    accent       : Color
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement   = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        tags.forEach { tag ->
            val accepted = acceptedTags.contains(tag)
            val bg = if (accepted) accent else Color(0xFFE0E7E3)
            val fg = if (accepted) Color.White else TextDark
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable { onToggle(tag) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (accepted) Icon(Icons.Default.Check, null, tint = fg, modifier = Modifier.size(12.dp))
                Text(tag, fontSize = 12.sp, color = fg)
            }
        }
    }
}