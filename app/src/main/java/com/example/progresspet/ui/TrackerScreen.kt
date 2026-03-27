package com.example.progresspet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrackerScreen(
    state: TrackerUiState,
    onNoteChanged: (String) -> Unit,
    onLogLapseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Time since last lapse: ${state.elapsedMinutesSinceLastUse} min", style = MaterialTheme.typography.titleMedium)
        Text("Current streak points: ${state.currentStreakPoints}")
        Text("Lifetime points: ${state.lifetimePoints}")
        Text("Unlocked rewards: ${state.unlockedRewardCount}")

        OutlinedTextField(
            value = state.feelingNoteDraft,
            onValueChange = onNoteChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Optional feeling note") },
            supportingText = { Text("Track how you felt when you indulged.") },
            minLines = 2,
        )

        Button(onClick = onLogLapseClick, modifier = Modifier.fillMaxWidth()) {
            Text("I indulged")
        }

        state.lastFeelingNote?.let { lastNote ->
            Text("Last note: $lastNote", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
