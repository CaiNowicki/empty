package com.example.progresspet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progresspet.data.repo.TrackerRepository
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val repository: TrackerRepository,
) : ViewModel() {

    private val nowTick = MutableStateFlow(Instant.now())
    private val noteDraft = MutableStateFlow("")

    val uiState: StateFlow<TrackerUiState> = combine(
        repository.useLogs,
        repository.currentStreakPoints,
        repository.lifetimePoints,
        repository.petState,
        nowTick,
        noteDraft,
    ) { logs, currentStreak, lifetime, pet, now, draft ->
        val lastUse = logs.lastOrNull()?.timestamp
        val elapsed = if (lastUse == null) 0 else Duration.between(lastUse, now).toMinutes().coerceAtLeast(0)
        TrackerUiState(
            elapsedMinutesSinceLastUse = elapsed,
            currentStreakPoints = currentStreak,
            lifetimePoints = lifetime,
            logs = logs,
            lastFeelingNote = logs.lastOrNull()?.feelingNote,
            feelingNoteDraft = draft,
            unlockedRewardCount = pet.unlockedRewardIds.size,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrackerUiState(),
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                nowTick.value = Instant.now()
            }
        }
    }

    fun onNoteDraftChanged(value: String) {
        noteDraft.value = value
    }

    fun onLogLapseClicked() {
        viewModelScope.launch {
            repository.logLapse(feelingNote = noteDraft.value)
            noteDraft.update { "" }
            nowTick.value = Instant.now()
        }
    }
}
