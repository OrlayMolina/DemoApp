package com.example.demoapp.features.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.core.components.DropdownMenu
import com.example.demoapp.domain.model.PriceRange
import com.example.demoapp.domain.model.TouristPointCategory

/**
 * U-06 · Crear Publicación — Paso 1
 *
 * Recopila: título, categoría, descripción, horario y rango de precio.
 * Al continuar se avanza a la galería de fotos (U-07).
 *
 * @param onNavigateBack      Volver a la pantalla anterior.
 * @param onNavigateToGallery Avanzar al siguiente paso (galería).
 * @param viewModel           ViewModel compartido con los demás pasos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePointStep1Screen(
    onNavigateBack: () -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    viewModel: CreatePointViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nuevo punto — Paso 1 / 3") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Encabezado ────────────────────────────────────────────────────
            Text(
                text = "Información básica",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Cuéntanos sobre el lugar que quieres compartir con la comunidad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // ── Título ────────────────────────────────────────────────────────
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.title.value,
                onValueChange = { viewModel.title.onChange(it) },
                label = { Text("Título del lugar *") },
                placeholder = { Text("Ej: Mirador del Café") },
                isError = viewModel.title.error != null,
                supportingText = {
                    val error = viewModel.title.error
                    val count = viewModel.title.value.length
                    if (error != null) {
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(text = "$count / 80", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                singleLine = true
            )

            // ── Categoría ─────────────────────────────────────────────────────
            DropdownMenu(
                value = viewModel.selectedCategory?.displayName ?: "",
                label = "Categoría *",
                list = TouristPointCategory.displayNames(),
                supportingText = viewModel.categoryError,
                onValueChange = { viewModel.onCategoryChange(it) }
            )

            // ── Descripción ───────────────────────────────────────────────────
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                value = viewModel.description.value,
                onValueChange = { viewModel.description.onChange(it) },
                label = { Text("Descripción *") },
                placeholder = { Text("Describe el lugar, qué ofrece y por qué vale la pena visitarlo…") },
                isError = viewModel.description.error != null,
                supportingText = {
                    val error = viewModel.description.error
                    val count = viewModel.description.value.length
                    if (error != null) {
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(text = "$count / 500", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                maxLines = 8
            )

            // ── Horario ───────────────────────────────────────────────────────
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.schedule.value,
                onValueChange = { viewModel.schedule.onChange(it) },
                label = { Text("Horario de atención *") },
                placeholder = { Text("Ej: Lun–Vie 9:00–18:00 / Fines de semana 8:00–20:00") },
                isError = viewModel.schedule.error != null,
                supportingText = {
                    viewModel.schedule.error?.let { Text(text = it) }
                },
                singleLine = true
            )

            // ── Rango de precio ───────────────────────────────────────────────
            DropdownMenu(
                value = viewModel.selectedPriceRange?.displayName ?: "",
                label = "Rango de precio *",
                list = PriceRange.displayNames(),
                supportingText = viewModel.priceRangeError,
                onValueChange = { viewModel.onPriceRangeChange(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Botón continuar ───────────────────────────────────────────────
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = {
                    if (viewModel.validateStep1()) {
                        onNavigateToGallery()
                    }
                }
            ) {
                Text("Continuar → Agregar fotos")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}