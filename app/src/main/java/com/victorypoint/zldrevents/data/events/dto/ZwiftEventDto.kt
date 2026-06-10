package com.victorypoint.zldrevents.data.events.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ZwiftEventDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "eventStart") val eventStart: String? = null,
    @Json(name = "durationInSeconds") val durationInSeconds: Int? = null,
    @Json(name = "distanceInMeters") val distanceInMeters: Double? = null,
    @Json(name = "laps") val laps: Int? = null,
    @Json(name = "sport") val sport: String? = null,
    @Json(name = "routeName") val routeName: String? = null,
    @Json(name = "eventType") val eventType: String? = null,
    @Json(name = "eventSubgroups") val eventSubgroups: List<EventSubgroupDto>? = null,
    @Json(name = "totalEntrantCount") val totalEntrantCount: Int? = null,
    // Non-public api/events/{id} only — always 0 from the public endpoint.
    @Json(name = "totalSignedUpCount") val totalSignedUpCount: Int? = null,
)
