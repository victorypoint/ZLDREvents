package com.victorypoint.zldrevents.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.victorypoint.zldrevents.data.model.EventSubgroup
import com.victorypoint.zldrevents.data.model.Sport
import com.victorypoint.zldrevents.data.model.ZwiftEvent
import com.victorypoint.zldrevents.ui.theme.ZldrBlue
import com.victorypoint.zldrevents.ui.theme.ZldrOrange
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy · h:mm a z")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is DetailUiState.Error) {
            snackbarHostState.showSnackbar((uiState as DetailUiState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is DetailUiState.Success -> {
                    EventDetail(state.event)
                }
                is DetailUiState.Error -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(state.message)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::retry) { Text("Retry") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDetail(event: ZwiftEvent) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (event.sport) {
                        Sport.CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
                        Sport.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
                        Sport.OTHER -> Icons.Default.Event
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        item {
            event.eventStart?.let { start ->
                val local = start.atZone(ZoneId.systemDefault())
                InfoRow(label = "Starts", value = DATE_FORMATTER.format(local))
            }
        }

        event.routeName?.let { route ->
            item { InfoRow(label = "Route", value = route) }
        }

        val hasDistance = (event.distanceInMeters ?: 0.0) > 0.0
        val hasDuration = (event.durationInSeconds ?: 0) > 0
        val hasLaps    = (event.laps ?: 0) > 0
        if (hasDistance || hasDuration || hasLaps) {
            item {
                val parts = mutableListOf<String>()
                if (hasDistance) parts += "%.1f km".format(event.distanceInMeters!! / 1000.0)
                if (hasLaps)     parts += "${event.laps} laps"
                if (hasDuration) {
                    val s = event.durationInSeconds!!
                    val h = s / 3600; val m = (s % 3600) / 60
                    parts += if (h > 0) "${h}h ${m}m" else "${m}m"
                }
                val label = when {
                    hasDistance && hasDuration -> "Distance / Duration"
                    hasDistance || hasLaps     -> "Distance"
                    else                       -> "Duration"
                }
                InfoRow(label = label, value = parts.joinToString(" · "))
            }
        }

        item {
            val context = LocalContext.current
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zwift.com/events/view/${event.id}"))
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZldrOrange),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Join")
                }
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://zwiftpower.com/events.php?zid=${event.id}"))
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZldrBlue),
                ) {
                    Text("ZwiftPower")
                }
            }
        }

        if (event.description.isNotBlank()) {
            item {
                Column {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (event.subgroups.isNotEmpty()) {
            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items(event.subgroups) { subgroup ->
                SubgroupRow(subgroup)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SubgroupRow(subgroup: EventSubgroup) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (subgroup.label.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = subgroup.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                if (subgroup.name.isNotBlank() && subgroup.name != subgroup.label) {
                    Text(subgroup.name, style = MaterialTheme.typography.bodySmall)
                }
                val detail = buildList {
                    subgroup.distanceInMeters?.let { add("%.1f km".format(it / 1000.0)) }
                    subgroup.laps?.let { if (it > 0) add("$it laps") }
                }.joinToString(" · ")
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
            val riderCount = maxOf(subgroup.totalEntrantCount, subgroup.signedUpCount)
            if (riderCount > 0) {
                Text(
                    text = if (riderCount == 1) "1 participant" else "$riderCount participants",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}
