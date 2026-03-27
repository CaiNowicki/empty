package com.example.progresspet.data.model

import java.time.Instant
import java.util.UUID

data class UseLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Instant,
    val feelingNote: String? = null,
    val source: LogSource = LogSource.BUTTON,
)

enum class LogSource {
    BUTTON,
    MANUAL,
}
