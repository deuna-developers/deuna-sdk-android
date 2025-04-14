package com.deuna.maven.widgets.next_action

import android.webkit.JavascriptInterface
import com.deuna.maven.widgets.checkout_widget.CheckoutEvent
import com.deuna.maven.shared.DeunaBridge
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.VoidCallback
import com.deuna.maven.shared.toMap
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.file_downloaders.downloadFile
import com.deuna.maven.web_views.file_downloaders.runOnUiThread
import com.deuna.maven.web_views.file_downloaders.saveBase64ImageToDevice
import com.deuna.maven.web_views.file_downloaders.takeSnapshot
import org.json.JSONException
import org.json.JSONObject

@Suppress("UNCHECKED_CAST")
class NextActionBridge(
    deunaWidget: DeunaWidget,
    val callbacks: NextActionCallbacks,
    onCloseByUser: VoidCallback? = null,
) : DeunaBridge(
    deunaWidget = deunaWidget,
    name = "android",
    onCloseByUser = onCloseByUser,
) {

    @JavascriptInterface
    fun consoleLog(message: String) {
        DeunaLogs.info("ConsoleLogBridge: $message")
    }

    override fun handleEvent(message: String) {
        deunaWidget.runOnUiThread {
            try {
                val json = JSONObject(message).toMap()

                val type = json["type"] as? String
                val data = json["data"] as? Json

                if (type == null || data == null) {
                    return@runOnUiThread
                }


                // This event is emitted by the widget when the download voucher button
                // is pressed
                if (type == "apmSaveId") {
                    val metadata = data["metadata"] as Json?
                    val downloadUrl =
                        metadata?.get("voucherPdfDownloadUrl") as String?

                    if (downloadUrl != null) {
                        deunaWidget.downloadFile(downloadUrl)
                    } else {
                        downloadVoucher()
                    }
                    return@runOnUiThread
                }

                val event = CheckoutEvent.valueOf(type)

                val checkoutEvent = CheckoutEvent.valueOf(type)
                callbacks.onEventDispatch?.invoke(checkoutEvent, data)

                when (event) {
                    CheckoutEvent.purchaseError -> {
                        deunaWidget.closeSubWebView()
                        deunaWidget.updateCloseEnabled(true)
                        val error = PaymentsError.fromJson(
                            type = PaymentsError.Type.PAYMENT_ERROR, data = data
                        )
                        callbacks.onError?.invoke(error)
                    }

                    CheckoutEvent.purchase -> {
                        deunaWidget.closeSubWebView()
                        callbacks.onSuccess?.invoke(data["order"] as Json)
                    }

                    CheckoutEvent.paymentMethods3dsInitiated -> {}
                    CheckoutEvent.linkClose -> {
                        onCloseByUser?.invoke()
                    }

                    else -> {}
                }
            } catch (_: IllegalArgumentException) {
            } catch (e: JSONException) {
                DeunaLogs.debug("PaymentWidgetBridge JSONException: $e")
            }
        }
    }


    /**
     * Uses js injection with html2canvas library to
     * take a screen shoot of the web page loaded in the web view
     */
    private fun downloadVoucher() {
        deunaWidget.runOnUiThread {
            deunaWidget.webView.takeSnapshot(deunaWidget.takeSnapshotBridge) { base64Image ->
                if (base64Image != null) {
                    saveBase64ImageToDevice(base64Image)
                }
            }
        }
    }
}