package com.example.demoapp.features.publish

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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
    photoUrl      : String?,
    title         : String,
    category      : TouristPointCategory?,
    description   : String,
    onPhotoUrl    : (String) -> Unit,
    onTitle       : (String) -> Unit,
    onCategory    : (TouristPointCategory) -> Unit,
    onDescription : (String) -> Unit,
    onNext        : () -> Unit,
    onCancel      : () -> Unit
) {
    var showCategoryMenu by remember { mutableStateOf(false) }
    var photoCounter     by remember { mutableIntStateOf(1) }

    // Abre galería real pero simula imagen con picsum
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onPhotoUrl("https://picsum.photos/seed/publish_$photoCounter/800/600")
            photoCounter++
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
                    text       = "Nueva Publicación",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )
                Text(
                    text     = "Cancelar",
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
                text     = "Paso 1 de 2: Información básica",
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
                            "Fotos",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF0F0F0))
                                .border(1.dp, DividerColor, RoundedCornerShape(10.dp))
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUrl != null) {
                                AsyncImage(
                                    model              = photoUrl,
                                    contentDescription = null,
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AddAPhoto,
                                        null,
                                        tint     = TextGray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text("Añadir foto", fontSize = 10.sp, color = TextGray)
                                }
                            }
                        }
                        Text(
                            "Puedes añadir hasta 6 fotos",
                            fontSize = 12.sp,
                            color    = TextGray
                        )

                        HorizontalDivider(color = DividerColor)

                        // Título
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Título *",
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextDark
                            )
                            OutlinedTextField(
                                value         = title,
                                onValueChange = onTitle,
                                modifier      = Modifier.fillMaxWidth(),
                                placeholder   = { Text("¿Qué lugar descubriste?", color = TextGray) },
                                shape         = RoundedCornerShape(10.dp),
                                colors        = publishFieldColors(),
                                singleLine    = true
                            )
                        }

                        // Categoría
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Categoría *",
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
                                    placeholder   = { Text("Selecciona una categoría", color = TextGray) },
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
                                "Descripción",
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
                                placeholder   = { Text("Cuéntanos más sobre este lugar", color = TextGray) },
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
                    "Siguiente: Ubicación",
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