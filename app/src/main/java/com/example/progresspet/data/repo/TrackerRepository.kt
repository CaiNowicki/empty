package com.example.progresspet.data.repo

import com.example.progresspet.data.model.LapsePreview
import com.example.progresspet.data.model.PetState
import com.example.progresspet.data.model.RewardPrize
import com.example.progresspet.data.model.RewardTier
import com.example.progresspet.data.model.UseLog
import java.time.Instant
import kotlinx.coroutines.flow.StateFlow

interface TrackerRepository {
    val useLogs: StateFlow<List<UseLog>>
    val currentStreakPoints: StateFlow<Int>
    val lifetimePoints: StateFlow<Int>
    val bankedPoints: StateFlow<Int>
    val petState: StateFlow<PetState>
    val rewardTiers: StateFlow<List<RewardTier>>
    val rewardPrizes: StateFlow<List<RewardPrize>>

    fun buildLapsePreview(now: Instant = Instant.now()): LapsePreview
    suspend fun logLapse(feelingNote: String?, confirmLastUseAccurate: Boolean)
    suspend fun addManualLog(timestamp: Instant, feelingNote: String?, pointAdjustment: Int)
    suspend fun spendPoints(prizeId: String)
}
