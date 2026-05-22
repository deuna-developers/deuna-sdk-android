package com.deuna.sdkexample.ui.screens.main.utils

import android.util.Log
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration
import com.deuna.maven.widgets.configuration.NextActionWidgetConfiguration
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration
import com.deuna.maven.widgets.next_action.NextActionCallbacks
import com.deuna.maven.widgets.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.widgets.voucher.VoucherCallbacks
import com.deuna.sdkexample.ui.screens.main.WidgetToShow

const val DEBUG_TAG = "DeunaSDK"

fun buildWidgetConfig(
    widgetToShow: WidgetToShow,
    orderToken: String,
    userToken: String,
    deunaSDK: DeunaSDK,
    onPaymentSuccess: (order: Json) -> Unit,
    onSaveCardSuccess: (cardData: Json) -> Unit
): DeunaWidgetConfiguration {
    return when (widgetToShow) {
        WidgetToShow.PAYMENT_WIDGET -> PaymentWidgetConfiguration(
            orderToken = orderToken,
            hidePayButton = true,
            userToken = userToken,
//            paymentMethods = listOf(
//                mapOf(
//                    "paymentMethod" to "wallet",
//                    "processors" to listOf("mercadopago_wallet")
//                )
//            ),
            callbacks = PaymentWidgetCallbacks().apply {
                onSuccess = onPaymentSuccess
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
                onEventDispatch = { event, _ ->
                    Log.i(DEBUG_TAG, "onEventDispatch event: $event")
                }
            },
            fraudCredentials = mapOf(
                "RISKIFIED" to mapOf(
                    "storeDomain" to "deuna.com"
                )
            ),
            sdkInstance = deunaSDK
        )

        WidgetToShow.NEXT_ACTION_WIDGET -> NextActionWidgetConfiguration(
            orderToken = orderToken,
            hidePayButton = true,
            callbacks = NextActionCallbacks().apply {
                onSuccess = onPaymentSuccess
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            },
            sdkInstance = deunaSDK
        )

        WidgetToShow.VOUCHER_WIDGET -> VoucherWidgetConfiguration(
            orderToken = orderToken,
            hidePayButton = true,
            callbacks = VoucherCallbacks().apply {
                onSuccess = onPaymentSuccess
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            },
            sdkInstance = deunaSDK
        )

        WidgetToShow.CHECKOUT_WIDGET -> CheckoutWidgetConfiguration(
            orderToken = orderToken,
            hidePayButton = true,
            userToken = userToken,
            callbacks = CheckoutCallbacks().apply {
                onSuccess = onPaymentSuccess
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            },
            sdkInstance = deunaSDK
        )

        WidgetToShow.VAULT_WIDGET -> ElementsWidgetConfiguration(
            userToken = userToken,
            hidePayButton = true,
            orderToken = orderToken,
            sdkInstance = deunaSDK,
            userInfo = UserInfo(
                firstName = "Darwin", lastName = "Morocho", email = "dmorocho@deuna.com"
            ),
            callbacks = ElementsCallbacks().apply {
                onSuccess = {
                    val cardData = (it["metadata"] as Json)["createdCard"] as Json
                    onSaveCardSuccess(cardData)
                }
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            }
        )

        WidgetToShow.CLICK_TO_PAY_WIDGET -> ElementsWidgetConfiguration(
            userToken = userToken,
            hidePayButton = true,
            orderToken = orderToken,
            sdkInstance = deunaSDK,
            callbacks = ElementsCallbacks().apply {
                onSuccess = {
                    val cardData = (it["metadata"] as Json)["createdCard"] as Json
                    onSaveCardSuccess(cardData)
                }
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            }
        )
    }
}
