package com.example.demoapp.domain.model

data class Comment(
    val id: String,
    val pointId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        val SAMPLE_BY_POINT: Map<String, List<Comment>> = mapOf(
            "1" to listOf(
                Comment(
                    id = "c1",
                    pointId = "1",
                    authorId = "user_2",
                    authorName = "Maria Garcia",
                    text = "Increible lugar! Definitivamente tengo que visitarlo.",
                    createdAt = 1708732200000L
                ),
                Comment(
                    id = "c2",
                    pointId = "1",
                    authorId = "user_admin",
                    authorName = "Carlos Admin",
                    text = "Excelente fotografia. Gracias por compartir.",
                    createdAt = 1708739400000L
                )
            ),
            "2" to listOf(
                Comment(
                    id = "c3",
                    pointId = "2",
                    authorId = "user_1",
                    authorName = "Juan Perez",
                    text = "El restaurante tiene muy buena atencion y porciones grandes.",
                    createdAt = 1708815600000L
                )
            )
        )
    }
}

