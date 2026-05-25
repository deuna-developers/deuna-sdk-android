package com.deuna.sdkexample.ui.screens.wallets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.deuna.maven.DeunaSDK
import com.deuna.maven.initElements
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.wallets.WalletProvider
import com.deuna.maven.wallets.WalletsError
import com.deuna.maven.wallets.getWalletsAvailable
import org.json.JSONObject

@Composable
fun WalletsScreen(
    deunaSDK: DeunaSDK,
    orderToken: String?,
    navController: NavController,
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var wallets by remember { mutableStateOf<List<WalletProvider>>(emptyList()) }
    var error by remember { mutableStateOf<WalletsError?>(null) }
    var loading by remember { mutableStateOf(false) }
    var successJson by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Google Pay Wallets",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    loading = true
                    error = null
                    wallets = emptyList()
                    statusMessage = null
                    successJson = null
                    deunaSDK.getWalletsAvailable(context) { result, err ->
                        wallets = result
                        error = err
                        loading = false
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (loading) "Loading..." else "Get Available Wallets")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    statusMessage = null
                    successJson = null
                    deunaSDK.initElements(
                        context = context,
                        orderToken = orderToken,
                        userInfo = UserInfo(
                            firstName = "Darwin",
                            lastName = "Morocho",
                            email = "3797270.qa@deuna.com",
                        ),
                        types = listOf(mapOf("name" to WalletProvider.GOOGLE_PAY.name)),
                        callbacks = ElementsCallbacks().apply {
                            onSuccess = { response ->
                                successJson = try {
                                    JSONObject(response as Map<*, *>).toString(2)
                                } catch (_: Exception) {
                                    response.toString()
                                }
                            }
                            onError = { err ->
                                statusMessage = "Error ${err.metadata?.code}: ${err.metadata?.message}"
                            }
                            onClosed = { action ->
                                if (action == CloseAction.userAction) {
                                    statusMessage = "Canceled by user"
                                }
                            }
                        },
                    )
                },
                enabled = WalletProvider.GOOGLE_PAY in wallets,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Pay with Google Pay")
            }

            Spacer(modifier = Modifier.height(24.dp))

            successJson?.let { json ->
                Text(
                    text = "onSuccess response",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = json,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 320.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { clipboardManager.setText(AnnotatedString(json)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Copy")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            statusMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDED)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Error: ${error!!.code}",
                            color = Color(0xFFB00020),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = error!!.message,
                            color = Color(0xFFB00020),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            } else if (wallets.isEmpty() && !loading) {
                Text(
                    text = "No wallets available",
                    color = Color.Gray,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                ) {
                    items(wallets) { wallet ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Text(
                                text = wallet.name,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Go back")
            }
        }
    }
}
