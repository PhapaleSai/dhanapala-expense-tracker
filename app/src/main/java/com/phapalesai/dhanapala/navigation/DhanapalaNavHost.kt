package com.phapalesai.dhanapala.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.phapalesai.dhanapala.ui.screens.home.HomeScreen

object Routes {
    const val HOME = "home"
}

@Composable
fun DhanapalaNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen() }
    }
}
