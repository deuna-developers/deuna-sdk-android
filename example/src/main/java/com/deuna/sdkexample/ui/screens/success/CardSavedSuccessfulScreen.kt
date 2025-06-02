package com.deuna.sdkexample.ui.screens.success

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toMap
import org.json.JSONObject


@Composable
fun CardSavedSuccessfulScreen(
    navController: NavController,
    title: String,
    savedCardData: Json
) {


    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title)
            Box(modifier = Modifier.height(20.dp))
            Text(text = "ID: ${savedCardData["id"]}")
            Text(text = "Holder Name: ${savedCardData["cardHolder"] ?: savedCardData["card_holder"]}")
            Text(text = "Card brand: ${savedCardData["company"]}")
            Text(text = "Expiration date: ${savedCardData["expirationDate"] ?: savedCardData["expiration_date"]}")
            Text(text = "First Six Digits: ${savedCardData["firstSix"] ?: savedCardData["first_six"]}")
            Text(text = "Last Four Digits: ${savedCardData["lastFour"] ?: savedCardData["last_four"]}")
            Box(modifier = Modifier.height(20.dp))
            ElevatedButton(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                ),
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

    CardSavedSuccessfulScreen(
        title = "Card saved successfully",
        navController = NavController(LocalContext.current),
        savedCardData = JSONObject(
            """
      {
      "id": "4fd0584b-d336-4406-ad63-cc253fd47f14",
      "verifiedAt": "0001-01-01T00:00:00Z",
      "verifiedWithTransactionId": "",
      "firstSix": "424242",
      "verifiedBy": "",
      "bankName": "",
      "storedCard": false,
      "company": "visa",
      "expirationDate": "12/30",
      "cardHolder": "Test User",
      "userId": "45aa4524-1e98-4f5d-8845-20394f0f37ee",
      "cardType": "credit_card",
      "isValid": false,
      "cardId": "4fd0584b-d336-4406-ad63-cc253fd47f14",
      "lastFour": "4242",
      "createdAt": "2024-07-10T17:54:26.970636182Z",
      "cardHolderDni": "",
      "deletedAt": null 
      }
            """.trimIndent()
        ).toMap()
    )

}