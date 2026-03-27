package com.example.progresspet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progresspet.data.repo.TrackerRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
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
    private val confirmLastUseAccurate = MutableStateFlow(false)
    private val manualTimestampDraft = MutableStateFlow("") // ISO local datetime, e.g. 2026-03-27T10:30
    private val manualPointsDraft = MutableStateFlow("0")

    val uiState: StateFlow<TrackerUiState> = combine(
        repository.useLogs,
        repository.currentStreakPoints,
        repository.lifetimePoints,
        repository.bankedPoints,
        repository.petState,
        repository.rewardTiers,
        repository.rewardPrizes,
        nowTick,
        noteDraft,
        confirmLastUseAccurate,
        manualTimestampDraft,
        manualPointsDraft,
    ) { logs, currentStreak, lifetime, banked, pet, tiers, prizes, now, draft, confirmAccurate, manualTs, manualPoints ->
        val lastUse = logs.maxByOrNull { it.timestamp }?.timestamp
        val elapsed = if (lastUse == null) 0 else Duration.between(lastUse, now).toMinutes().coerceAtLeast(0)
        val preview = repository.buildLapsePreview(now)
        TrackerUiState(
            elapsedMinutesSinceLastUse = elapsed,
            currentStreakPoints = currentStreak,
            lifetimePoints = lifetime,
            bankedPoints = banked,
            logs = logs,
            rewardTiers = tiers,
            rewardPrizes = prizes,
            unlockedRewardTierIds = pet.unlockedRewardIds,
            lastFeelingNote = logs.lastOrNull()?.feelingNote,
            feelingNoteDraft = draft,
            confirmLastUseAccurate = confirmAccurate,
            lastRecordedUse = preview.lastRecordedUse,
            projectedPointsOnLapse = preview.pointsToAward,
            manualTimestampDraft = manualTs,
            manualPointsAdjustmentDraft = manualPoints,
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

    fun onConfirmLastUseAccurateChanged(value: Boolean) {
        confirmLastUseAccurate.value = value
    }

    fun onManualTimestampDraftChanged(value: String) {
        manualTimestampDraft.value = value
    }

    fun onManualPointsDraftChanged(value: String) {
        manualPointsDraft.value = value
    }

    fun onLogLapseClicked() {
        viewModelScope.launch {
            repository.logLapse(
                feelingNote = noteDraft.value,
                confirmLastUseAccurate = confirmLastUseAccurate.value,
            )
            noteDraft.update { "" }
            confirmLastUseAccurate.value = false
            nowTick.value = Instant.now()
        }
    }

    fun onAddManualLogClicked() {
        viewModelScope.launch {
            val timestamp = parseLocalDateTimeOrNull(manualTimestampDraft.value) ?: return@launch
            val points = manualPointsDraft.value.toIntOrNull() ?: 0
            repository.addManualLog(
                timestamp = timestamp,
                feelingNote = noteDraft.value,
                pointAdjustment = points,
            )
            manualTimestampDraft.value = ""
            manualPointsDraft.value = "0"
            noteDraft.value = ""
            nowTick.value = Instant.now()
        }
    }

    fun onSpendPrizeClicked(prizeId: String) {
        viewModelScope.launch {
            repository.spendPoints(prizeId)
        }
    }

    private fun parseLocalDateTimeOrNull(value: String): Instant? {
        return runCatching {
            LocalDateTime.parse(value).toInstant(ZoneOffset.UTC)
        }.getOrNull()
    }
}
