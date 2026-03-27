package com.example.progresspet.data.model

import java.time.Instant
import java.util.UUID

data class PointTransaction(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Instant,
    val amount: Int,
    val reason: String,
)
