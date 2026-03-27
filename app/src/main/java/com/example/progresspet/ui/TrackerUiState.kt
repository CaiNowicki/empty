package com.example.progresspet.ui

import com.example.progresspet.data.model.RewardPrize
import com.example.progresspet.data.model.RewardTier
import com.example.progresspet.data.model.UseLog
import java.time.Instant

/**
 * UI state for the tracker screen.
 */
data class TrackerUiState(
    val elapsedMinutesSinceLastUse: Long = 0,
    val currentStreakPoints: Int = 0,
    val lifetimePoints: Int = 0,
    val bankedPoints: Int = 0,
    val logs: List<UseLog> = emptyList(),
    val rewardTiers: List<RewardTier> = emptyList(),
    val rewardPrizes: List<RewardPrize> = emptyList(),
    val unlockedRewardTierIds: Set<String> = emptySet(),
    val lastFeelingNote: String? = null,
    val feelingNoteDraft: String = "",
    val confirmLastUseAccurate: Boolean = false,
    val lastRecordedUse: Instant? = null,
    val projectedPointsOnLapse: Int = 0,
    val manualTimestampDraft: String = "",
    val manualPointsAdjustmentDraft: String = "0",
)
