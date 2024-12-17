package com.deuna.compose_demo

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.deuna.compose_demo.screens.*
import com.deuna.compose_demo.view_models.home.HomeViewModel
import com.deuna.maven.*
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toMap
import org.json.JSONObject

class MainActivity() : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Define the navigation for the app
            Navigator {
                // Set up the navigation host, which manages navigation within the app
                NavHost(
                    navController = LocalNavController.current,
                    startDestination = Screens.Home.route // Set the start destination to the home screen
                ) {
                    // Define the composable function for the home screen
                    composable(Screens.Home.route) {
                        HomeScreen(
                            // Initialize the view model with DeunaSDK configuration for sandbox environment
                            homeViewModel = HomeViewModel(
                                deunaSDK = DeunaSDK(
                                    environment = Environment.SANDBOX,
                                    publicApiKey = "YOUR_PUBLIC_API_KEY",
                                ),
                            )
                        )
                    }
                    // Define the composable function for the success screen
                    composable(Screens.PaymentSuccessful.route) {
                        PaymentSuccessfulScreen(
                            json = JSONObject(it.arguments?.getString("jsonOrder")!!).toMap(),
                        )
                    }
                    composable(Screens.SavedCardSuccessful.route) {
                        NavigateToCardSavedSuccessfulScreen(it.arguments)
                    }
                    composable(Screens.ClickToPaySuccessful.route) {
                        NavigateToCardSavedSuccessfulScreen(it.arguments)
                    }
                }
            }
        }
    }
}

@Composable
fun NavigateToCardSavedSuccessfulScreen(arguments: Bundle?) {
    val data = JSONObject(
        arguments?.getString("data")!!
    ).toMap()

    CardSavedSuccessfulScreen(
        title = data["title"] as String,
        savedCardData = data["savedCardData"] as Json
    )
}