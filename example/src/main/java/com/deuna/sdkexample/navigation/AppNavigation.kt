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
import com.deuna.sdkexample.ui.screens.embedded.EmbeddedWidgetScreen
import com.deuna.sdkexample.ui.screens.embedded.EmbeddedWidgetScreenParams
import com.deuna.sdkexample.ui.screens.main.MainScreen
import com.deuna.sdkexample.ui.screens.main.WidgetToShow
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel
import com.deuna.sdkexample.ui.screens.success.CardSavedSuccessfulScreen
import com.deuna.sdkexample.ui.screens.success.PaymentSuccessfulScreen
import org.json.JSONObject


enum class AppRoutes(val route: String) {
    MAIN("main"),
    EMBEDDED("embedded/{orderToken}/{userToken}/{widgetToShow}"),
    PAYMENT_SUCCESS("payment-success/{json}"),
    CARD_SAVED_SUCCESS("card-saved-success/{json}")
}


@Composable
fun AppNavigation(deunaSDK: DeunaSDK) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "main") {
        composable(AppRoutes.MAIN.route) {
            MainScreen(
                MainViewModel(
                    deunaSDK = deunaSDK,
                ),
                navController = navController,
            )
        }
        composable(
            AppRoutes.EMBEDDED.route,
            arguments = listOf(
                navArgument("orderToken") { type = NavType.StringType },
                navArgument("userToken") { type = NavType.StringType },
                navArgument("widgetToShow") { type = NavType.StringType }),
        ) { backStackEntry ->
            val orderToken = backStackEntry.arguments?.getString("orderToken") ?: ""
            val userToken = backStackEntry.arguments?.getString("userToken") ?: ""
            val widgetToShowStr = backStackEntry.arguments?.getString("widgetToShow") ?: ""

            val widgetToShow = try {
                WidgetToShow.valueOf(widgetToShowStr)
            } catch (e: IllegalArgumentException) {
                WidgetToShow.PAYMENT_WIDGET
            }

            EmbeddedWidgetScreen(
                params = EmbeddedWidgetScreenParams(
                    deunaSDK = deunaSDK,
                    orderToken = orderToken,
                    userToken = userToken,
                    widgetToShow = widgetToShow
                ),
                onSuccess = { data ->
                    val jsonStr = Uri.encode(JSONObject(data).toString())
                    when (widgetToShow) {
                        WidgetToShow.CHECKOUT_WIDGET, WidgetToShow.PAYMENT_WIDGET, WidgetToShow.NEXT_ACTION_WIDGET, WidgetToShow.VOUCHER_WIDGET -> {
                            navController.navigate("payment-success/$jsonStr") {
                                popUpTo(AppRoutes.EMBEDDED.route) {
                                    inclusive = true
                                }
                            }
                        }

                        WidgetToShow.VAULT_WIDGET, WidgetToShow.CLICK_TO_PAY_WIDGET -> {
                            navController.navigate("card-saved-success/$jsonStr") {
                                popUpTo(AppRoutes.EMBEDDED.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
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