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
    val activity: PaymentWidgetActivity
) : WebViewBridge(name = "android") {

    @JavascriptInterface
    fun consoleLog(message: String) {
        DeunaLogs.info("ConsoleLogBridge: $message")
    }

    @JavascriptInterface
    fun saveBase64Image(base64Image: String) {
        saveBase64ImageToDevice(base64Image)
    }

    override fun handleEvent(message: String) {
        try {
            val json = JSONObject(message).toMap()

            val type = json["type"] as? String
            val data = json["data"] as? Json

            if (type == null || data == null) {
                return
            }


            // This event is emitted by the widget when the download voucher button
            // is pressed
            if (type == "apmSaveId") {
                downloadVoucher()
                return
            }

            val event = CheckoutEvent.valueOf(type)

            val checkoutEvent = CheckoutEvent.valueOf(type)
            activity.callbacks?.onEventDispatch?.invoke(checkoutEvent, data)

            when (event) {
                CheckoutEvent.purchaseError, CheckoutEvent.purchaseRejected -> {
                    activity.closeSubWebView()
                    activity.updateCloseEnabled(true)
                    val error = PaymentsError.fromJson(
                        type = PaymentsError.Type.PAYMENT_ERROR, data = data
                    )
                    activity.callbacks?.onError?.invoke(error)
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

                CheckoutEvent.purchase, CheckoutEvent.apmSuccess -> {
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

    /**
     * Uses js injection with html2canvas library to
     * take a screen shoot of the web page loaded in the web view
     */
    private fun downloadVoucher() {
        DeunaLogs.info("Start downloading")
        val js = """
             (function() {
                function captureInvoice() {
                    html2canvas(document.body, { allowTaint:true, useCORS: true }).then((canvas) => {
                        // Convert the canvas to a base64 image
                        var imgData = canvas.toDataURL("image/png");
                        // Emit a local post message with the image as a base64 string.
                        android.saveBase64Image(imgData);
                    });
                }
             
             
                // If html2canvas is not added
                if (typeof html2canvas === "undefined") {
                    var script = document.createElement("script");
                    script.src = "https://html2canvas.hertzen.com/dist/html2canvas.min.js";
                    script.onload = function () {
                        captureInvoice();
                    };
                    document.head.appendChild(script);
                } else { captureInvoice(); }
             })();
        """.trimIndent()

        activity.runOnUiThread {
            activity.webView.evaluateJavascript(js, null)
        }
    }


}