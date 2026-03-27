package com.example.progresspet.data.model

data class PetState(
    val name: String = "Sprout",
    val mood: String = "Encouraging",
    val unlockedRewardIds: Set<String> = emptySet(),
)
