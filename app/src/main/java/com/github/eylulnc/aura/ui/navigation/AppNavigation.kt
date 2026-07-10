package com.github.eylulnc.aura.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.ui.components.FullScreenLoading
import com.github.eylulnc.aura.ui.events.MoodLoggerEvents
import com.github.eylulnc.aura.ui.history.HistoryScreen
import com.github.eylulnc.aura.ui.onboarding.LoginScreen
import com.github.eylulnc.aura.ui.settings.DataPrivacyScreen
import com.github.eylulnc.aura.ui.settings.SettingsScreen
import com.github.eylulnc.aura.ui.settings.SettingsViewModel
import com.github.eylulnc.aura.ui.theme.auraColors
import com.github.eylulnc.aura.ui.home.TodayScreen

sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    data object Today : Screen("today", R.string.tab_today, Icons.Default.Home)
    data object History : Screen("history", R.string.tab_history, Icons.Default.DateRange)
    data object Settings : Screen("settings", R.string.tab_settings, Icons.Default.Settings)
}

private val tabs = listOf(Screen.Today, Screen.History, Screen.Settings)

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_MAIN = "main"
private const val ROUTE_DATA_PRIVACY = "data_privacy"

@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel, showOnboarding: Boolean) {
    val rootNavController = rememberNavController()
    val isSyncing by settingsViewModel.isSyncing.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = rootNavController,
            startDestination = if (showOnboarding) ROUTE_ONBOARDING else ROUTE_MAIN,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(ROUTE_ONBOARDING) {
                LoginScreen(
                    viewModel = settingsViewModel,
                    onComplete = {
                        rootNavController.navigate(ROUTE_MAIN) {
                            popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(ROUTE_MAIN) {
                val navigateToOnboarding = {
                    rootNavController.navigate(ROUTE_ONBOARDING) {
                        popUpTo(ROUTE_MAIN) { inclusive = true }
                    }
                }
                MainScaffold(
                    settingsViewModel = settingsViewModel,
                    onSignedOut = navigateToOnboarding,
                    onAccountDeleted = navigateToOnboarding
                )
            }
        }

        if (isSyncing) {
            FullScreenLoading(message = stringResource(R.string.loading_working))
        }
    }
}

@Composable
private fun MainScaffold(
    settingsViewModel: SettingsViewModel,
    onSignedOut: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val navController = rememberNavController()
    val colors = auraColors
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val openMoodRequest by MoodLoggerEvents.openRequest.collectAsState()
    var lastHandledOpenRequest by remember { mutableStateOf(0L) }
    LaunchedEffect(openMoodRequest) {
        if (openMoodRequest != 0L && openMoodRequest != lastHandledOpenRequest) {
            lastHandledOpenRequest = openMoodRequest
            navController.navigate(Screen.Today.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                tonalElevation = 0.dp
            ) {
                tabs.forEach { screen ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(screen.labelRes)
                            )
                        },
                        label = { Text(stringResource(screen.labelRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.textSecondary,
                            unselectedTextColor = colors.textSecondary,
                            indicatorColor = colors.surfaceSubtle
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Today.route) { TodayScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onSignedOut = onSignedOut,
                    onNavigateToDataPrivacy = { navController.navigate(ROUTE_DATA_PRIVACY) }
                )
            }
            composable(ROUTE_DATA_PRIVACY) {
                DataPrivacyScreen(
                    viewModel = settingsViewModel,
                    onAccountDeleted = onAccountDeleted,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
