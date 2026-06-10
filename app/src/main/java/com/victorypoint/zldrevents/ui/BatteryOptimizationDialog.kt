package com.victorypoint.zldrevents.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val isSamsung = Build.MANUFACTURER.equals("samsung", ignoreCase = true)

@Composable
fun BatteryOptimizationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val samsungNote = if (isSamsung) {
        "\n\nSamsung note: also go to Settings → Battery → Background usage limits → " +
        "Sleeping apps and remove ZLDR Events from the list if it appears there."
    } else ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unrestricted Network Access") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "ZLDR Events loads upcoming events from Zwift each time the app is opened. " +
                    "Some Android devices restrict app network access to save battery, which can " +
                    "cause the event list to load slowly or fail to refresh." +
                    "\n\nTap Open Settings and select \"Allow\" to ensure events always load reliably.$samsungNote"
                )
                Text(
                    "You can change this at any time in your device's battery settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Open Settings") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        },
    )
}
