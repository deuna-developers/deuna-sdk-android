package com.deuna.compose_demo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.deuna.compose_demo.*


@Composable
fun SuccessScreen(message: String) {
  val navController = LocalNavController.current

  Scaffold { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = message)
      Box(modifier = Modifier.height(20.dp))

      ElevatedButton(
        onClick = {
          // pop the current screen
          navController.popBackStack()
        },
      ) {
        Text(text = "Go back!")
      }
    }
  }
}


@Preview
@Composable
private fun Preview() {
  Navigator {
    SuccessScreen(message = "Payment successful!")
  }
}