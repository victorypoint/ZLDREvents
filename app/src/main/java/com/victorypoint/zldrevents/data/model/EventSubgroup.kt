package com.victorypoint.zldrevents.data.model

import com.victorypoint.zldrevents.data.events.dto.EventSubgroupDto
import java.time.Instant

data class EventSubgroup(
    val id: Long,
    val name: String,
    val label: String,
    val distanceInMeters: Double?,
    val laps: Int?,
    val durationInSeconds: Int?,
    val totalEntrantCount: Int,
    val signedUpCount: Int,
    val subgroupStart: Instant?,
)

private val CATEGORY_LABELS = mapOf(1 to "A", 2 to "B", 3 to "C", 4 to "D", 5 to "E")

fun EventSubgroupDto.toDomain(): EventSubgroup = EventSubgroup(
    id = id,
    name = name.orEmpty(),
    label = subgroupLabel ?: CATEGORY_LABELS[label] ?: label?.toString() ?: "",
    distanceInMeters = distanceInMeters,
    laps = laps,
    durationInSeconds = durationInSeconds,
    totalEntrantCount = totalEntrantCount ?: 0,
    signedUpCount = signedUpCount ?: 0,
    subgroupStart = eventSubgroupStart?.let { runCatching { Instant.parse(it) }.getOrNull() },
)
