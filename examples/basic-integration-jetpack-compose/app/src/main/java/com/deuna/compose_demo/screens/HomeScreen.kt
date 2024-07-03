package com.deuna.compose_demo.screens

import android.annotation.*
import android.content.*
import android.net.*
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import com.deuna.compose_demo.*
import com.deuna.compose_demo.view_models.*
import com.deuna.maven.*
import com.deuna.maven.shared.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {

  // Retrieve the NavController from the composition's LocalNavController
  // use it for navigation
  val navController = LocalNavController.current

  // Retrieve the user token and order token states from the view model
  val userTokenState = homeViewModel.userToken
  val orderTokenState = homeViewModel.orderToken

  // Retrieve the Context from the composition's LocalContext
  val context = LocalContext.current

  // Function to perform payment and navigate to the success screen upon successful payment
  fun performPayment() {
    homeViewModel.payment(context = context, completion = { result ->
      when (result) {
        is CheckoutResult.Canceled -> Log.d("PAYMENT", "Canceled")
        is CheckoutResult.Error -> Log.d("PAYMENT ERROR", result.error.type.message)
        is CheckoutResult.Success -> navController.navigate(
          "/success/${Uri.encode("Payment successful!")}"
        )
      }
    })
  }

  // Function to save card and navigate to the success screen upon successful card saving
  fun saveCard() {
    homeViewModel.saveCard(context = context, completion = { result ->
      when (result) {
        is ElementsResult.Canceled ->  Log.d("SAVING CARD", "Canceled")
        is ElementsResult.Error -> Log.d("SAVING CARD ERROR", result.error.type.message)
        is ElementsResult.Success -> navController.navigate(
          "/success/${Uri.encode("Save Card successful!")}"
        )
      }
    })
  }

  // Build the UI using Scaffold and Column composables
  Scaffold {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      OutlinedTextField(modifier = Modifier.fillMaxWidth(),
        value = orderTokenState.value,
        onValueChange = { orderTokenState.value = it },
        label = { Text("Order Token") })

      OutlinedTextField(modifier = Modifier.fillMaxWidth(),
        value = userTokenState.value,
        onValueChange = { userTokenState.value = it },
        label = { Text("User Token") })

      Box(modifier = Modifier.height(20.dp))

      ElevatedButton(modifier = Modifier.fillMaxWidth(), onClick = { performPayment() }) {
        Text(text = "Start Payment")
      }

      ElevatedButton(modifier = Modifier.fillMaxWidth(), onClick = { saveCard() }) {
        Text(text = "Save Card")
      }
    }
  }


}

/**
 * Preview function to display a preview of the HomeScreen composable.
 */
@Preview
@Composable
private fun Preview() {
  Navigator {
    HomeScreen(
      homeViewModel = HomeViewModel(
        deunaSDK = DeunaSDK(
          environment = Environment.SANDBOX, publicApiKey = "FAKE_API_KEY"
        )
      )
    )
  }
}