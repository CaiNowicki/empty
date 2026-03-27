package com.example.progresspet.data.repo

import com.example.progresspet.data.model.LapsePreview
import com.example.progresspet.data.model.LogSource
import com.example.progresspet.data.model.PetState
import com.example.progresspet.data.model.PointTransaction
import com.example.progresspet.data.model.RewardPrize
import com.example.progresspet.data.model.RewardTier
import com.example.progresspet.data.model.UseLog
import com.example.progresspet.domain.TrackerEngine
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InMemoryTrackerRepository : TrackerRepository {
    override val rewardTiers = MutableStateFlow(
        listOf(
            RewardTier(id = "tier_bronze", label = "Bronze", unlockMinutesRequired = 60),
            RewardTier(id = "tier_silver", label = "Silver", unlockMinutesRequired = 360),
            RewardTier(id = "tier_gold", label = "Gold", unlockMinutesRequired = 1_440),
        ),
    )

    override val rewardPrizes = MutableStateFlow(
        listOf(
            RewardPrize(id = "leaf_hat", tierId = "tier_bronze", label = "Leaf Hat", pointCost = 80),
            RewardPrize(id = "blue_scarf", tierId = "tier_silver", label = "Blue Scarf", pointCost = 220),
            RewardPrize(id = "star_room", tierId = "tier_gold", label = "Star Room Decor", pointCost = 500),
        ),
    )

    private val trackerEngine = TrackerEngine(rewardTiers.value)

    override val useLogs = MutableStateFlow<List<UseLog>>(emptyList())
    override val currentStreakPoints = MutableStateFlow(0)
    override val lifetimePoints = MutableStateFlow(0)
    override val bankedPoints = MutableStateFlow(0)
    override val petState = MutableStateFlow(PetState())

    private val transactions = MutableStateFlow<List<PointTransaction>>(emptyList())

    override fun buildLapsePreview(now: Instant): LapsePreview {
        val lastUse = useLogs.value.maxByOrNull { it.timestamp }?.timestamp
        val elapsedMinutes = if (lastUse == null) 0 else Duration.between(lastUse, now).toMinutes().coerceAtLeast(0)
        val pointsToAward = trackerEngine.pointsForMinutes(elapsedMinutes)
        val projectedUnlocks = trackerEngine.projectedUnlockedTierIds(
            logs = useLogs.value,
            now = now,
            includeCurrentPendingWindow = true,
        )
        return LapsePreview(
            now = now,
            lastRecordedUse = lastUse,
            elapsedMinutes = elapsedMinutes,
            pointsToAward = pointsToAward,
            projectedUnlockTierIds = projectedUnlocks,
        )
    }

    override suspend fun logLapse(feelingNote: String?, confirmLastUseAccurate: Boolean) {
        val now = Instant.now()
        val previousUse = useLogs.value.maxByOrNull { it.timestamp }?.timestamp
        val earned = trackerEngine.pointsEarnedFromDelay(previousUse, now)

        useLogs.update { logs ->
            (logs + UseLog(timestamp = now, feelingNote = sanitizeNote(feelingNote), source = LogSource.BUTTON))
                .sortedBy { it.timestamp }
        }

        transactions.update {
            it + PointTransaction(
                timestamp = now,
                amount = earned,
                reason = "Delay points awarded on lapse",
            )
        }

        refreshTotals(now = now, includePendingWindowForUnlocks = confirmLastUseAccurate)
        currentStreakPoints.value = 0
    }

    override suspend fun addManualLog(timestamp: Instant, feelingNote: String?, pointAdjustment: Int) {
        useLogs.update { logs ->
            (logs + UseLog(timestamp = timestamp, feelingNote = sanitizeNote(feelingNote), source = LogSource.MANUAL))
                .sortedBy { it.timestamp }
        }

        if (pointAdjustment != 0) {
            transactions.update {
                it + PointTransaction(
                    timestamp = Instant.now(),
                    amount = pointAdjustment,
                    reason = "Manual adjustment",
                )
            }
        }

        refreshTotals(now = Instant.now(), includePendingWindowForUnlocks = false)
    }

    override suspend fun spendPoints(prizeId: String) {
        val prize = rewardPrizes.value.firstOrNull { it.id == prizeId } ?: return
        if (prize.tierId !in petState.value.unlockedRewardIds) return

        transactions.update {
            it + PointTransaction(
                timestamp = Instant.now(),
                amount = -prize.pointCost,
                reason = "Spent on ${prize.label}",
            )
        }
        refreshTotals(now = Instant.now(), includePendingWindowForUnlocks = false)
    }

    private fun refreshTotals(now: Instant, includePendingWindowForUnlocks: Boolean) {
        val sortedLogs = useLogs.value.sortedBy { it.timestamp }
        val earnedFromIntervals = trackerEngine.computeTotalEarnedPoints(sortedLogs)
        val transactionSum = transactions.value.sumOf { it.amount }
        val banked = (earnedFromIntervals + transactionSum).coerceAtLeast(0)

        // lifetime only grows from naturally earned interval points.
        lifetimePoints.value = earnedFromIntervals.coerceAtLeast(lifetimePoints.value)
        bankedPoints.value = banked

        val lastUse = sortedLogs.lastOrNull()?.timestamp
        currentStreakPoints.value = if (lastUse == null) {
            0
        } else {
            trackerEngine.pointsForMinutes(Duration.between(lastUse, now).toMinutes().coerceAtLeast(0))
        }

        val unlocked = trackerEngine.projectedUnlockedTierIds(
            logs = sortedLogs,
            now = now,
            includeCurrentPendingWindow = includePendingWindowForUnlocks,
        )

        petState.update { old ->
            old.copy(unlockedRewardIds = old.unlockedRewardIds + unlocked)
        }
    }

    private fun sanitizeNote(value: String?): String? {
        return value?.trim().orEmpty().ifBlank { null }
    }
}
