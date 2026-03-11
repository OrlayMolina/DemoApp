package com.example.demoapp.domain.model

data class TouristPoint(
    val id: String,
    val authorId: String,
    val title: String,
    val category: TouristPointCategory,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val schedule: String,
    val priceRange: PriceRange,
    val photoUrls: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val isRejected: Boolean = false,
    val rejectionReason: String? = null,
    val isResolved: Boolean = false,
    val importantVotes: Int = 0,
    val visitedByUserIds: List<String> = emptyList(),
    val commentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        // ── Datos quemados – reemplazar por llamadas a Firestore ──────────────
        val SAMPLE_LIST = listOf(
            TouristPoint(
                id = "1",
                authorId = "user_1",
                title = "Mirador del Café",
                category = TouristPointCategory.NATURE,
                description = "Espectacular vista panorámica de los cafetales de la región. El mejor lugar para ver el amanecer.",
                latitude = 4.5339,
                longitude = -75.6811,
                address = "Vereda El Castillo, Armenia, Quindío",
                schedule = "Todos los días 5:00 – 18:00",
                priceRange = PriceRange.FREE,
                photoUrls = listOf(
                    "https://picsum.photos/seed/mirador/800/600",
                    "https://picsum.photos/seed/cafe/800/600"
                ),
                isVerified = true,
                importantVotes = 42,
                commentCount = 8
            ),
            TouristPoint(
                id = "2",
                authorId = "user_2",
                title = "Restaurante La Fonda Quindiana",
                category = TouristPointCategory.GASTRONOMY,
                description = "Auténtica cocina paisa con bandeja paisa, sancocho y trucha de la región. Ambiente acogedor.",
                latitude = 4.5339,
                longitude = -75.6744,
                address = "Calle 20 #14-35, Armenia",
                schedule = "Mar–Dom 12:00 – 21:00",
                priceRange = PriceRange.MODERATE,
                photoUrls = listOf("https://picsum.photos/seed/fonda/800/600"),
                isVerified = true,
                importantVotes = 18,
                commentCount = 5
            ),
            TouristPoint(
                id = "3",
                authorId = "user_3",
                title = "Museo del Oro Quimbaya",
                category = TouristPointCategory.CULTURE,
                description = "Colección arqueológica de la cultura Quimbaya con piezas de oro precolombinas únicas en el mundo.",
                latitude = 4.5457,
                longitude = -75.6691,
                address = "Av. Bolívar, Armenia",
                schedule = "Mar–Sáb 10:00 – 17:00",
                priceRange = PriceRange.CHEAP,
                photoUrls = listOf("https://picsum.photos/seed/museo/800/600"),
                isVerified = true,
                importantVotes = 35,
                commentCount = 12
            ),
            TouristPoint(
                id = "4",
                authorId = "user_1",
                title = "Sendero Bosque de Niebla",
                category = TouristPointCategory.NATURE,
                description = "Sendero ecológico de 3 km rodeado de flora nativa, con avistamiento de aves y cascadas pequeñas.",
                latitude = 4.5200,
                longitude = -75.7000,
                address = "Parque Nacional Los Nevados, acceso Quindío",
                schedule = "Lun–Dom 7:00 – 15:00",
                priceRange = PriceRange.CHEAP,
                photoUrls = listOf("https://picsum.photos/seed/bosque/800/600"),
                isVerified = false,
                importantVotes = 7,
                commentCount = 2
            ),
            TouristPoint(
                id = "5",
                authorId = "user_4",
                title = "Bar El Bareto",
                category = TouristPointCategory.ENTERTAINMENT,
                description = "Bar con música en vivo los fines de semana, coctelería artesanal y ambiente bohemio.",
                latitude = 4.5365,
                longitude = -75.6780,
                address = "Carrera 13 #19-45, Armenia",
                schedule = "Jue–Sáb 18:00 – 2:00",
                priceRange = PriceRange.MODERATE,
                photoUrls = listOf("https://picsum.photos/seed/bareto/800/600"),
                isVerified = true,
                importantVotes = 22,
                commentCount = 6
            )
        )
    }
}