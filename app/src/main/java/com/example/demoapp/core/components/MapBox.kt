package com.example.demoapp.core.component

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.demoapp.domain.model.TouristPoint
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.DefaultViewportTransitionOptions
import kotlin.math.roundToInt

// ─── Estado de permiso ────────────────────────────────────────────────────────

class LocationPermissionState(
    hasPermission: Boolean = false,
    val requestPermission: () -> Unit = {}
) {
    var hasPermission by mutableStateOf(hasPermission)
        internal set
    var wasJustGranted by mutableStateOf(false)
        internal set
}

@Composable
fun rememberLocationPermissionState(
    permission: String = android.Manifest.permission.ACCESS_FINE_LOCATION
): LocationPermissionState {
    val context = LocalContext.current

    val initialPermission = remember {
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    val state = remember { LocationPermissionState(hasPermission = initialPermission) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.wasJustGranted = granted && !state.hasPermission
        state.hasPermission  = granted
    }

    return remember(state, launcher) {
        LocationPermissionState(
            hasPermission     = state.hasPermission,
            requestPermission = { launcher.launch(permission) }
        ).also { it.wasJustGranted = state.wasJustGranted }
    }
}

// ─── Componente ───────────────────────────────────────────────────────────────

@Composable
fun MapBox(
    modifier             : Modifier           = Modifier,
    points               : List<TouristPoint> = emptyList(),
    showMyLocationButton : Boolean            = true,
    activateClick        : Boolean            = false,
    clickedPoint         : Point?             = null,
    onMapClickListener   : (Point) -> Unit    = {}
) {
    val permissionState  = rememberLocationPermissionState()
    var shouldFollowUser by remember { mutableStateOf(false) }
    var internalClicked  by remember { mutableStateOf<Point?>(null) }

    // Posición en pantalla del pin (para superponerlo con Compose)
    var pinScreenOffset  by remember { mutableStateOf<Offset?>(null) }

    val activePoint = clickedPoint ?: internalClicked

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(8.0)
            center(Point.fromLngLat(-75.6491181, 4.4687891))
        }
    }

    Box(modifier = modifier) {

        MapboxMap(
            modifier           = Modifier.matchParentSize(),
            mapViewportState   = mapViewportState,
            onMapClickListener = { point ->
                if (activateClick) {
                    internalClicked = point
                    onMapClickListener(point)
                }
                true
            }
        ) {
            // ── Fix fluidez: desactiva scroll del padre durante gestos ─────
            MapEffect(Unit) { mapView ->
                // Permite que el mapa consuma los gestos sin competir con ScrollView
                mapView.gestures.apply {
                    scrollDecelerationEnabled = true
                    pinchToZoomEnabled        = true
                    doubleTouchToZoomOutEnabled = true
                    doubleTapToZoomInEnabled  = true
                }
            }

            // ── Seguir ubicación del usuario ───────────────────────────────
            if (permissionState.hasPermission && shouldFollowUser) {
                MapEffect(key1 = "follow_puck") { mapView ->
                    mapView.location.updateSettings {
                        locationPuck       = createDefault2DPuck(withBearing = true)
                        enabled            = true
                        puckBearing        = PuckBearing.COURSE
                        puckBearingEnabled = true
                    }
                    mapViewportState.transitionToFollowPuckState(
                        defaultTransitionOptions = DefaultViewportTransitionOptions.Builder()
                            .maxDurationMs(1500)
                            .build()
                    )
                }
            }

            // ── Marcadores de publicaciones existentes ─────────────────────
            points.forEach { touristPoint ->
                PointAnnotation(
                    point = Point.fromLngLat(touristPoint.longitude, touristPoint.latitude)
                )
            }

            // ── Calcular posición en pantalla del pin seleccionado ─────────
            if (activePoint != null) {
                MapEffect(activePoint) { mapView ->
                    val screenCoordinate = mapView.mapboxMap
                        .pixelForCoordinate(activePoint)
                    pinScreenOffset = Offset(
                        screenCoordinate.x.toFloat(),
                        screenCoordinate.y.toFloat()
                    )
                }
            }
        }

        // ── Pin de Compose superpuesto (visible y con color) ───────────────
        pinScreenOffset?.let { offset ->
            Icon(
                imageVector        = Icons.Default.LocationOn,
                contentDescription = "Ubicación seleccionada",
                tint               = Color(0xFFE53935),   // rojo
                modifier           = Modifier
                    .size(36.dp)
                    .offset {
                        IntOffset(
                            // Centra horizontalmente y ancla la punta abajo
                            x = (offset.x - 18.dp.toPx()).roundToInt(),
                            y = (offset.y - 36.dp.toPx()).roundToInt()
                        )
                    }
            )
        }

        // ── Botón mi ubicación ─────────────────────────────────────────────
        if (showMyLocationButton) {
            FloatingActionButton(
                onClick = {
                    if (permissionState.hasPermission) {
                        shouldFollowUser = true
                    } else {
                        permissionState.requestPermission()
                    }
                },
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = Icons.Default.MyLocation,
                    contentDescription = "Mi ubicación"
                )
            }
        }
    }
}