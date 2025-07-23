package com.deuna.sdkexample.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.toMap
import com.deuna.sdkexample.ui.screens.main.MainScreen
import com.deuna.sdkexample.ui.screens.main.WidgetToShow
import com.deuna.sdkexample.ui.screens.success.CardSavedSuccessfulScreen
import com.deuna.sdkexample.ui.screens.success.PaymentSuccessfulScreen
import org.json.JSONObject


enum class AppRoutes(val route: String) {
    MAIN("main"),
    PAYMENT_SUCCESS("payment-success/{json}"),
    CARD_SAVED_SUCCESS("card-saved-success/{json}")
}


@Composable
fun AppNavigation(deunaSDK: DeunaSDK) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "main") {
        composable(AppRoutes.MAIN.route) {
            MainScreen(
                deunaSDK = deunaSDK,
                navController = navController,
            )
        }

        composable(
            AppRoutes.PAYMENT_SUCCESS.route,
            arguments = listOf(
                navArgument("json") { type = NavType.StringType }
            ),
        ) { backStackEntry ->
            val jsonStr = backStackEntry.arguments?.getString("json") ?: ""

            PaymentSuccessfulScreen(
                navController = navController, json = JSONObject(jsonStr).toMap()
            )
        }

        composable(
            AppRoutes.CARD_SAVED_SUCCESS.route,
            arguments = listOf(
                navArgument("json") { type = NavType.StringType }
            ),
        ) { backStackEntry ->
            val jsonStr = backStackEntry.arguments?.getString("json") ?: ""

            CardSavedSuccessfulScreen(
                navController = navController,
                title = "Card saved successfully",
                savedCardData = JSONObject(jsonStr).toMap()
            )
        }
    }
}