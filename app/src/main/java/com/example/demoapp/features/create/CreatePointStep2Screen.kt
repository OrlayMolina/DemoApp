package com.example.demoapp.features.publish

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.core.component.MapBox
import com.mapbox.geojson.Point
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset

@Composable
fun CreatePointStep2Screen(
    latitude    : String,
    longitude   : String,
    address     : String,
    isEditing   : Boolean = false,
    onLatitude  : (String) -> Unit,
    onLongitude : (String) -> Unit,
    onAddress   : (String) -> Unit,
    onBack      : () -> Unit,
    onPublish   : () -> Unit,
    onSaveDraft : () -> Unit
) {
    val context = LocalContext.current
    val lat     = latitude.toDoubleOrNull()  ?: 4.4687891
    val lng     = longitude.toDoubleOrNull() ?: -75.6491181

    // Punto seleccionado para pasarlo al mapa
    var selectedPoint by remember {
        mutableStateOf<Point?>(Point.fromLngLat(lng, lat))
    }

    val mapNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource) = Offset.Zero
        }
    }

    if (!isEditing) {
        OutlinedButton(
            onClick = {
                onSaveDraft()
                Toast.makeText(context, "Guardado como borrador", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
        ) {
            Text("Guardar como borrador", fontSize = 15.sp)
        }
    }

    Scaffold(containerColor = BackgroundGray) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Atrás", tint = TextDark)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text       = if (isEditing) "Editar Publicación" else "Nueva Publicación",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }

            // ── Progreso ───────────────────────────────────────────────────
            LinearProgressIndicator(
                progress   = { 1f },
                modifier   = Modifier.fillMaxWidth(),
                color      = BluePrimary,
                trackColor = DividerColor
            )
            Text(
                text     = if (isEditing) "Paso 2 de 2: Editar ubicación"
                else "Paso 2 de 2: Ubicación",
                fontSize = 12.sp,
                color    = TextGray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Mapa ───────────────────────────────────────────────────
                Card(
                    shape    = RoundedCornerShape(14.dp),
                    colors   = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.nestedScroll(mapNestedScrollConnection)
                ) {
                    MapBox(
                        modifier             = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        activateClick        = true,
                        clickedPoint         = selectedPoint,
                        showMyLocationButton = true,
                        onMapClickListener   = { point ->
                            selectedPoint = point
                            onLatitude("%.4f".format(point.latitude()))
                            onLongitude("%.4f".format(point.longitude()))
                        }
                    )
                }

                // ── Coordenadas y dirección ────────────────────────────────
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Coordenadas",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value         = latitude,
                                onValueChange = { value ->
                                    onLatitude(value)
                                    val newLat = value.toDoubleOrNull()
                                    val newLng = longitude.toDoubleOrNull()
                                    if (newLat != null && newLng != null) {
                                        selectedPoint = Point.fromLngLat(newLng, newLat)
                                    }
                                },
                                modifier      = Modifier.weight(1f),
                                placeholder   = { Text("Latitud", color = TextGray) },
                                shape         = RoundedCornerShape(10.dp),
                                colors        = publishFieldColors(),
                                singleLine    = true
                            )
                            OutlinedTextField(
                                value         = longitude,
                                onValueChange = { value ->
                                    onLongitude(value)
                                    val newLat = latitude.toDoubleOrNull()
                                    val newLng = value.toDoubleOrNull()
                                    if (newLat != null && newLng != null) {
                                        selectedPoint = Point.fromLngLat(newLng, newLat)
                                    }
                                },
                                modifier      = Modifier.weight(1f),
                                placeholder   = { Text("Longitud", color = TextGray) },
                                shape         = RoundedCornerShape(10.dp),
                                colors        = publishFieldColors(),
                                singleLine    = true
                            )
                        }

                        Text(
                            "Dirección (opcional)",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextDark
                        )
                        OutlinedTextField(
                            value         = address,
                            onValueChange = onAddress,
                            modifier      = Modifier.fillMaxWidth(),
                            placeholder   = { Text("Calle, colonia, ciudad ...", color = TextGray) },
                            shape         = RoundedCornerShape(10.dp),
                            colors        = publishFieldColors(),
                            singleLine    = true
                        )
                    }
                }

                // ── Consejo ────────────────────────────────────────────────
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))
                ) {
                    Row(
                        modifier              = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            null,
                            tint     = Color(0xFFF9A825),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text     = "Arrastra el mapa o ingresa las coordenadas manualmente para marcar la ubicación exacta del lugar",
                            fontSize = 13.sp,
                            color    = Color(0xFF795548)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
            }

            // ── Botones ────────────────────────────────────────────────────
            Column(
                modifier            = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick  = {
                        onPublish()
                        Toast.makeText(
                            context,
                            if (isEditing) "¡Publicación actualizada con éxito! ️"
                            else "¡Publicación realizada con éxito!",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                ) {
                    Text(
                        if (isEditing) "Guardar cambios" else "Publicar",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick  = {
                        onSaveDraft()
                        Toast.makeText(
                            context,
                            "Guardado como borrador",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
                ) {
                    Text("Guardar como borrador", fontSize = 15.sp)
                }
            }
        }
    }
}