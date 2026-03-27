package com.example.progresspet.data.model

import java.time.Instant

data class UseLog(
    val timestamp: Instant,
    val feelingNote: String? = null,
)
