package com.zerodev.clen.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zerodev.clen.R
import com.zerodev.clen.presentation.home.HomeScreen
import com.zerodev.clen.presentation.onboarding.OnboardingScreen
import com.zerodev.clen.presentation.scan.ScanResultsScreen
import com.zerodev.clen.presentation.scan.ScanScreen
import com.zerodev.clen.presentation.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun ClenApp(
    onboardingCompleted: Boolean,
    onOnboardingCompleted: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomNavItems = listOf(
        TopLevelDestination(
            route = ClenRoute.Home.route,
            labelResId = R.string.home_title,
            icon = Icons.Outlined.Home,
        ),
        TopLevelDestination(
            route = ClenRoute.Scan.route,
            labelResId = R.string.scan_title,
            icon = Icons.Outlined.Search,
        ),
        TopLevelDestination(
            route = ClenRoute.Settings.route,
            labelResId = R.string.settings_title,
            icon = Icons.Outlined.Settings,
        ),
    )
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination.isRouteInHierarchy(item.route)
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar) {
                ClenNavigationBar(
                    destinations = bottomNavItems,
                    currentDestination = currentDestination,
                    onDestinationClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingCompleted) {
                ClenRoute.Home.route
            } else {
                ClenRoute.Onboarding.route
            },
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ClenRoute.Onboarding.route) {
                OnboardingScreen(
                    onCompleteClick = {
                        coroutineScope.launch {
                            onOnboardingCompleted()
                            navController.navigate(ClenRoute.Home.route) {
                                popUpTo(ClenRoute.Onboarding.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }
            composable(ClenRoute.Home.route) {
                HomeScreen(
                    onQuickCleanClick = {
                        navController.navigate(ClenRoute.Scan.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(ClenRoute.Scan.route) {
                ScanScreen(
                    onViewResultsClick = {
                        navController.navigate(ClenRoute.ScanResults.route)
                    },
                )
            }
            composable(ClenRoute.ScanResults.route) {
                ScanResultsScreen()
            }
            composable(ClenRoute.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun ClenNavigationBar(
    destinations: List<TopLevelDestination>,
    currentDestination: NavDestination?,
    onDestinationClick: (String) -> Unit,
) {
    NavigationBar {
        destinations.forEach { destination ->
            val selected = currentDestination.isRouteInHierarchy(destination.route)
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationClick(destination.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(text = stringResource(destination.labelResId))
                },
            )
        }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: String): Boolean =
    this?.hierarchy?.any { destination -> destination.route == route } == true

private data class TopLevelDestination(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector,
)
