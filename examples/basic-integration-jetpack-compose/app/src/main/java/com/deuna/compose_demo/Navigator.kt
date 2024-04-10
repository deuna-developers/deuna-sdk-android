package com.deuna.compose_demo

import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.*

sealed class Screens(val route: String) {
  data object Home : Screens(route = "/home")
  data object Success : Screens(route = "/success/{message}")
}

val LocalNavController = compositionLocalOf<NavHostController> {
  error("NavHostController not found")
}


@Composable
fun Navigator(content: @Composable () -> Unit) {
  // use CompositionLocalProvider to inject a NavHostController for screen navigation
  CompositionLocalProvider(
    LocalNavController provides rememberNavController(),
  ) {
    content()
  }
}