package com.example.progresspet.domain

import com.example.progresspet.data.model.RewardTier
import java.time.Duration
import java.time.Instant

class TrackerEngine(
    private val rewardTiers: List<RewardTier>,
) {
    fun pointsEarnedFromDelay(previousUse: Instant?, newUse: Instant): Int {
        if (previousUse == null) return 0
        val elapsed = Duration.between(previousUse, newUse).toMinutes().toInt()
        return elapsed.coerceAtLeast(0)
    }

    fun newlyUnlockedRewardIds(lifetimePoints: Int, alreadyUnlockedIds: Set<String>): Set<String> {
        return rewardTiers
            .asSequence()
            .filter { it.pointsRequired <= lifetimePoints }
            .map { it.id }
            .filterNot { it in alreadyUnlockedIds }
            .toSet()
    }
}
