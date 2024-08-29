package com.deuna.compose_demo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.deuna.compose_demo.*
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toMap
import org.json.JSONObject


@Composable
fun VaultSuccessfulScreen(card: Json) {
    val navController = LocalNavController.current

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Card Saved")
            Box(modifier = Modifier.height(20.dp))
            Text(text = "ID: ${card["id"]}")
            Text(text = "Holder Name: ${card["cardHolder"]}")
            Text(text = "First Six Digits: ${card["firstSix"]}")
            Text(text = "Last Four Digits: ${card["lastFour"]}")
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
        VaultSuccessfulScreen(
            card = JSONObject(
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
}