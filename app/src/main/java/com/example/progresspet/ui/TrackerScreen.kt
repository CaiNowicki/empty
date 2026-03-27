package com.example.progresspet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrackerScreen(
    state: TrackerUiState,
    onNoteChanged: (String) -> Unit,
    onConfirmLastUseAccurateChanged: (Boolean) -> Unit,
    onManualTimestampDraftChanged: (String) -> Unit,
    onManualPointsDraftChanged: (String) -> Unit,
    onLogLapseClick: () -> Unit,
    onAddManualLogClick: () -> Unit,
    onSpendPrizeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Time since last lapse: ${state.elapsedMinutesSinceLastUse} min", style = MaterialTheme.typography.titleMedium)
        Text("Projected points if you lapse now: ${state.projectedPointsOnLapse}")
        Text("Current streak points: ${state.currentStreakPoints}")
        Text("Banked points (spendable): ${state.bankedPoints}")
        Text("Lifetime points (historical): ${state.lifetimePoints}")
        Text("Last recorded use: ${state.lastRecordedUse ?: "none"}")

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.confirmLastUseAccurate,
                onCheckedChange = onConfirmLastUseAccurateChanged,
            )
            Text("Last recorded use is accurate")
        }

        OutlinedTextField(
            value = state.feelingNoteDraft,
            onValueChange = onNoteChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Optional feeling note") },
            minLines = 2,
        )

        Button(onClick = onLogLapseClick, modifier = Modifier.fillMaxWidth()) {
            Text("I indulged")
        }

        Text("Manual log entry (UTC LocalDateTime, ex: 2026-03-27T10:30)")
        OutlinedTextField(
            value = state.manualTimestampDraft,
            onValueChange = onManualTimestampDraftChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Past lapse timestamp") },
        )
        OutlinedTextField(
            value = state.manualPointsAdjustmentDraft,
            onValueChange = onManualPointsDraftChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Manual points adjustment (can be negative)") },
        )
        Button(onClick = onAddManualLogClick, modifier = Modifier.fillMaxWidth()) {
            Text("Add manual lapse")
        }

        Text("Unlocked tiers: ${state.unlockedRewardTierIds.joinToString().ifBlank { "none" }}")
        state.rewardPrizes.forEach { prize ->
            val unlocked = prize.tierId in state.unlockedRewardTierIds
            val canBuy = unlocked && state.bankedPoints >= prize.pointCost
            Button(
                onClick = { onSpendPrizeClick(prize.id) },
                enabled = canBuy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Buy ${prize.label} (${prize.pointCost})")
            }
        }
    }
}
