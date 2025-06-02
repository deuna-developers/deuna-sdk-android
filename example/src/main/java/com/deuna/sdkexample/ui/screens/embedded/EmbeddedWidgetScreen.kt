package com.deuna.sdkexample.ui.screens.embedded

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.deuna.maven.DeunaSDK
import com.deuna.maven.widgets.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.ElementsWidget
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.TWO_STEP_FLOW
import com.deuna.maven.web_views.deuna.extensions.build
import com.deuna.maven.web_views.deuna.extensions.submit
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration
import com.deuna.maven.widgets.configuration.NextActionWidgetConfiguration
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration
import com.deuna.maven.widgets.next_action.NextActionCallbacks
import com.deuna.maven.widgets.voucher.VoucherCallbacks
import com.deuna.sdkexample.shared.views.Separator
import com.deuna.sdkexample.ui.screens.embedded.views.PayButton
import com.deuna.sdkexample.ui.screens.main.WidgetToShow
import com.deuna.sdkexample.ui.screens.main.view_model.DEBUG_TAG

data class EmbeddedWidgetScreenParams(
    val deunaSDK: DeunaSDK,
    val orderToken: String,
    val userToken: String,
    val widgetToShow: WidgetToShow
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EmbeddedWidgetScreen(
    params: EmbeddedWidgetScreenParams,
    onSuccess: (data: Json) -> Unit
) {
    val deunaSDK = params.deunaSDK
    val orderToken = params.orderToken
    val userToken = params.userToken
    val widgetToShow = params.widgetToShow

    val deunaWidget = remember { mutableStateOf<DeunaWidget?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F7))
            .padding(16.dp)
    ) {
        Text("Confirm and pay", style = MaterialTheme.typography.titleLarge)
        Separator(16.dp)

        Card(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    DeunaWidget(context).apply {
                        this.widgetConfiguration = when (widgetToShow) {
                            WidgetToShow.PAYMENT_WIDGET -> PaymentWidgetConfiguration(
                                sdkInstance = deunaSDK,
                                hidePayButton = true, // Hide the pay button in the embedded widget
                                orderToken = orderToken,
                                userToken = userToken,
                                paymentMethods = listOf(
                                    mapOf(
                                        "paymentMethod" to "wallet",
                                        "processors" to listOf("paypal_wallet"),
                                        "configuration" to mapOf(
                                            "express" to false,
                                            "flowType" to mapOf("type" to TWO_STEP_FLOW)
                                        )
                                    )
                                ),
                                callbacks = PaymentWidgetCallbacks().apply {
                                    onEventDispatch = { event, data ->
                                        Log.i(DEBUG_TAG, "Event: $event, Data: $data")
                                    }
                                    this.onSuccess = { data ->
                                        Log.i(DEBUG_TAG, "✅ Success: $data")
                                        onSuccess(data)
                                    }
                                    onError = { error ->
                                        Log.e(DEBUG_TAG, "❌ Error: $error")
                                    }
                                },
                            )

                            WidgetToShow.VAULT_WIDGET -> ElementsWidgetConfiguration(
                                sdkInstance = deunaSDK,
                                hidePayButton = true, // Hide the pay button in the embedded widget
                                orderToken = orderToken,
                                userToken = userToken,
                                userInfo = UserInfo(
                                    firstName = "Darwin",
                                    lastName = "Morocho",
                                    email = "dmorocho+10@deuna.com"
                                ),
                                callbacks = ElementsCallbacks().apply {
                                    this.onSuccess = { data ->
                                        val savedCard =
                                            (data["metadata"] as Json)["createdCard"] as Json
                                        onSuccess(savedCard)
                                    }
                                },
                            )

                            WidgetToShow.CHECKOUT_WIDGET -> CheckoutWidgetConfiguration(
                                sdkInstance = deunaSDK,
                                hidePayButton = true, // Hide the pay button in the embedded widget
                                orderToken = orderToken,
                                userToken = userToken,
                                callbacks = CheckoutCallbacks().apply {
                                    this.onSuccess = { data ->
                                        onSuccess(data)
                                    }
                                },
                            )

                            WidgetToShow.CLICK_TO_PAY_WIDGET -> ElementsWidgetConfiguration(
                                sdkInstance = deunaSDK,
                                hidePayButton = true, // Hide the pay button in the embedded widget
                                orderToken = orderToken,
                                userToken = userToken,
                                userInfo = UserInfo(
                                    firstName = "Darwin",
                                    lastName = "Morocho",
                                    email = "dmorocho+10@deuna.com"
                                ),
                                types = listOf(
                                    mapOf(
                                        "name" to ElementsWidget.CLICK_TO_PAY
                                    )
                                ),
                                callbacks = ElementsCallbacks().apply {
                                    this.onSuccess = { data ->
                                        val savedCard =
                                            (data["metadata"] as Json)["createdCard"] as Json
                                        onSuccess(savedCard)
                                    }
                                },
                            )

                            WidgetToShow.NEXT_ACTION_WIDGET -> NextActionWidgetConfiguration(
                                sdkInstance = deunaSDK,
                                hidePayButton = true, // Hide the pay button in the embedded widget
                                orderToken = orderToken,
                                callbacks = NextActionCallbacks().apply {
                                    this.onSuccess = { data ->
                                        onSuccess(data)
                                    }
                                    this.onError = { error ->
                                        Log.e(DEBUG_TAG, "❌ Error: $error")
                                    }
                                    this.onEventDispatch = { event, data ->
                                        Log.i(DEBUG_TAG, "Event: $event, Data: $data")
                                    }
                                },
                            )

                            WidgetToShow.VOUCHER_WIDGET -> VoucherWidgetConfiguration(
                                sdkInstance = deunaSDK,
                                hidePayButton = true, // Hide the pay button in the embedded widget
                                orderToken = orderToken,
                                callbacks = VoucherCallbacks().apply {
                                    this.onSuccess = { data ->
                                        onSuccess(data)
                                    }
                                    this.onError = { error ->
                                        Log.e(DEBUG_TAG, "❌ Error: $error")
                                    }
                                    this.onEventDispatch = { event, data ->
                                        Log.i(DEBUG_TAG, "Event: $event, Data: $data")
                                    }
                                },
                            )
                        }
                        this.build()
                        deunaWidget.value = this
                    }
                }
            )
        }


        Separator(16.dp)

        PayButton {
            deunaWidget.value?.let { deunaWidget ->
                deunaWidget.submit { result ->
                    Log.i(DEBUG_TAG, "Submit result: ${result.status} - ${result.message}")
                }
            }
        }

    }

    // Dispose the DeunaWidget when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            deunaWidget.value?.destroy()
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    EmbeddedWidgetScreen(
        params = EmbeddedWidgetScreenParams(
            deunaSDK = DeunaSDK(
                environment = Environment.SANDBOX,
                publicApiKey = "FAKE_API_KEY"
            ),
            orderToken = "FAKE_ORDER_TOKEN",
            userToken = "FAKE_USER_TOKEN",
            widgetToShow = WidgetToShow.PAYMENT_WIDGET
        ),
        onSuccess = {}
    )
}