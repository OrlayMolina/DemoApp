package com.example.demoapp.features.publish

import androidx.compose.runtime.*
import com.example.demoapp.domain.model.TouristPointCategory

@Composable
fun PublishScreen(
    onCancel: () -> Unit = {}
) {
    var step        by remember { mutableIntStateOf(1) }
    var photoUrl    by remember { mutableStateOf<String?>(null) }
    var title       by remember { mutableStateOf("") }
    var category    by remember { mutableStateOf<TouristPointCategory?>(null) }
    var description by remember { mutableStateOf("") }
    var latitude    by remember { mutableStateOf("4.4687891") }
    var longitude   by remember { mutableStateOf("-75.6491181") }
    var address     by remember { mutableStateOf("") }

    fun resetAll() {
        step        = 1
        photoUrl    = null
        title       = ""
        category    = null
        description = ""
        latitude    = "4.4687891"
        longitude   = "-75.6491181"
        address     = ""
    }

    when (step) {
        1 -> CreatePointStep1Screen(
            photoUrl      = photoUrl,
            title         = title,
            category      = category,
            description   = description,
            onPhotoUrl    = { photoUrl    = it },
            onTitle       = { title       = it },
            onCategory    = { category    = it },
            onDescription = { description = it },
            onNext        = { step = 2 },
            onCancel = {
                resetAll()
                onCancel()
            }
        )
        2 -> CreatePointStep2Screen(
            latitude    = latitude,
            longitude   = longitude,
            address     = address,
            onLatitude  = { latitude  = it },
            onLongitude = { longitude = it },
            onAddress   = { address   = it },
            onBack      = { step = 1 },
            onPublish   = { resetAll(); onCancel() },
            onSaveDraft = { resetAll(); onCancel() }
        )
    }
}