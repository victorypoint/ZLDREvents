package com.victorypoint.zldrevents.ui.events

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun RunningTab(
    uiState: EventsUiState,
    onEventClick: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    listState: LazyListState,
) = EventTab(
    emptyMessage = "No upcoming ZLDR running events",
    uiState = uiState,
    onEventClick = onEventClick,
    snackbarHostState = snackbarHostState,
    listState = listState,
)
