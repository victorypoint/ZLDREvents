package com.victorypoint.zldrevents.ui.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.victorypoint.zldrevents.data.model.Sport
import com.victorypoint.zldrevents.data.model.ZwiftEvent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a")

@Composable
fun EventListItem(event: ZwiftEvent, onClick: () -> Unit, backgroundColor: Color) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = backgroundColor),
        headlineContent = {
            Text(
                text = event.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Column {
                event.eventStart?.let { start ->
                    val local = start.atZone(ZoneId.systemDefault())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val hour = local.hour
                        val (icon, tint) = when {
                            hour < 12 -> Icons.Default.WbTwilight to Color(0xFFFFAB40)
                            hour < 18 -> Icons.Default.WbSunny    to Color(0xFFFFD600)
                            else      -> Icons.Default.Bedtime     to Color(0xFF90CAF9)
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = tint,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = DATE_FORMATTER.format(local),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
                val detail = buildEventDetail(event)
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        },
        leadingContent = {
            Icon(
                imageVector = when (event.sport) {
                    Sport.CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
                    Sport.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
                    Sport.OTHER -> Icons.Default.Event
                },
                contentDescription = event.sport.name,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingContent = {
            if (event.totalEntrantCount > 0) {
                Text(
                    text = "${event.totalEntrantCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        },
    )
}

private fun buildEventDetail(event: ZwiftEvent): String {
    val parts = mutableListOf<String>()
    event.eventType?.let { parts += formatEventType(it) }
    event.distanceInMeters?.let { parts += "%.1f km".format(it / 1000.0) }
    event.laps?.let { if (it > 0) parts += "$it laps" }
    event.durationInSeconds?.let { s ->
        val h = s / 3600; val m = (s % 3600) / 60
        parts += if (h > 0) "${h}h ${m}m" else "${m}m"
    }
    event.routeName?.takeIf { it.isNotBlank() }?.let { parts += it }
    val labeledGroups = event.subgroups.filter { it.label.isNotBlank() }.sortedBy { it.label }
    if (labeledGroups.isNotEmpty()) {
        parts += labeledGroups.joinToString("/") { it.label }
    } else {
        val total = event.subgroups.sumOf { maxOf(it.totalEntrantCount, it.signedUpCount) }
            .takeIf { it > 0 } ?: event.totalEntrantCount
        if (total > 0) parts += "$total signed up"
    }
    return parts.joinToString(" · ")
}

private fun formatEventType(type: String): String = when (type.uppercase()) {
    "RIDE"            -> "Group Ride"
    "TIME_TRIAL"      -> "Time Trial"
    "GROUP_WORKOUT"   -> "Group Workout"
    "RACE"            -> "Race"
    "GRAN_FONDO"      -> "Gran Fondo"
    "CRITERIUM"       -> "Criterium"
    else -> type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}
