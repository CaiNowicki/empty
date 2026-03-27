package com.example.progresspet.data.model

import java.time.Instant

data class LapsePreview(
    val now: Instant,
    val lastRecordedUse: Instant?,
    val elapsedMinutes: Long,
    val pointsToAward: Int,
    val projectedUnlockTierIds: Set<String>,
)
