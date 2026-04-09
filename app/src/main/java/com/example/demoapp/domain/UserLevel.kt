package com.example.demoapp.domain.model

enum class UserLevel {
    NOVATO,
    COLABORADOR,
    GUARDIAN,
    HEROE_COMUNITARIO;

    companion object {
        fun fromPoints(points: Int): UserLevel = when {
            points >= 500 -> HEROE_COMUNITARIO
            points >= 200 -> GUARDIAN
            points >= 50  -> COLABORADOR
            else          -> NOVATO
        }
    }
}