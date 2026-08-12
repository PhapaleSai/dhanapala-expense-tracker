package com.phapalesai.dhanapala.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phapalesai.dhanapala.ui.screens.accounts.AccountsScreen
import com.phapalesai.dhanapala.ui.screens.analytics.AnalyticsScreen
import com.phapalesai.dhanapala.ui.screens.brokeometer.BrokeOMeterScreen
import com.phapalesai.dhanapala.ui.screens.chat.DhanpalChatScreen
import com.phapalesai.dhanapala.ui.screens.home.HomeScreen
import com.phapalesai.dhanapala.ui.screens.messages.MessagesScreen
import com.phapalesai.dhanapala.ui.screens.panic.PanicButtonScreen
import com.phapalesai.dhanapala.ui.screens.settings.SettingsScreen
import com.phapalesai.dhanapala.ui.screens.split.SplitCalculatorScreen
import com.phapalesai.dhanapala.ui.screens.splitgroups.SplitGroupsScreen
import com.phapalesai.dhanapala.ui.screens.timemachine.TimeMachineScreen
import com.phapalesai.dhanapala.ui.screens.transactions.TransactionsScreen
import com.phapalesai.dhanapala.util.ShakeDetector

object Routes {
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val MESSAGES = "messages"
    const val SPLIT_CALCULATOR = "split_calculator"
    const val ACCOUNTS = "accounts"
    const val PANIC_BUTTON = "panic_button"
    const val BROKE_O_METER = "broke_o_meter"
    const val DHANPAL_CHAT = "dhanpal_chat"
    const val TIME_MACHINE = "time_machine"
    const val SPLIT_GROUPS = "split_groups"
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Filled.Home),
    BottomTab(Routes.TRANSACTIONS, "Transactions", Icons.Filled.ReceiptLong),
    BottomTab(Routes.ANALYTICS, "Analytics", Icons.Filled.BarChart),
    BottomTab(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
)

@Composable
fun DhanapalaNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // A hard shake anywhere in the app pops up the Broke-o-Meter — a fun,
    // read-only gut-check, never triggered from a screen that has unsaved input.
    DisposableEffect(Unit) {
        val detector = ShakeDetector(context) {
            navController.navigate(Routes.BROKE_O_METER)
        }
        detector.start()
        onDispose { detector.stop() }
    }

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
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 10 } },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { it / 10 } }
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onAddTransaction = { navController.navigate(Routes.TRANSACTIONS) },
                    onPanicButton = { navController.navigate(Routes.PANIC_BUTTON) }
                )
            }
            composable(Routes.TRANSACTIONS) { TransactionsScreen() }
            composable(Routes.ANALYTICS) { AnalyticsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onViewRawSms = { navController.navigate(Routes.MESSAGES) },
                    onSplitBill = { navController.navigate(Routes.SPLIT_CALCULATOR) },
                    onManageAccounts = { navController.navigate(Routes.ACCOUNTS) },
                    onOpenChat = { navController.navigate(Routes.DHANPAL_CHAT) },
                    onOpenTimeMachine = { navController.navigate(Routes.TIME_MACHINE) },
                    onOpenSplitGroups = { navController.navigate(Routes.SPLIT_GROUPS) }
                )
            }
            composable(Routes.MESSAGES) { MessagesScreen() }
            composable(Routes.SPLIT_CALCULATOR) { SplitCalculatorScreen() }
            composable(Routes.ACCOUNTS) { AccountsScreen() }
            composable(Routes.PANIC_BUTTON) { PanicButtonScreen(onDone = { navController.popBackStack() }) }
            composable(Routes.BROKE_O_METER) { BrokeOMeterScreen(onDismiss = { navController.popBackStack() }) }
            composable(Routes.DHANPAL_CHAT) { DhanpalChatScreen() }
            composable(Routes.TIME_MACHINE) { TimeMachineScreen() }
            composable(Routes.SPLIT_GROUPS) { SplitGroupsScreen() }
        }
    }
}
