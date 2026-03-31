package com.example.demoapp.features.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory

class MapPointsViewModel : ViewModel() {

    var allPoints by mutableStateOf(TouristPoint.SAMPLE_LIST)
        private set

    // Categoría seleccionada — null significa "todas"
    var selectedCategory by mutableStateOf<TouristPointCategory?>(null)
        private set

    // Si true muestra el mapa, si false muestra la pantalla de selección
    var showingMap by mutableStateOf(false)
        private set

    val filteredPoints get() = selectedCategory
        ?.let { cat -> allPoints.filter { it.category == cat } }
        ?: allPoints

    // Cuántas publicaciones por categoría (para los recuadros)
    val countByCategory: Map<TouristPointCategory, Int> get() =
        TouristPointCategory.entries.associateWith { cat ->
            allPoints.count { it.category == cat }
        }.filter { it.value > 0 }

    // Top 4 categorías con más publicaciones
    val topCategories get() = countByCategory
        .entries
        .sortedByDescending { it.value }
        .take(4)

    fun selectCategory(category: TouristPointCategory?) {
        selectedCategory = category
        showingMap       = true
    }

    fun goBackToSelection() {
        showingMap       = false
        selectedCategory = null
    }
}