package com.victorypoint.zldrevents.data.events

import android.util.Log
import com.victorypoint.zldrevents.data.events.dto.ZwiftEventDto
import com.victorypoint.zldrevents.data.model.Sport
import com.victorypoint.zldrevents.data.model.ZwiftEvent
import com.victorypoint.zldrevents.data.model.toDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val TAG = "EventsRepository"
private const val PAGE_SIZE = 200
private const val MAX_NAME_SCAN_PAGES = 5

class EventsRepository(private val eventsApi: EventsApi) {

    private val mutex = Mutex()
    private var cache: List<ZwiftEvent>? = null

    suspend fun getCyclingEvents(): Result<List<ZwiftEvent>> =
        withContext(Dispatchers.IO) { runCatching { cachedEvents().filter { it.sport == Sport.CYCLING } } }

    suspend fun getRunningEvents(): Result<List<ZwiftEvent>> =
        withContext(Dispatchers.IO) { runCatching { cachedEvents().filter { it.sport == Sport.RUNNING } } }

    suspend fun invalidateAndRefresh() {
        mutex.withLock { cache = null }
    }

    suspend fun getEventDetail(eventId: Long): Result<ZwiftEvent> =
        withContext(Dispatchers.IO) {
            runCatching {
                mutex.withLock { cache }?.find { it.id == eventId }
                    ?: eventsApi.getEvent(eventId).toDomain()
            }
        }

    private suspend fun cachedEvents(): List<ZwiftEvent> =
        mutex.withLock { cache ?: fetchAllZldrFromFeed().also { cache = it } }

    private suspend fun fetchAllZldrFromFeed(): List<ZwiftEvent> {
        // Discovery: concurrent tag queries (primary) + paginated name-prefix scan (fallback).
        val rawDtos = withContext(Dispatchers.IO) {
            coroutineScope {
                val zldrDeferred = async {
                    runCatching { eventsApi.getUpcomingEvents(tags = "zldr") }.getOrElse {
                        Log.w(TAG, "tags=zldr fetch failed: ${it.message}"); emptyList()
                    }
                }
                val zldrIdersDeferred = async {
                    runCatching { eventsApi.getUpcomingEvents(tags = "zldriders") }.getOrElse {
                        Log.w(TAG, "tags=zldriders fetch failed: ${it.message}"); emptyList()
                    }
                }
                // Name-prefix scan runs concurrently; catches events missing the tag.
                val nameScanDeferred = async { namePrefixScan() }

                val zldr        = zldrDeferred.await()
                val zldrIders   = zldrIdersDeferred.await()
                val nameMatches = nameScanDeferred.await()

                val taggedIds = (zldr + zldrIders).map { it.id }.toHashSet()
                val extras    = nameMatches.filter { it.id !in taggedIds }
                if (extras.isNotEmpty()) {
                    Log.d(TAG, "Name scan found ${extras.size} untagged ZLDR event(s): " +
                            extras.joinToString { "'${it.name}' (${it.id})" })
                }

                val seen = mutableSetOf<Long>()
                (zldr + zldrIders + nameMatches)
                    .filter { seen.add(it.id) }
                    .sortedBy { it.eventStart }
            }
        }

        // Resolve open question: does the discovery endpoint already return routeName?
        val withRouteName = rawDtos.count { !it.routeName.isNullOrBlank() }
        Log.d(TAG, "Discovery: ${rawDtos.size} events — routeName populated: $withRouteName/${rawDtos.size}")

        return withContext(Dispatchers.IO) {
            enrichWithCounts(rawDtos.map { it.toDomain() })
        }
    }

    // Separate function so break/continue are not inside inline-lambda context.
    private suspend fun namePrefixScan(): List<ZwiftEventDto> {
        val result = mutableListOf<ZwiftEventDto>()
        var start = 0
        for (page in 0 until MAX_NAME_SCAN_PAGES) {
            val pageData = try {
                eventsApi.getUpcomingEvents(start = start)
            } catch (e: Exception) {
                Log.w(TAG, "Name scan page $page failed: ${e.message}")
                break
            }
            result.addAll(pageData.filter {
                it.name?.startsWith("ZLDR", ignoreCase = true) == true
            })
            if (pageData.size < PAGE_SIZE) break
            start += PAGE_SIZE
        }
        return result
    }

    // Calls the non-public api/events/{id} for each event to get:
    //   • totalSignedUpCount (accurate; public API always returns 0)
    //   • routeName (fills in if missing from the discovery response)
    //   • Complete subgroup data with per-category signedUpCounts
    // Degrades gracefully: if a call fails, the event is kept with discovery-response values.
    private suspend fun enrichWithCounts(events: List<ZwiftEvent>): List<ZwiftEvent> {
        if (events.isEmpty()) return events
        Log.d(TAG, "Enriching ${events.size} events via api/events/{id}…")
        return coroutineScope {
            events.map { event ->
                async {
                    try {
                        val detail = eventsApi.getEventWithCounts(event.id).toDomain()
                        event.copy(
                            totalEntrantCount = detail.totalEntrantCount
                                .takeIf { it > 0 } ?: event.totalEntrantCount,
                            routeName = detail.routeName?.takeIf { it.isNotBlank() }
                                ?: event.routeName,
                            subgroups = detail.subgroups.ifEmpty { event.subgroups },
                        )
                    } catch (_: Exception) {
                        event
                    }
                }
            }.awaitAll()
        }.also { Log.d(TAG, "Enrichment complete") }
    }
}
