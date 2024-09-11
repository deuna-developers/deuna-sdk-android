package com.deuna.maven.payment_widget.domain

import android.webkit.JavascriptInterface
import com.deuna.maven.checkout.domain.CheckoutEvent
import com.deuna.maven.closeWebView
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.WebViewBridge
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.shared.toMap
import com.deuna.maven.web_views.PaymentWidgetActivity
import org.json.JSONException
import org.json.JSONObject

@Suppress("UNCHECKED_CAST")
class PaymentWidgetBridge(
    private val activity: PaymentWidgetActivity
) : WebViewBridge(name = "android") {

    @JavascriptInterface
    fun consoleLog(message: String) {
        DeunaLogs.info("ConsoleLogBridge: $message")
    }

    override fun handleEvent(message: String) {
        try {
            val json = JSONObject(message).toMap()

            val type = json["type"] as? String
            val data = json["data"] as? Json

            if (type == null || data == null) {
                return
            }

            val event = CheckoutEvent.valueOf(type)

            val checkoutEvent = CheckoutEvent.valueOf(type)
            activity.callbacks?.onEventDispatch?.invoke(checkoutEvent, data)

            when (event) {
                CheckoutEvent.purchaseError -> {
                    activity.closeSubWebView()
                    activity.updateCloseEnabled(true)
                    val error = PaymentsError.fromJson(
                        type = PaymentsError.Type.PAYMENT_ERROR, data = data
                    )
                    if (error != null) {
                        activity.callbacks?.onError?.invoke(error)
                    }
                }

                CheckoutEvent.onBinDetected -> {
                    handleCardBinDetected(data["metadata"] as? Json)
                }

                CheckoutEvent.onInstallmentSelected -> {
                    handleInstallmentSelected(data["metadata"] as? Json)
                }

                CheckoutEvent.paymentProcessing -> {
                    activity.updateCloseEnabled(false)
                    activity.callbacks?.onPaymentProcessing?.invoke()
                }

                CheckoutEvent.purchase -> {
                    activity.closeSubWebView()
                    activity.callbacks?.onSuccess?.invoke(data["order"] as Json)
                }
                CheckoutEvent.paymentMethods3dsInitiated -> {}
                CheckoutEvent.linkClose -> {
                    activity.onCanceledByUser()
                    closeWebView(activity.sdkInstanceId!!)
                }

                else -> {}
            }
        } catch (_: IllegalArgumentException) {
        } catch (e: JSONException) {
            DeunaLogs.debug("PaymentWidgetBridge JSONException: $e")
        }
    }


    private fun handleCardBinDetected(metadata: Json?) {
        activity.callbacks?.onCardBinDetected?.invoke(metadata)
    }

    private fun handleInstallmentSelected(metadata: Json?) {
        activity.callbacks?.onInstallmentSelected?.invoke(metadata)
    }
}