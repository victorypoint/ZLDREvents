package com.victorypoint.zldrevents.data.events

import com.victorypoint.zldrevents.data.events.dto.ZwiftEventDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface EventsApi {
    // Offset-based pagination via `start` (0, 200, 400, …).
    // `tags` filter works and is the primary ZLDR discovery mechanism.
    // Other filter params (sport, organizerGroupId, eventStartsAfter, organizerId) are ignored.
    @GET("api/public/events/upcoming")
    suspend fun getUpcomingEvents(
        @Header("Zwift-Api-Version") apiVersion: String = "2.5",
        @Query("limit") limit: Int = 200,
        @Query("start") start: Int = 0,
        @Query("tags") tags: String? = null,
    ): List<ZwiftEventDto>

    // Public detail endpoint — used as fallback in getEventDetail() when not cached.
    @GET("api/public/events/{eventId}")
    suspend fun getEvent(
        @Path("eventId") eventId: Long,
        @Header("Zwift-Api-Version") apiVersion: String = "2.5",
    ): ZwiftEventDto

    // Non-public endpoint — returns accurate totalSignedUpCount (always 0 from public endpoint).
    // Requires Bearer token (handled by AuthInterceptor).
    @GET("api/events/{eventId}")
    suspend fun getEventWithCounts(
        @Path("eventId") eventId: Long,
        @Header("Zwift-Api-Version") apiVersion: String = "2.5",
    ): ZwiftEventDto
}
