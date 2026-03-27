package com.example.progresspet.data.repo

import com.example.progresspet.data.model.PetState
import com.example.progresspet.data.model.RewardTier
import com.example.progresspet.data.model.UseLog
import com.example.progresspet.domain.TrackerEngine
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InMemoryTrackerRepository : TrackerRepository {
    private val rewardTiers = listOf(
        RewardTier(id = "hat_leaf", label = "Leaf Hat", pointsRequired = 120),
        RewardTier(id = "scarf_blue", label = "Blue Scarf", pointsRequired = 300),
        RewardTier(id = "room_plant", label = "Room Plant", pointsRequired = 600),
    )

    private val trackerEngine = TrackerEngine(rewardTiers)

    override val useLogs = MutableStateFlow<List<UseLog>>(emptyList())
    override val currentStreakPoints = MutableStateFlow(0)
    override val lifetimePoints = MutableStateFlow(0)
    override val petState = MutableStateFlow(PetState())

    override suspend fun logLapse(feelingNote: String?) {
        val now = Instant.now()
        val previousUse = useLogs.value.lastOrNull()?.timestamp

        val earnedPoints = trackerEngine.pointsEarnedFromDelay(previousUse = previousUse, newUse = now)
        val newLifetime = lifetimePoints.value + earnedPoints
        val newlyUnlocked = trackerEngine.newlyUnlockedRewardIds(
            lifetimePoints = newLifetime,
            alreadyUnlockedIds = petState.value.unlockedRewardIds,
        )

        useLogs.update { logs -> logs + UseLog(timestamp = now, feelingNote = feelingNote?.trim().orEmpty().ifBlank { null }) }
        lifetimePoints.value = newLifetime
        // Progress-first rule: current streak resets on lapse, but unlocked rewards remain.
        currentStreakPoints.value = 0
        if (newlyUnlocked.isNotEmpty()) {
            petState.update { old ->
                old.copy(unlockedRewardIds = old.unlockedRewardIds + newlyUnlocked)
            }
        }
    }
}
