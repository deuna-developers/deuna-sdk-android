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
import org.json.JSONObject

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {

    var paymentError by remember { mutableStateOf<PaymentsError?>(null) }

    // Retrieve the NavController from the composition's LocalNavController
    // use it for navigation
    val navController = LocalNavController.current

    // Retrieve the user token and order token states from the view model
    val userTokenState = homeViewModel.userToken
    val orderTokenState = homeViewModel.orderToken

    // Retrieve the Context from the composition's LocalContext
    val context = LocalContext.current

    // Function to perform payment and navigate to the success screen upon successful payment
    fun performPaymentWithPaymentWidget() {
        homeViewModel.showPaymentWidget(context = context, completion = { result ->
            when (result) {
                is PaymentWidgetResult.Canceled -> Log.d("PAYMENT", "Canceled")
                is PaymentWidgetResult.Error -> paymentError = result.error
                is PaymentWidgetResult.Success -> navController.navigate(
                    "/payment-success/${Uri.encode(JSONObject(result.order).toString())}"
                )
            }
        })
    }

    // Function to perform payment and navigate to the success screen upon successful payment
    fun performPaymentWithCheckout() {
        homeViewModel.showCheckout(context = context, completion = { result ->
            when (result) {
                is CheckoutResult.Canceled -> Log.d("PAYMENT", "Canceled")
                is CheckoutResult.Error -> paymentError = result.error
                is CheckoutResult.Success -> navController.navigate(
                    "/payment-success/${Uri.encode(JSONObject(result.order).toString())}"
                )
            }
        })
    }

    // Function to save card and navigate to the success screen upon successful card saving
    fun saveCard() {
        homeViewModel.saveCard(context = context, completion = { result ->
            when (result) {
                is ElementsResult.Canceled -> Log.d("SAVING CARD", "Canceled")
                is ElementsResult.Error -> Log.d("SAVING CARD ERROR", result.error.type.message)
                is ElementsResult.Success -> navController.navigate(
                    "/vault-success/${Uri.encode(JSONObject(result.savedCard).toString())}"
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


            ElevatedButton(modifier = Modifier.fillMaxWidth(),
                onClick = { performPaymentWithPaymentWidget() }) {
                Text(text = "Show Payment Widget")
            }

            ElevatedButton(modifier = Modifier.fillMaxWidth(),
                onClick = { performPaymentWithCheckout() }) {
                Text(text = "Show Checkout")
            }

            ElevatedButton(modifier = Modifier.fillMaxWidth(), onClick = { saveCard() }) {
                Text(text = "Save Card")
            }
        }
    }

    if (paymentError != null) {
        AlertDialog(
            onDismissRequest = { paymentError = null },
            title = {
                Text(
                    paymentError!!.metadata?.code ?: ""
                )
            },
            text = {
                Text(
                    paymentError!!.metadata?.message ?: ""
                )
            },
            confirmButton = {
                Button(
                    onClick = { paymentError = null }
                ) {
                    Text("Dismiss")
                }
            },
        )
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