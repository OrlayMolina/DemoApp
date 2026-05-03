package com.example.demoapp.domain.model

enum class AchievementType(
    val id: String,
    val icon: AchievementIcon,
    val goal: Int
) {
    NOVICE_EXPLORER(id = "novice_explorer", icon = AchievementIcon.STAR, goal = 1),
    URBAN_PHOTOGRAPHER(id = "urban_photographer", icon = AchievementIcon.CAMERA, goal = 10),
    LOCAL_INFLUENCER(id = "local_influencer", icon = AchievementIcon.HEART, goal = 100),
    MASTER_EXPLORER(id = "master_explorer", icon = AchievementIcon.TROPHY, goal = 50),
    ACTIVE_COMMUNITY_MEMBER(id = "active_community_member", icon = AchievementIcon.COMMUNITY, goal = 500),
    VERIFIED_USER(id = "verified_user", icon = AchievementIcon.CHECK, goal = 5);
}
