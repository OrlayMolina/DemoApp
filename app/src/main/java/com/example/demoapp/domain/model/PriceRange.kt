package com.example.demoapp.domain.model

enum class PriceRange(val displayName: String) {
    FREE("Gratuito"),
    CHEAP("Económico"),
    MODERATE("Moderado"),
    EXPENSIVE("Costoso");

    companion object {
        fun displayNames(): List<String> = entries.map { it.displayName }
        fun fromDisplayName(name: String): PriceRange? =
            entries.firstOrNull { it.displayName == name }
    }
}