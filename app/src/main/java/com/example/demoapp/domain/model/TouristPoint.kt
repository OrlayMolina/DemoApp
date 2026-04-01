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
    val isReported: Boolean = false,
    val reportReason: String? = null,
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
                commentCount = 8,
                createdAt = 1708725000000L   // 24 feb 2024, 03:30
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
                commentCount = 5,
                createdAt = 1708810200000L   // 25 feb 2024, 10:42 (aprox)
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
                commentCount = 12,
                createdAt = 1708552800000L   // 22 feb 2024, 07:20
            ),
            TouristPoint(
                id = "4", authorId = "user_4",
                title = "Grafiti ofensivo reportado",
                category = TouristPointCategory.ENTERTAINMENT,
                description = "Arte urbano controversial.",
                latitude = 4.5365, longitude = -75.6780,
                address = "Carrera 13 #19-45, Armenia",
                schedule = "24/7",
                priceRange = PriceRange.FREE,
                photoUrls = listOf("https://picsum.photos/seed/grafiti/800/600"),
                isVerified = true,
                isReported = true,
                reportReason = "Contenido ofensivo para la comunidad",
                importantVotes = 3, commentCount = 8,
                createdAt = 1708725600000L
            ),
            TouristPoint(
                id = "5", authorId = "user_2",
                title = "Plaza histórica renovada",
                category = TouristPointCategory.HISTORY,
                description = "Espacio público recién restaurado.",
                latitude = 4.5300, longitude = -75.6700,
                address = "Plaza Bolívar, Armenia",
                schedule = "Todos los días",
                priceRange = PriceRange.FREE,
                photoUrls = listOf("https://picsum.photos/seed/plaza/800/600"),
                isVerified = true,
                isReported = true,
                reportReason = "Información incorrecta sobre horarios",
                importantVotes = 10, commentCount = 2,
                createdAt = 1708984200000L
            ),

            // ── Pendientes de verificación (isVerified = false) ───────────
            TouristPoint(
                id = "6", authorId = "user_1",
                title = "Nuevo café artesanal",
                category = TouristPointCategory.GASTRONOMY,
                description = "Café pequeño con decoración vintage y granos de origen.",
                latitude = 4.5350, longitude = -75.6750,
                address = "Calle 15 #8-22, Armenia",
                schedule = "Lun–Sáb 8:00 – 20:00",
                priceRange = PriceRange.MODERATE,
                photoUrls = listOf("https://picsum.photos/seed/cafe2/800/600"),
                isVerified = false, importantVotes = 0, commentCount = 0,
                createdAt = 1709026800000L
            ),
            TouristPoint(
                id = "7", authorId = "user_3",
                title = "Sendero Bosque de Niebla",
                category = TouristPointCategory.NATURE,
                description = "Sendero ecológico de 3 km con avistamiento de aves.",
                latitude = 4.5200, longitude = -75.7000,
                address = "Parque Nacional Los Nevados, Quindío",
                schedule = "Lun–Dom 7:00 – 15:00",
                priceRange = PriceRange.CHEAP,
                photoUrls = listOf("https://picsum.photos/seed/bosque/800/600"),
                isVerified = false, importantVotes = 0, commentCount = 0,
                createdAt = 1708984200000L
            ),
            TouristPoint(
                id = "8", authorId = "user_4",
                title = "Mirador Las Palmas",
                category = TouristPointCategory.NATURE,
                description = "Vista espectacular al valle desde 1800 metros.",
                latitude = 4.5450, longitude = -75.6850,
                address = "Vía al Mirador Las Palmas, Armenia",
                schedule = "Todos los días 6:00 – 18:00",
                priceRange = PriceRange.FREE,
                photoUrls = listOf("https://picsum.photos/seed/palmas/800/600"),
                isVerified = false, importantVotes = 0, commentCount = 0,
                createdAt = 1708898000000L
            ),
            TouristPoint(
                id = "9", authorId = "user_2",
                title = "Galería de Arte Contemporáneo",
                category = TouristPointCategory.CULTURE,
                description = "Exposiciones de artistas locales y nacionales.",
                latitude = 4.5380, longitude = -75.6720,
                address = "Av. Centenario #12-40, Armenia",
                schedule = "Mar–Dom 10:00 – 19:00",
                priceRange = PriceRange.CHEAP,
                photoUrls = listOf("https://picsum.photos/seed/galeria/800/600"),
                isVerified = false, importantVotes = 0, commentCount = 0,
                createdAt = 1708811000000L
            )
        )
    }
}