package com.rabha.privacymonitor.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rabha.privacymonitor.auth.LoginScreen
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.dashboard.DashboardScreen
import com.rabha.privacymonitor.monitor.MonitorHubScreen
import com.rabha.privacymonitor.notifications.NotificationsScreen
import com.rabha.privacymonitor.settings.SettingsScreen
import com.rabha.privacymonitor.wallpaper.WallpapersScreen

private data class Dest(val route: String, val label: String, val icon: ImageVector)

private val destinations = listOf(
    Dest("home", "Home", Icons.Filled.Home),
    Dest("monitor", "Monitor", Icons.Filled.Monitor),
    Dest("wallpapers", "Wallpapers", Icons.Filled.Wallpaper),
    Dest("notifications", "Alerts", Icons.Filled.Notifications),
    Dest("settings", "Settings", Icons.Filled.Settings)
)

@Composable
internal fun RabhaApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = RabhaColors.Black,
        bottomBar = {
            if (currentRoute != "account") {
                NavigationBar(containerColor = RabhaColors.Surface) {
                    destinations.forEach { dest ->
                        val selected = backStackEntry?.destination?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RabhaColors.Amber,
                                selectedTextColor = RabhaColors.Amber,
                                unselectedIconColor = RabhaColors.TextSecondary,
                                unselectedTextColor = RabhaColors.TextSecondary,
                                indicatorColor = RabhaColors.SurfaceHigh
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    DashboardScreen(
                        onOpenNetwork = { navController.navigate("monitor") },
                        onOpenUsage = { navController.navigate("monitor") }
                    )
                }
                composable("monitor") { MonitorHubScreen() }
                composable("wallpapers") { WallpapersScreen() }
                composable("notifications") { NotificationsScreen() }
                composable("settings") {
                    SettingsScreen(onOpenAccount = { navController.navigate("account") })
                }
                composable("account") {
                    LoginScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
