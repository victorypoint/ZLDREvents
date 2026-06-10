package com.victorypoint.zldrevents.ui.navigation

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.victorypoint.zldrevents.ZldrApplication
import com.victorypoint.zldrevents.ui.BatteryOptimizationDialog
import com.victorypoint.zldrevents.ui.detail.EventDetailScreen
import com.victorypoint.zldrevents.ui.detail.EventDetailViewModel
import com.victorypoint.zldrevents.ui.events.EventsScreen
import com.victorypoint.zldrevents.ui.events.EventsViewModel
import com.victorypoint.zldrevents.ui.login.LoginScreen
import com.victorypoint.zldrevents.ui.login.LoginViewModel
import com.victorypoint.zldrevents.ui.settings.SettingsScreen
import com.victorypoint.zldrevents.ui.settings.SettingsViewModel

private const val ROUTE_LOGIN = "login"
private const val ROUTE_EVENTS = "events"
private const val ROUTE_DETAIL = "detail/{eventId}"
private const val ROUTE_SETTINGS = "settings"

@Composable
fun ZldrNavGraph(app: ZldrApplication) {
    val navController = rememberNavController()
    val startDestination = if (app.tokenStore.hasRefreshToken()) ROUTE_EVENTS else ROUTE_LOGIN

    LaunchedEffect(Unit) {
        app.authRepository.sessionExpired.collect {
            navController.navigate(ROUTE_LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ROUTE_LOGIN) {
            val vm: LoginViewModel = viewModel { LoginViewModel(app.authRepository) }
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = {
                    navController.navigate(ROUTE_EVENTS) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(ROUTE_EVENTS) {
            val context = LocalContext.current
            val pm = context.getSystemService(PowerManager::class.java)
            var showBatteryDialog by remember {
                mutableStateOf(
                    !app.appPrefsStore.batteryPromptShown &&
                    !pm.isIgnoringBatteryOptimizations(context.packageName)
                )
            }
            if (showBatteryDialog) {
                BatteryOptimizationDialog(
                    onConfirm = {
                        app.appPrefsStore.batteryPromptShown = true
                        showBatteryDialog = false
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    },
                    onDismiss = {
                        app.appPrefsStore.batteryPromptShown = true
                        showBatteryDialog = false
                    },
                )
            }

            val vm: EventsViewModel = viewModel { EventsViewModel(app.eventsRepository) }
            EventsScreen(
                viewModel = vm,
                onEventClick = { eventId ->
                    navController.navigate("detail/$eventId")
                },
                onSettingsClick = {
                    navController.navigate(ROUTE_SETTINGS)
                },
            )
        }

        composable(ROUTE_SETTINGS) {
            val vm: SettingsViewModel = viewModel { SettingsViewModel(app.authRepository, app.eventsRepository) }
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments!!.getLong("eventId")
            val vm: EventDetailViewModel = viewModel { EventDetailViewModel(app.eventsRepository, eventId) }
            EventDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
