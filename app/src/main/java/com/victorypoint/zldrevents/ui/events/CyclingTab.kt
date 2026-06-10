package com.victorypoint.zldrevents.ui.events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.victorypoint.zldrevents.data.model.ZwiftEvent
import com.victorypoint.zldrevents.ui.theme.EventDayColors
import java.time.ZoneId

@Composable
fun EventTab(
    emptyMessage: String,
    uiState: EventsUiState,
    onEventClick: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    listState: LazyListState,
) {
    LaunchedEffect(uiState) {
        if (uiState is EventsUiState.Error) {
            snackbarHostState.showSnackbar(uiState.message)
        }
    }

    val events = (uiState as? EventsUiState.Success)?.events ?: emptyList()
    val eventBgColor: Map<Long, Color> = remember(events) { dayColorMap(events) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is EventsUiState.Loading -> {}
            is EventsUiState.Success -> {
                if (uiState.events.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(32.dp),
                        )
                    }
                } else {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        items(uiState.events, key = { it.id }) { event ->
                            EventListItem(
                                event = event,
                                onClick = { onEventClick(event.id) },
                                backgroundColor = eventBgColor[event.id] ?: EventDayColors[0],
                            )
                            HorizontalDivider(
                                thickness = 2.dp,
                                color = MaterialTheme.colorScheme.background,
                            )
                        }
                    }
                }
            }
            is EventsUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Tap refresh to retry",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

internal fun dayColorMap(events: List<ZwiftEvent>): Map<Long, Color> = buildMap {
    events.forEach { event ->
        // DayOfWeek.value: 1=Monday … 7=Sunday → index 0…6
        val dow = event.eventStart
            ?.atZone(ZoneId.systemDefault())
            ?.dayOfWeek?.value?.minus(1) ?: 0
        put(event.id, EventDayColors[dow])
    }
}
