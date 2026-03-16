package com.example.demoapp.domain.model

data class Achievement(
    val id          : String,
    val title       : String,
    val description : String,
    val icon        : AchievementIcon,
    val isUnlocked  : Boolean,
    val unlockedDate: String? = null,
    val progress    : Int     = 0,
    val goal        : Int     = 1
){
    companion object {
        val SAMPLE_LIST = listOf(
            Achievement(
                id           = "1",
                title        = "Explorador Novato",
                description  = "Publica tu primera ubicación",
                icon         = AchievementIcon.STAR,
                isUnlocked   = true,
                unlockedDate = "19 de enero de 2024",
                progress     = 1,
                goal         = 1
            ),
            Achievement(
                id           = "2",
                title        = "Fotógrafo Urbano",
                description  = "Publica 10 ubicaciones con fotos",
                icon         = AchievementIcon.CAMERA,
                isUnlocked   = true,
                unlockedDate = "14 de marzo de 2024",
                progress     = 10,
                goal         = 10
            ),
            Achievement(
                id           = "3",
                title        = "Influencer Local",
                description  = "Obtén 100 likes en total",
                icon         = AchievementIcon.HEART,
                isUnlocked   = true,
                unlockedDate = "14 de marzo de 2024",
                progress     = 100,
                goal         = 100
            ),
            Achievement(
                id           = "4",
                title        = "Maestro Explorador",
                description  = "Publica 50 ubicaciones",
                icon         = AchievementIcon.TROPHY,
                isUnlocked   = false,
                progress     = 47,
                goal         = 50
            ),
            Achievement(
                id           = "5",
                title        = "Comunidad Activa",
                description  = "Recibe 500 likes en total",
                icon         = AchievementIcon.COMMUNITY,
                isUnlocked   = false,
                progress     = 234,
                goal         = 500
            ),
            Achievement(
                id           = "6",
                title        = "Verificado",
                description  = "Obtén 5 publicaciones verificadas",
                icon         = AchievementIcon.CHECK,
                isUnlocked   = false,
                progress     = 2,
                goal         = 5
            )
        )
    }
}

