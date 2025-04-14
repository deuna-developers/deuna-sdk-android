package com.deuna.maven.widgets.elements_widget

import android.webkit.JavascriptInterface
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.file_downloaders.runOnUiThread
import org.json.*

@Suppress("UNCHECKED_CAST")
class ElementsBridge(
    deunaWidget: DeunaWidget,
    val callbacks: ElementsCallbacks,
    private val closeEvents: Set<ElementsEvent> = emptySet(),
    val onCloseByEvent: VoidCallback? = null,
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

                    ElementsEvent.vaultSaveSuccess -> {
                        deunaWidget.closeSubWebView()
                        callbacks.onSuccess?.invoke(data)
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
                        onCloseByUser?.invoke()
                    }

                    else -> {}
                }

                if (closeEvents.contains(event)) {
                    onCloseByEvent?.invoke()
                }
            } catch (_: IllegalArgumentException) {
            } catch (e: Exception) {
                DeunaLogs.debug("ElementsBridge JSONException: $e")
            }
        }
    }
}