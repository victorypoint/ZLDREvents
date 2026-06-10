package com.victorypoint.zldrevents.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventsViewModel,
    onEventClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val cyclingState by viewModel.cyclingState.collectAsStateWithLifecycle()
    val runningState by viewModel.runningState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(viewModel.savedSelectedTab) }
    LaunchedEffect(selectedTab) { viewModel.saveSelectedTab(selectedTab) }
    val isLoading = cyclingState is EventsUiState.Loading || runningState is EventsUiState.Loading
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialized from ViewModel so position survives navigation to detail and back.
    val runningListState = remember { LazyListState(viewModel.savedRunningScrollIndex, viewModel.savedRunningScrollOffset) }
    val cyclingListState = remember { LazyListState(viewModel.savedCyclingScrollIndex, viewModel.savedCyclingScrollOffset) }
    DisposableEffect(runningListState) {
        onDispose { viewModel.saveRunningScroll(runningListState.firstVisibleItemIndex, runningListState.firstVisibleItemScrollOffset) }
    }
    DisposableEffect(cyclingListState) {
        onDispose { viewModel.saveCyclingScroll(cyclingListState.firstVisibleItemIndex, cyclingListState.firstVisibleItemScrollOffset) }
    }

    val tabs = listOf(
        "Running" to Icons.AutoMirrored.Filled.DirectionsRun,
        "Cycling" to Icons.AutoMirrored.Filled.DirectionsBike,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZLDR Events") },
                actions = {
                    if (isLoading) {
                        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(title)
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
            }

            when (selectedTab) {
                0 -> RunningTab(
                    uiState = runningState,
                    onEventClick = onEventClick,
                    snackbarHostState = snackbarHostState,
                    listState = runningListState,
                )
                1 -> EventTab(
                    emptyMessage = "No upcoming ZLDR cycling events",
                    uiState = cyclingState,
                    onEventClick = onEventClick,
                    snackbarHostState = snackbarHostState,
                    listState = cyclingListState,
                )
            }
        }
    }
}
