package com.deuna.explore.presentation.screens.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.deuna.maven.shared.Json

@Composable
fun CardSavedSuccessScreen(cardData: Json, onBack: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text("Card saved successfully", style = MaterialTheme.typography.headlineSmall)
            Text("ID: ${cardData["id"]}")
            Text("Holder: ${cardData["cardHolder"] ?: cardData["card_holder"]}")
            Text("Card brand: ${cardData["company"]}")
            Text("Expiration: ${cardData["expirationDate"] ?: cardData["expiration_date"]}")
            Text("Last four: ${cardData["lastFour"] ?: cardData["last_four"]}")
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF147AE8)),
            ) {
                Text("Go back!")
            }
        }
    }
}
