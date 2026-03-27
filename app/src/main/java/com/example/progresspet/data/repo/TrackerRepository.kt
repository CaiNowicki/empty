package com.example.progresspet.data.repo

import com.example.progresspet.data.model.PetState
import com.example.progresspet.data.model.UseLog
import kotlinx.coroutines.flow.StateFlow

interface TrackerRepository {
    val useLogs: StateFlow<List<UseLog>>
    val currentStreakPoints: StateFlow<Int>
    val lifetimePoints: StateFlow<Int>
    val petState: StateFlow<PetState>

    suspend fun logLapse(feelingNote: String?)
}
