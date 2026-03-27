package com.example.progresspet.domain

import com.example.progresspet.data.model.RewardTier
import com.example.progresspet.data.model.UseLog
import java.time.Duration
import java.time.Instant

class TrackerEngine(
    private val rewardTiers: List<RewardTier>,
) {
    /**
     * Dynamic pacing: longer delay windows produce faster point gain.
     * Returns the total points earned for one delay interval.
     */
    fun pointsEarnedFromDelay(previousUse: Instant?, newUse: Instant): Int {
        if (previousUse == null) return 0
        val minutes = Duration.between(previousUse, newUse).toMinutes().coerceAtLeast(0)
        return pointsForMinutes(minutes)
    }

    fun pointsForMinutes(minutes: Long): Int {
        var remaining = minutes
        var points = 0

        fun consume(windowMinutes: Long, ratePerMinute: Int) {
            val used = minOf(remaining, windowMinutes)
            points += (used * ratePerMinute).toInt()
            remaining -= used
        }

        consume(windowMinutes = 60, ratePerMinute = 1) // first hour: slow start
        consume(windowMinutes = 180, ratePerMinute = 2) // hours 2-4
        consume(windowMinutes = 1_200, ratePerMinute = 3) // up to 24h
        if (remaining > 0) {
            points += (remaining * 5).toInt() // 24h+ acceleration
        }

        return points
    }

    fun maxDelayMinutes(logs: List<UseLog>): Long {
        if (logs.size < 2) return 0
        val sorted = logs.sortedBy { it.timestamp }
        return sorted.zipWithNext { a, b -> Duration.between(a.timestamp, b.timestamp).toMinutes().coerceAtLeast(0) }
            .maxOrNull() ?: 0
    }

    fun unlockedTierIdsFromMaxDelay(maxDelayMinutes: Long): Set<String> {
        return rewardTiers
            .asSequence()
            .filter { it.unlockMinutesRequired <= maxDelayMinutes }
            .map { it.id }
            .toSet()
    }

    fun computeTotalEarnedPoints(logs: List<UseLog>): Int {
        if (logs.size < 2) return 0
        val sorted = logs.sortedBy { it.timestamp }
        return sorted.zipWithNext { a, b -> pointsEarnedFromDelay(a.timestamp, b.timestamp) }.sum()
    }

    fun projectedUnlockedTierIds(
        logs: List<UseLog>,
        now: Instant,
        includeCurrentPendingWindow: Boolean,
    ): Set<String> {
        val maxFromLogs = maxDelayMinutes(logs)
        val last = logs.maxByOrNull { it.timestamp }?.timestamp
        val pendingWindow = if (includeCurrentPendingWindow && last != null) {
            Duration.between(last, now).toMinutes().coerceAtLeast(0)
        } else {
            0
        }
        return unlockedTierIdsFromMaxDelay(maxOf(maxFromLogs, pendingWindow))
    }
}
