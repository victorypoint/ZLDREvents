package com.victorypoint.zldrevents.data.model

import com.victorypoint.zldrevents.data.events.dto.ZwiftEventDto
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class ZwiftEvent(
    val id: Long,
    val name: String,
    val description: String,
    val eventStart: Instant?,
    val durationInSeconds: Int?,
    val distanceInMeters: Double?,
    val laps: Int?,
    val sport: Sport,
    val eventType: String?,
    val routeName: String?,
    val subgroups: List<EventSubgroup>,
    val totalEntrantCount: Int,
)

enum class Sport { CYCLING, RUNNING, OTHER }

// Zwift returns RFC-822 offsets like "+0000" (no colon), which Instant.parse() rejects.
private val FORMATTERS = listOf(
    DateTimeFormatter.ISO_INSTANT,
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
    DateTimeFormatter.ISO_OFFSET_DATE_TIME,
)

fun parseZwiftTimestamp(s: String): Instant? {
    for (fmt in FORMATTERS) {
        try {
            return OffsetDateTime.parse(s, fmt).toInstant()
        } catch (_: Exception) {}
    }
    return try { Instant.parse(s) } catch (_: Exception) { null }
}

fun ZwiftEventDto.toDomain(): ZwiftEvent = ZwiftEvent(
    id = id,
    name = name.orEmpty(),
    description = description.orEmpty(),
    eventStart = eventStart?.let { parseZwiftTimestamp(it) },
    durationInSeconds = durationInSeconds,
    distanceInMeters = distanceInMeters,
    laps = laps,
    sport = when (sport?.uppercase()) {
        "CYCLING" -> Sport.CYCLING
        "RUNNING" -> Sport.RUNNING
        else -> Sport.OTHER
    },
    eventType = eventType,
    routeName = routeName,
    subgroups = eventSubgroups?.map { it.toDomain() } ?: emptyList(),
    // Prefer totalSignedUpCount (non-public endpoint only); fall back to totalEntrantCount.
    totalEntrantCount = totalSignedUpCount?.takeIf { it > 0 } ?: totalEntrantCount ?: 0,
)
