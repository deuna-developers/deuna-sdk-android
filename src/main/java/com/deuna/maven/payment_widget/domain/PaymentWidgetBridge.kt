package com.deuna.maven.payment_widget.domain

import android.webkit.JavascriptInterface
import com.deuna.maven.closePaymentWidget
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.WebViewBridge
import com.deuna.maven.shared.toMap
import com.deuna.maven.web_views.PaymentWidgetActivity
import org.json.JSONException
import org.json.JSONObject

@Suppress("UNCHECKED_CAST")
class PaymentWidgetBridge(
    private val activity: PaymentWidgetActivity
) : WebViewBridge(name = "android") {


    private val refetchOrderRequests = mutableMapOf<Int, (Json?) -> Unit>()
    private var refetchOrderRequestId = 0

    @JavascriptInterface
    fun consoleLog(message: String) {
        DeunaLogs.info("ConsoleLogBridge: $message")
    }

    @JavascriptInterface
    fun onRefetchOrder(message: String) {
        handleEvent(message)
    }

    override fun handleEvent(message: String) {
        try {
            val json = JSONObject(message).toMap()

            val type = json["type"] as? String
            val data = json["data"] as? Json

            if (type == null || data == null) {
                return
            }

            val event = PaymentWidgetEvent.valueOf(type)

            when (event) {
                PaymentWidgetEvent.purchaseError -> {
                    val error = PaymentsError.fromJson(
                        type = PaymentsError.Type.PAYMENT_ERROR,
                        data = data
                    )
                    if (error != null) {
                        activity.callbacks?.onError?.invoke(error)
                    }
                }

                PaymentWidgetEvent.onBinDetected -> {
                    handleCardBinDetected(data["metadata"] as? Json)
                }

                PaymentWidgetEvent.onInstallmentSelected -> {
                    handleInstallmentSelected(data["metadata"] as? Json)
                }

                PaymentWidgetEvent.refetchOrder -> {
                    handleOnRefetchOrder(json)
                }

                PaymentWidgetEvent.purchase -> activity.callbacks?.onSuccess?.invoke(data)
                PaymentWidgetEvent.paymentMethods3dsInitiated -> {}
                PaymentWidgetEvent.linkClose -> {
                    closePaymentWidget(activity.sdkInstanceId!!)
                    activity.callbacks?.onCanceled?.invoke()
                }
            }
        } catch (_: IllegalArgumentException) {
        } catch (e: JSONException) {
            DeunaLogs.debug("PaymentWidgetBridge JSONException: $e")
        }
    }


    private fun handleCardBinDetected(metadata: Json?) {
        if (metadata == null) {
            activity.callbacks?.onCardBinDetected?.invoke(
                null
            ) { callback -> refetchOrder(callback) }
            return
        }

        activity.callbacks?.onCardBinDetected?.invoke(
            metadata,
        ) { callback -> refetchOrder(callback) }
    }

    private fun handleInstallmentSelected(metadata: Json?) {
        if (metadata == null) {
            activity.callbacks?.onInstallmentSelected?.invoke(
                null
            ) { callback -> refetchOrder(callback) }
            return
        }

        activity.callbacks?.onInstallmentSelected?.invoke(
            metadata,
        ) { callback -> refetchOrder(callback) }
    }

    private fun handleOnRefetchOrder(json: Json) {
        val requestId = json["requestId"] as? Int
        if (!refetchOrderRequests.contains(requestId)) {
            return
        }

        refetchOrderRequests[requestId]?.invoke(
            json["data"] as? Json
        )
        refetchOrderRequests.remove(requestId)
    }

    private fun refetchOrder(callback: (Json?) -> Unit) {
        refetchOrderRequestId++
        refetchOrderRequests[refetchOrderRequestId] = callback

        activity.runOnUiThread {
            activity.webView.evaluateJavascript(
                """
        (function() {
            function refetchOrder(callback) {
                deunaRefetchOrder()
                    .then(data => {
                        callback({type:"refetchOrder", data: data , requestId: $refetchOrderRequestId });
                    })
                    .catch(error => {
                        callback({type:"refetchOrder", data: null , requestId: $refetchOrderRequestId });
                    });
            }

            refetchOrder(function(result) {
                android.onRefetchOrder(JSON.stringify(result));
            });
        })();
            """.trimIndent(), null
            )
        }
    }
}