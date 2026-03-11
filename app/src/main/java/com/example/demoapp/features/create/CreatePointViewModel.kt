package com.example.demoapp.features.create

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ValidatedField
import com.example.demoapp.domain.model.PriceRange
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel compartido entre los pasos de creación de publicación.
 * Se debe obtener con el mismo scope (activityViewModels o NavBackStackEntry)
 * para que el estado persista entre Step1 → Gallery → Step2.
 */
class CreatePointViewModel : ViewModel() {

    // ── Paso 1: Información básica ────────────────────────────────────────────

    val title = ValidatedField(initialValue = "", validate = {
        when {
            it.isBlank() -> "El título es obligatorio"
            it.length < 5 -> "Mínimo 5 caracteres"
            it.length > 80 -> "Máximo 80 caracteres"
            else -> null
        }
    })

    val description = ValidatedField(initialValue = "", validate = {
        when {
            it.isBlank() -> "La descripción es obligatoria"
            it.length < 20 -> "Mínimo 20 caracteres"
            it.length > 500 -> "Máximo 500 caracteres"
            else -> null
        }
    })

    val schedule = ValidatedField(initialValue = "", validate = {
        when {
            it.isBlank() -> "El horario es obligatorio"
            else -> null
        }
    })

    // Selección de categoría
    var selectedCategory by mutableStateOf<TouristPointCategory?>(null)
        private set

    var categoryError by mutableStateOf<String?>(null)
        private set

    fun onCategoryChange(displayName: String) {
        selectedCategory = TouristPointCategory.fromDisplayName(displayName)
        categoryError = null
    }

    // Selección de rango de precio
    var selectedPriceRange by mutableStateOf<PriceRange?>(null)
        private set

    var priceRangeError by mutableStateOf<String?>(null)
        private set

    fun onPriceRangeChange(displayName: String) {
        selectedPriceRange = PriceRange.fromDisplayName(displayName)
        priceRangeError = null
    }

    val isStep1Valid: Boolean
        get() = title.isValid &&
                description.isValid &&
                schedule.isValid &&
                selectedCategory != null &&
                selectedPriceRange != null

    /** Valida visualmente todos los campos del paso 1 antes de avanzar */
    fun validateStep1(): Boolean {
        title.onChange(title.value)
        description.onChange(description.value)
        schedule.onChange(schedule.value)
        if (selectedCategory == null) categoryError = "Selecciona una categoría"
        if (selectedPriceRange == null) priceRangeError = "Selecciona un rango de precio"
        return isStep1Valid
    }

    // ── Galería de fotos ──────────────────────────────────────────────────────

    // URLs/URIs seleccionadas. Cuando se conecte almacenamiento real, aquí
    // vendrán los Uri locales antes de subir y las URLs remotas después.
    val selectedPhotoUrls = mutableStateListOf<String>()

    var photoError by mutableStateOf<String?>(null)
        private set

    fun addPhoto(url: String) {
        if (selectedPhotoUrls.size >= 5) {
            photoError = "Máximo 5 fotos permitidas"
            return
        }
        if (!selectedPhotoUrls.contains(url)) {
            selectedPhotoUrls.add(url)
            photoError = null
        }
    }

    fun removePhoto(url: String) {
        selectedPhotoUrls.remove(url)
    }

    val isGalleryValid: Boolean
        get() = selectedPhotoUrls.isNotEmpty()

    fun validateGallery(): Boolean {
        if (selectedPhotoUrls.isEmpty()) photoError = "Agrega al menos una foto"
        return isGalleryValid
    }

    // ── Paso 2: Ubicación ─────────────────────────────────────────────────────

    var latitude by mutableStateOf<Double?>(null)
        private set

    var longitude by mutableStateOf<Double?>(null)
        private set

    var address by mutableStateOf("")
        private set

    var locationError by mutableStateOf<String?>(null)
        private set

    fun onLocationSelected(lat: Double, lng: Double, resolvedAddress: String = "") {
        latitude = lat
        longitude = lng
        address = resolvedAddress
        locationError = null
    }

    val isStep2Valid: Boolean
        get() = latitude != null && longitude != null

    fun validateStep2(): Boolean {
        if (latitude == null || longitude == null) locationError = "Selecciona una ubicación en el mapa"
        return isStep2Valid
    }

    // ── Envío final ───────────────────────────────────────────────────────────

    var createResult by mutableStateOf<RequestResult<TouristPoint>?>(null)
        private set

    fun submitPoint(authorId: String = "current_user_id") {
        if (!isStep1Valid || !isGalleryValid || !isStep2Valid) return

        viewModelScope.launch {
            createResult = RequestResult.Loading
            delay(1500) // Simula llamada a API / Firestore

            // TODO: reemplazar por llamada real a Firestore + subida de imágenes
            val newPoint = TouristPoint(
                id = System.currentTimeMillis().toString(),
                authorId = authorId,
                title = title.value,
                category = selectedCategory!!,
                description = description.value,
                latitude = latitude!!,
                longitude = longitude!!,
                address = address,
                schedule = schedule.value,
                priceRange = selectedPriceRange!!,
                photoUrls = selectedPhotoUrls.toList(),
                isVerified = false
            )

            Log.d("CreatePoint", "Punto creado: $newPoint")
            createResult = RequestResult.Success(newPoint)
        }
    }

    /** Resetea todo el flujo para poder crear otro punto */
    fun reset() {
        title.reset()
        description.reset()
        schedule.reset()
        selectedCategory = null
        categoryError = null
        selectedPriceRange = null
        priceRangeError = null
        selectedPhotoUrls.clear()
        photoError = null
        latitude = null
        longitude = null
        address = ""
        locationError = null
        createResult = null
    }
}