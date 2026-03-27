package com.example.progresspet.ui

import com.example.progresspet.data.model.UseLog

/**
 * UI state for the MVP tracker screen.
 */
data class TrackerUiState(
    val elapsedMinutesSinceLastUse: Long = 0,
    val currentStreakPoints: Int = 0,
    val lifetimePoints: Int = 0,
    val logs: List<UseLog> = emptyList(),
    val lastFeelingNote: String? = null,
    val feelingNoteDraft: String = "",
    val unlockedRewardCount: Int = 0,
)
