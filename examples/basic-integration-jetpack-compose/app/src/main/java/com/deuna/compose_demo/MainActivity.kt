package com.deuna.compose_demo

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.navigation.compose.*
import com.deuna.compose_demo.screens.*
import com.deuna.compose_demo.view_models.*
import com.deuna.maven.*
import com.deuna.maven.shared.Environment

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
                  environment = Environment.STAGING,
                  publicApiKey = "77c319962031a1762d98b5a5dff8916c0070a71b747a4104fc88e3f1269653ccfc2b49a6f224af3aa7c0677b8d9210099480d48b7f3461a95efb39117c00"
                ),
              )
            )
          }
          // Define the composable function for the success screen
          composable(Screens.Success.route) {
            SuccessScreen(message = it.arguments?.getString("message") ?: "Thank You")
          }
        }
      }
    }
  }
}

