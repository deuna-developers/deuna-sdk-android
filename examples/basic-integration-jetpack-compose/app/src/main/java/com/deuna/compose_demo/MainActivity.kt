package com.deuna.compose_demo

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.navigation.compose.*
import com.deuna.compose_demo.screens.*
import com.deuna.compose_demo.view_models.*
import com.deuna.maven.*
import com.deuna.maven.shared.Environment
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
                                    publicApiKey = "YOUR_PUBLIC_API_KEY"
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
                        VaultSuccessfulScreen(
                            card = JSONObject( it.arguments?.getString("jsonCard")!!).toMap()
                        )
                    }
                }
            }
        }
    }
}

