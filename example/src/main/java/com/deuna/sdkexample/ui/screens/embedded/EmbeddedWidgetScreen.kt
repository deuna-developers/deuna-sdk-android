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
import com.deuna.maven.widgets.checkout_widget.CheckoutBridge
import com.deuna.maven.widgets.elements_widget.ElementsBridge
import com.deuna.maven.widgets.payment_widget.PaymentWidgetBridge
import com.deuna.maven.widgets.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.ElementsWidget
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.submit
import com.deuna.maven.widgets.checkout_widget.buildCheckoutWidgetUrl
import com.deuna.maven.widgets.elements_widget.buildElementsWidgetUrl
import com.deuna.maven.widgets.next_action.NextActionBridge
import com.deuna.maven.widgets.next_action.NextActionCallbacks
import com.deuna.maven.widgets.next_action.buildNextActionUrl
import com.deuna.maven.widgets.payment_widget.buildPaymentWidgetUrl
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

                        // Hide the pay button in the embedded widget.
                        // So you must call to the `submit` method to complete the transaction
                        this.hidePayButton = true

                        when (widgetToShow) {
                            WidgetToShow.PAYMENT_WIDGET -> {
                                val url = deunaSDK.buildPaymentWidgetUrl(
                                    orderToken = orderToken,
                                    userToken = userToken
                                )
                                bridge = PaymentWidgetBridge(
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
                                    deunaWidget = this,
                                )
                                this.loadUrl(url)
                            }

                            WidgetToShow.VAULT_WIDGET -> {
                                val url = deunaSDK.buildElementsWidgetUrl(
                                    orderToken = orderToken,
                                    userToken = userToken,
                                    userInfo = UserInfo(
                                        firstName = "Darwin",
                                        lastName = "Morocho",
                                        email = "dmorocho+10@deuna.com"
                                    )
                                )
                                bridge = ElementsBridge(
                                    callbacks = ElementsCallbacks().apply {
                                        this.onSuccess = { data ->
                                            val savedCard =
                                                (data["metadata"] as Json)["createdCard"] as Json
                                            onSuccess(savedCard)
                                        }
                                    },
                                    deunaWidget = this,
                                )
                                this.loadUrl(url)
                            }

                            WidgetToShow.CHECKOUT_WIDGET -> {
                                bridge = CheckoutBridge(
                                    callbacks = CheckoutCallbacks().apply {
                                        this.onSuccess = { data ->
                                            onSuccess(data)
                                        }
                                    },
                                    deunaWidget = this,
                                )
                                deunaSDK.buildCheckoutWidgetUrl(
                                    orderToken = orderToken,
                                    userToken = userToken
                                ) { error, url ->
                                    error?.let {
                                        Log.i(DEBUG_TAG, "Error: $error")
                                    }
                                    url?.let {
                                        this.loadUrl(it)
                                    }
                                }
                            }

                            WidgetToShow.CLICK_TO_PAY_WIDGET -> {
                                val url = deunaSDK.buildElementsWidgetUrl(
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
                                )
                                bridge = ElementsBridge(
                                    callbacks = ElementsCallbacks().apply {
                                        this.onSuccess = { data ->
                                            val savedCard =
                                                (data["metadata"] as Json)["createdCard"] as Json
                                            onSuccess(savedCard)
                                        }
                                    },
                                    deunaWidget = this,
                                )
                                this.loadUrl(url)
                            }

                            WidgetToShow.NEXT_ACTION_WIDGET -> {
                                val url = deunaSDK.buildNextActionUrl(
                                    orderToken = orderToken
                                )
                                bridge = NextActionBridge(
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
                                    deunaWidget = this,
                                )
                                this.loadUrl(url)
                            }
                        }

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