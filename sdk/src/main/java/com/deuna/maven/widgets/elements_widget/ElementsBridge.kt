package com.deuna.maven.widgets.elements_widget

import android.webkit.JavascriptInterface
import com.deuna.maven.shared.*
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.file_downloaders.runOnUiThread
import com.deuna.maven.widgets.checkout_widget.CheckoutEvent
import org.json.*

@Suppress("UNCHECKED_CAST")
class ElementsBridge(
    deunaWidget: DeunaWidget,
    val callbacks: ElementsCallbacks,
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

                val event = ElementsEvent.valueOf(type)
                callbacks.onEventDispatch?.invoke(event, data)

                when (event) {

                    ElementsEvent.onBinDetected -> {
                        handleCardBinDetected(data["metadata"] as? Json)
                    }

                    ElementsEvent.vaultSaveSuccess -> {
                        deunaWidget.closeSubWebView()
                        deunaWidget.widgetConfiguration?.hasReportedSuccess = true
                        callbacks.onSuccess?.invoke(deunaWidget.buildSuccessPayload(data))
                    }

                    ElementsEvent.vaultSaveError -> {
                        deunaWidget.closeSubWebView()
                        val error = ElementsError.fromJson(
                            type = ElementsError.Type.VAULT_SAVE_ERROR,
                            data = data
                        )
                        if (error != null) {
                            callbacks.onError?.invoke(error)
                        }
                    }

                    ElementsEvent.vaultClosed -> {
                        deunaWidget.closeAction = CloseAction.userAction
                        onCloseByUser?.invoke()
                    }

                    ElementsEvent.onInstallmentSelected -> {
                        callbacks.onInstallmentSelected?.invoke(data["metadata"] as? Json)
                    }

                    else -> {}
                }
            } catch (_: IllegalArgumentException) {
            } catch (e: Exception) {
                DeunaLogs.debug("ElementsBridge JSONException: $e")
            }
        }
    }


    private fun handleCardBinDetected(metadata: Json?) {
        callbacks.onCardBinDetected?.invoke(metadata)
    }
}
