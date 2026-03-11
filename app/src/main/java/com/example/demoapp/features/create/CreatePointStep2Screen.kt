package com.example.demoapp.features.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.core.utils.RequestResult

/**
 * U-08 · Crear Publicación — Paso 2 (Mapa)
 *
 * Permite al usuario seleccionar la ubicación del punto de interés.
 *
 * La sección del mapa es un PLACEHOLDER — para integrar Google Maps o Mapbox:
 *   1. Agrega la dependencia en build.gradle
 *   2. Reemplaza el bloque `MapPlaceholder` por el composable del SDK elegido
 *   3. En el callback `onMapClick` llama a `viewModel.onLocationSelected(lat, lng, address)`
 *
 * Mientras tanto, el usuario puede ingresar lat/lng manualmente para pruebas.
 *
 * @param onNavigateBack   Volver a la galería.
 * @param onPublishSuccess Callback cuando la publicación se crea con éxito.
 * @param viewModel        ViewModel compartido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePointStep2Screen(
    onNavigateBack: () -> Unit = {},
    onPublishSuccess: () -> Unit = {},
    viewModel: CreatePointViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Campos auxiliares para ingresar lat/lng manualmente (modo prueba)
    var latInput by remember { mutableStateOf(viewModel.latitude?.toString() ?: "") }
    var lngInput by remember { mutableStateOf(viewModel.longitude?.toString() ?: "") }
    var addressInput by remember { mutableStateOf(viewModel.address) }

    // Reaccionamos al resultado del envío
    LaunchedEffect(viewModel.createResult) {
        when (val result = viewModel.createResult) {
            is RequestResult.Success -> {
                snackbarHostState.showSnackbar("¡Punto publicado con éxito!")
                onPublishSuccess()
                viewModel.reset()
            }
            is RequestResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ubicación — Paso 3 / 3") },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Selecciona la ubicación",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Toca el mapa para marcar el punto exacto del lugar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Mapa (PLACEHOLDER) ────────────────────────────────────────────
            // TODO: reemplazar por GoogleMap o MapboxMap composable
            MapPlaceholder(
                selectedLat = viewModel.latitude,
                selectedLng = viewModel.longitude,
                onMockLocationSelected = { lat, lng ->
                    viewModel.onLocationSelected(lat, lng, "Dirección obtenida del mapa")
                    latInput = lat.toString()
                    lngInput = lng.toString()
                    addressInput = "Dirección obtenida del mapa"
                }
            )

            // ── Entrada manual de coordenadas (solo para pruebas) ─────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Coordenadas (para pruebas)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = latInput,
                            onValueChange = { latInput = it },
                            label = { Text("Latitud") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = lngInput,
                            onValueChange = { lngInput = it },
                            label = { Text("Longitud") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Dirección") },
                        singleLine = true
                    )

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val lat = latInput.toDoubleOrNull()
                            val lng = lngInput.toDoubleOrNull()
                            if (lat != null && lng != null) {
                                viewModel.onLocationSelected(lat, lng, addressInput)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmar ubicación")
                    }
                }
            }

            // ── Error de ubicación ────────────────────────────────────────────
            viewModel.locationError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            // ── Ubicación confirmada ──────────────────────────────────────────
            if (viewModel.latitude != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = buildString {
                            if (viewModel.address.isNotBlank()) append(viewModel.address + " · ")
                            append("%.5f, %.5f".format(viewModel.latitude, viewModel.longitude))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Feedback del resultado ────────────────────────────────────────
            when (val result = viewModel.createResult) {
                is RequestResult.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Publicando punto de interés…")
                    }
                }
                is RequestResult.Error ->
                    Text(result.message, color = MaterialTheme.colorScheme.error)
                else -> {}
            }

            // ── Botón publicar ────────────────────────────────────────────────
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = {
                    if (viewModel.validateStep2()) {
                        viewModel.submitPoint()
                    }
                },
                enabled = viewModel.createResult !is RequestResult.Loading
            ) {
                Text("Publicar punto de interés")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── MapPlaceholder ─────────────────────────────────────────────────────────────
// Reemplazar este bloque completo por el mapa real (Google Maps / Mapbox)

@Composable
private fun MapPlaceholder(
    selectedLat: Double?,
    selectedLng: Double?,
    onMockLocationSelected: (Double, Double) -> Unit
) {
    // Mock locations para pruebas (Armenia, Quindío y alrededores)
    val mockLocations = listOf(
        Triple("Centro Armenia", 4.5339, -75.6811),
        Triple("Parque Centenario", 4.5360, -75.6780),
        Triple("Zona Cafetera", 4.5200, -75.7000),
        Triple("Aeropuerto", 4.4528, -75.7661)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mapa",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Integra Google Maps o Mapbox aquí",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botones rápidos para simular selección de ubicación
        Text(
            text = "Toca para simular ubicación:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            mockLocations.take(2).forEach { (name, lat, lng) ->
                val isSelected = selectedLat == lat && selectedLng == lng
                FilterChip(
                    selected = isSelected,
                    onClick = { onMockLocationSelected(lat, lng) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            mockLocations.drop(2).forEach { (name, lat, lng) ->
                val isSelected = selectedLat == lat && selectedLng == lng
                FilterChip(
                    selected = isSelected,
                    onClick = { onMockLocationSelected(lat, lng) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}