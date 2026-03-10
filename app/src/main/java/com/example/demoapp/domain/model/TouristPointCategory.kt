package com.example.demoapp.domain.model

enum class TouristPointCategory(val displayName: String) {
    GASTRONOMY("Gastronomía"),
    CULTURE("Cultura"),
    NATURE("Naturaleza"),
    ENTERTAINMENT("Entretenimiento"),
    HISTORY("Historia");

    companion object {
        fun displayNames(): List<String> = entries.map { it.displayName }
        fun fromDisplayName(name: String): TouristPointCategory? =
            entries.firstOrNull { it.displayName == name }
    }
}