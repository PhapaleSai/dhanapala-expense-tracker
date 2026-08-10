package com.phapalesai.dhanapala.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phapalesai.dhanapala.ui.screens.analytics.AnalyticsScreen
import com.phapalesai.dhanapala.ui.screens.home.HomeScreen
import com.phapalesai.dhanapala.ui.screens.messages.MessagesScreen
import com.phapalesai.dhanapala.ui.screens.settings.SettingsScreen
import com.phapalesai.dhanapala.ui.screens.transactions.TransactionsScreen

object Routes {
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val MESSAGES = "messages"
}

private data class BottomTab(val route: String, val label: String, val emoji: String)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", "🏠"),
    BottomTab(Routes.TRANSACTIONS, "Transactions", "💸"),
    BottomTab(Routes.ANALYTICS, "Analytics", "📊"),
    BottomTab(Routes.SETTINGS, "Settings", "⚙️")
)

@Composable
fun DhanapalaNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            NavigationBar {
                bottomTabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(tab.emoji) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) { HomeScreen() }
            composable(Routes.TRANSACTIONS) { TransactionsScreen() }
            composable(Routes.ANALYTICS) { AnalyticsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(onViewRawSms = { navController.navigate(Routes.MESSAGES) })
            }
            composable(Routes.MESSAGES) { MessagesScreen() }
        }
    }
}
