package com.deuna.maven.widgets.checkout_widget

import android.webkit.JavascriptInterface
import com.deuna.maven.shared.*
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.file_downloaders.runOnUiThread
import org.json.*

@Suppress("UNCHECKED_CAST")
class CheckoutBridge(
    deunaWidget: DeunaWidget,
    val callbacks: CheckoutCallbacks,
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

                val event = CheckoutEvent.valueOf(type)
                callbacks.onEventDispatch?.invoke(event, data)

                when (event) {
                    CheckoutEvent.purchase -> {
                        deunaWidget.closeSubWebView()
                        callbacks.onSuccess?.invoke(data["order"] as Json)
                    }

                    CheckoutEvent.purchaseError -> {
                        deunaWidget.closeSubWebView()
                        val error = PaymentsError.fromJson(
                            type = PaymentsError.Type.PAYMENT_ERROR, data = data
                        )
                        callbacks.onError?.invoke(error)
                    }

                    CheckoutEvent.linkFailed, CheckoutEvent.linkCriticalError -> {
                        val error = PaymentsError.fromJson(
                            type = PaymentsError.Type.INITIALIZATION_FAILED, data = data
                        )
                        callbacks.onError?.invoke(error)
                    }

                    CheckoutEvent.linkClose -> {
                        deunaWidget.closeAction = CloseAction.userAction
                        onCloseByUser?.invoke()
                    }

                    CheckoutEvent.paymentMethods3dsInitiated, CheckoutEvent.apmClickRedirect -> {
                        // No action required for these events
                    }

                    else -> {
                        DeunaLogs.debug("CheckoutBridge Unhandled event: $event")
                    }
                }
            } catch (_: IllegalArgumentException) {
            } catch (e: JSONException) {
                DeunaLogs.debug("CheckoutBridge JSONException: $e")
            }
        }
    }
}
