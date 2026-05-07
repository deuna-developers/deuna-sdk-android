package com.deuna.explore.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deuna.explore.presentation.ExploreViewModel
import com.deuna.explore.presentation.NavigationEvent
import com.deuna.explore.presentation.screens.main.MainScreen
import com.deuna.explore.presentation.screens.result.CardSavedSuccessScreen
import com.deuna.explore.presentation.screens.result.PaymentSuccessScreen
import com.deuna.explore.presentation.screens.wallets.WalletsScreen
import com.deuna.maven.shared.toMap
import org.json.JSONObject

private enum class Routes(val route: String) {
    MAIN("main"),
    PAYMENT_SUCCESS("payment-success/{json}"),
    CARD_SAVED_SUCCESS("card-saved-success/{json}"),
    WALLETS("wallets/{orderToken}"),
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: ExploreViewModel = viewModel(factory = ExploreViewModel.Factory(context))

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.PaymentSuccess -> {
                    val encoded = Uri.encode(event.orderJson)
                    navController.navigate("payment-success/$encoded")
                }
                is NavigationEvent.CardSavedSuccess -> {
                    val encoded = Uri.encode(event.cardJson)
                    navController.navigate("card-saved-success/$encoded")
                }
                is NavigationEvent.OpenWallets -> {
                    val token = Uri.encode(event.orderToken ?: "")
                    navController.navigate("wallets/$token")
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.MAIN.route) {
        composable(Routes.MAIN.route) {
            MainScreen(viewModel = viewModel)
        }

        composable(
            route = Routes.PAYMENT_SUCCESS.route,
            arguments = listOf(navArgument("json") { type = NavType.StringType }),
        ) { backStack ->
            val jsonStr = backStack.arguments?.getString("json") ?: "{}"
            PaymentSuccessScreen(
                json = JSONObject(jsonStr).toMap(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.CARD_SAVED_SUCCESS.route,
            arguments = listOf(navArgument("json") { type = NavType.StringType }),
        ) { backStack ->
            val jsonStr = backStack.arguments?.getString("json") ?: "{}"
            CardSavedSuccessScreen(
                cardData = JSONObject(jsonStr).toMap(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.WALLETS.route,
            arguments = listOf(navArgument("orderToken") { type = NavType.StringType }),
        ) { backStack ->
            val encodedToken = backStack.arguments?.getString("orderToken") ?: ""
            val orderToken = Uri.decode(encodedToken).ifEmpty { null }
            WalletsScreen(
                deunaSDK = viewModel.deunaSDK,
                orderToken = orderToken,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
