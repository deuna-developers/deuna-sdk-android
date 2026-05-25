package com.deuna.explore.presentation.screens.wallets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deuna.maven.DeunaSDK
import com.deuna.maven.initElements
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.wallets.GetWalletsAvailableParams
import com.deuna.maven.wallets.WalletProvider
import com.deuna.maven.wallets.getWalletsAvailable
import org.json.JSONObject

private val walletUserInfo = UserInfo(
    firstName = "Darwin",
    lastName = "Morocho",
    email = "dmorocho@deuna.com",
)

@Composable
fun WalletsScreen(
    deunaSDK: DeunaSDK,
    orderToken: String?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var isFetchingWallets by remember { mutableStateOf(false) }
    var availableWallets by remember { mutableStateOf<List<WalletProvider>>(emptyList()) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    var successJson by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var closedMessage by remember { mutableStateOf<String?>(null) }
    var copiedConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBarSection(onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = {
                    isFetchingWallets = true
                    fetchError = null
                    availableWallets = emptyList()
                    successJson = null
                    errorMessage = null
                    closedMessage = null

                    val params = GetWalletsAvailableParams(
                        orderToken = orderToken,
                        userInfo = walletUserInfo,
                    )
                    deunaSDK.getWalletsAvailable(context, params) { wallets, error ->
                        isFetchingWallets = false
                        if (error != null) {
                            fetchError = "[${error.code}] ${error.message}"
                        } else {
                            availableWallets = wallets
                            if (wallets.isEmpty()) {
                                fetchError = "No wallets available on this device."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isFetchingWallets,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF147AE8)),
            ) {
                if (isFetchingWallets) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (isFetchingWallets) "Fetching..." else "Get Wallets Available",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (fetchError != null) {
                StatusCard(message = fetchError!!, color = Color.Red)
            }

            if (availableWallets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Available Wallets",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    availableWallets.forEach { wallet ->
                        Button(
                            onClick = {
                                successJson = null
                                errorMessage = null
                                closedMessage = null

                                deunaSDK.initElements(
                                    context = context,
                                    callbacks = ElementsCallbacks().apply {
                                        onSuccess = { data ->
                                            successJson = try {
                                                JSONObject(data as Map<*, *>).toString(2)
                                            } catch (e: Exception) {
                                                data.toString()
                                            }
                                        }
                                        onError = { error ->
                                            val code = error.metadata?.code ?: "ERROR"
                                            val msg = error.metadata?.message ?: "Unknown error"
                                            errorMessage = "[$code] $msg"
                                        }
                                        onClosed = {
                                            if (successJson == null) {
                                                closedMessage = "Payment sheet closed without completing."
                                            }
                                        }
                                    },
                                    userInfo = walletUserInfo,
                                    types = listOf(mapOf("name" to wallet.processorName.uppercase())),
                                    orderToken = orderToken,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        ) {
                            Text(walletLabel(wallet), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (successJson != null) {
                SuccessCard(
                    json = successJson!!,
                    copied = copiedConfirmation,
                    onCopy = {
                        clipboard.setText(AnnotatedString(successJson!!))
                        copiedConfirmation = true
                    },
                )
            }

            if (errorMessage != null) {
                StatusCard(message = errorMessage!!, color = Color.Red)
            }

            if (closedMessage != null) {
                StatusCard(message = closedMessage!!, color = Color(0xFFFF9500))
            }
        }
    }

    LaunchedEffect(copiedConfirmation) {
        if (copiedConfirmation) {
            kotlinx.coroutines.delay(2000)
            copiedConfirmation = false
        }
    }
}

private fun walletLabel(wallet: WalletProvider): String = when (wallet) {
    WalletProvider.GOOGLE_PAY -> "Google Pay"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarSection(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Wallets") },
        navigationIcon = {
            TextButton(onClick = onBack) { Text("Back") }
        },
    )
}

@Composable
private fun StatusCard(message: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(message, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SuccessCard(json: String, copied: Boolean, onCopy: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF34C759).copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF34C759).copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34C759), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Success", fontWeight = FontWeight.SemiBold, color = Color(0xFF34C759), fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onCopy) {
                    Text(if (copied) "Copied" else "Copy", color = if (copied) Color(0xFF34C759) else Color(0xFF147AE8), fontSize = 12.sp)
                }
            }
            Text(
                text = json,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.heightIn(max = 200.dp),
            )
        }
    }
}
