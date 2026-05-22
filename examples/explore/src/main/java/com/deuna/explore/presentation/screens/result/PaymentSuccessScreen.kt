package com.deuna.explore.presentation.screens.result

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.deuna.maven.shared.Json

@Suppress("UNCHECKED_CAST")
@Composable
fun PaymentSuccessScreen(json: Json, onBack: () -> Unit) {
    val orderId = json["order_id"] as? String ?: ""
    val items = (json["items"] as? List<Json>)?.mapNotNull { item ->
        val id = item["id"] as? String ?: return@mapNotNull null
        val name = item["name"] as? String ?: return@mapNotNull null
        val options = item["options"] as? String
        val totalAmount = ((item["total_amount"] as? Json)?.get("display_amount") as? String) ?: ""
        mapOf("id" to id, "name" to name, "options" to options, "totalAmount" to totalAmount)
    } ?: emptyList()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Payment Successful", style = MaterialTheme.typography.headlineSmall)
            Text("Order ID: $orderId")

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item["name"] as String, style = MaterialTheme.typography.bodyLarge)
                            val options = item["options"]
                            if (!options.isNullOrEmpty()) Text(options, style = MaterialTheme.typography.bodyMedium)
                            Text("Total: ${item["totalAmount"]}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

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
