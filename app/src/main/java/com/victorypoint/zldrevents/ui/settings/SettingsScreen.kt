package com.victorypoint.zldrevents.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.victorypoint.zldrevents.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val logoutConfirm by viewModel.logoutConfirmVisible.collectAsState()
    val clearConfirm  by viewModel.clearConfirmVisible.collectAsState()
    var helpVisible   by remember { mutableStateOf(false) }
    var aboutVisible  by remember { mutableStateOf(false) }

    if (logoutConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLogoutConfirm,
            title = { Text("Log out?") },
            text  = { Text("You will need to sign in again to view events.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissLogoutConfirm()
                    viewModel.logout()
                    onLoggedOut()
                }) { Text("Log out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissLogoutConfirm) { Text("Cancel") }
            },
        )
    }

    if (clearConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearConfirm,
            title = { Text("Clear cached data?") },
            text  = { Text("This will remove all cached events. The list will reload on next refresh.") },
            confirmButton = {
                TextButton(onClick = viewModel::clearCache) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissClearConfirm) { Text("Cancel") }
            },
        )
    }

    if (helpVisible) {
        AlertDialog(
            onDismissRequest = { helpVisible = false },
            title = { Text("Help") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HelpSection("Events screen") {
                        HelpItem("Cycling / Running tabs", "Switch between ZLDR cycling and running events")
                        HelpItem("Registered count", "Number of riders signed up — fetched from Zwift's authenticated endpoint for accuracy")
                        HelpItem("Refresh button", "Tap to reload events from Zwift; appears in the top bar")
                    }
                    HelpSection("Event detail") {
                        HelpItem("Tap an event", "Opens full detail — route, distance, description, subgroups, and sign-up counts")
                        HelpItem("Subgroups", "Each category (A/B/C/D) shown with its distance and elevation")
                    }
                    HelpSection("Account") {
                        HelpItem("Log out", "Clears your stored tokens; you will need to sign in again")
                        HelpItem("Clear cached data", "Removes locally cached events; they reload on next refresh")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { helpVisible = false }) { Text("OK") }
            },
        )
    }

    if (aboutVisible) {
        AlertDialog(
            onDismissRequest = { aboutVisible = false },
            title = { Text("ZLDR Events") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingsRow(label = "Version",    value = BuildConfig.VERSION_NAME)
                    SettingsRow(label = "Build date", value = BuildConfig.BUILD_DATE)
                    Text(
                        text  = "Developed by Alan Udell for the ZLDR community",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                    Text(
                        text  = "This app relies on unofficial, unpublished Zwift APIs that are not officially supported. Zwift may change or remove them at any time without notice.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { aboutVisible = false }) { Text("OK") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SectionHeader("About")
            OutlinedButton(
                onClick  = { helpVisible = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Help")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick  = { aboutVisible = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("About")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            SectionHeader("Zwift Account")
            SettingsRow(label = "Signed in as", value = viewModel.username ?: "Unknown")
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick  = viewModel::requestLogout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Log Out")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            SectionHeader("Cache")
            OutlinedButton(
                onClick  = viewModel::requestClearCache,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border   = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)),
            ) {
                Text("Clear Cached Data")
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HelpSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text  = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}

@Composable
private fun HelpItem(label: String, description: String) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        Text(description, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), lineHeight = 16.sp)
    }
}
