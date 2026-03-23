package com.deuna.sdkexample.ui.screens.main

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.deuna.maven.DeunaSDK
import com.deuna.maven.generateFraudId
import com.deuna.maven.shared.Environment
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.submit
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import com.deuna.sdkexample.navigation.AppRoutes
import com.deuna.sdkexample.shared.views.Separator
import com.deuna.sdkexample.ui.screens.main.utils.buildWidgetConfig
import com.deuna.sdkexample.ui.screens.main.views.DeunaButton
import com.deuna.sdkexample.ui.screens.main.views.Inputs
import com.deuna.sdkexample.ui.screens.main.views.WidgetContainer
import com.deuna.sdkexample.ui.screens.main.views.WidgetPicker
import org.json.JSONObject


enum class WidgetToShow(val label: String) {
    PAYMENT_WIDGET("Payment Widget"),
    NEXT_ACTION_WIDGET("Next Action Widget"),
    VOUCHER_WIDGET("Voucher Widget"),
    CHECKOUT_WIDGET("Checkout Widget"),
    VAULT_WIDGET("Vault Widget"),
    CLICK_TO_PAY_WIDGET("Click to Pay Widget"),
    ;
}

@Composable
fun MainScreen(
    deunaSDK: DeunaSDK,
    navController: NavHostController
) {

    // Retrieve the Context from the composition's LocalContext
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var orderToken by remember { mutableStateOf("") }
    var userToken by remember { mutableStateOf("") }
    var fraudId by remember { mutableStateOf("") }
    var widgetToShow by remember { mutableStateOf(WidgetToShow.PAYMENT_WIDGET) }
    var config by remember { mutableStateOf<DeunaWidgetConfiguration?>(null) }

    val deunaWidget = remember { mutableStateOf<DeunaWidget?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            WidgetContainer(
                config = config,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onWidgetCreated = {
                    deunaWidget.value = it
                }
            )

            Separator(10.dp)

            Row {
                WidgetPicker(widgetToShow = widgetToShow) {
                    widgetToShow = it
                }

                if (config == null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    DeunaButton(
                        onClick = {
                            config = buildWidgetConfig(
                                widgetToShow = widgetToShow,
                                orderToken = orderToken,
                                userToken = userToken,
                                deunaSDK = deunaSDK,
                                onPaymentSuccess = { data ->
                                    deunaWidget.value?.waitUntilExternalUrlIsClosed {
                                        deunaWidget.value?.destroy()
                                        val jsonStr = Uri.encode(JSONObject(data).toString())
                                        navController.navigate("payment-success/$jsonStr") {
                                        }
                                    }
                                },
                                onSaveCardSuccess = { data ->
                                    deunaWidget.value?.destroy()
                                    val jsonStr = Uri.encode(JSONObject(data).toString())
                                    navController.navigate("card-saved-success/$jsonStr") {
                                    }
                                }
                            )
                        },
                        text = "Show Widget",
                        backgroundColor = Color(0xFF007AFF)
                    )
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                    DeunaButton(
                        onClick = {
                            deunaWidget.value?.submit {}
                        },
                        text = "Pay",
                        backgroundColor = Color.Red
                    )
                }
            }

            Inputs(
                userToken = userToken,
                orderToken = orderToken,
                onOrderTokenChange = {
                    orderToken = it
                },
                onUserTokenChange = {
                    userToken = it
                }
            )
            Separator(30.dp)

            DeunaButton(
                onClick = {
                    deunaSDK.generateFraudId(
                        context = context,
                        params = mapOf(
                            "RISKIFIED" to mapOf(
                                "storeDomain" to "deuna.com"
                            )
                        )
                    ) {
                        fraudId = it ?: "ERROR"
                    }
                },
                text = "Generate Fraud ID"
            )
            Text("Fraud ID: $fraudId")

        }
    }

    // IMPORTANT: Destroy the widget when the screen is disposed
    // or when it's not needed anymore
    DisposableEffect(Unit) {
        onDispose {
            deunaWidget.value?.destroy()
        }
    }
}

@Preview(showBackground = true, name = "MyScreen Preview")
@Composable
fun MyScreenPreview() {
    MainScreen(
        deunaSDK = DeunaSDK(
            environment = Environment.SANDBOX,
            publicApiKey = "FAKE_API_KEY"
        ),
        navController = NavHostController(LocalContext.current)
    )
}