package com.deuna.maven.element.domain

import android.webkit.JavascriptInterface
import com.deuna.maven.*
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.widgets.ElementsActivity
import org.json.*

@Suppress("UNCHECKED_CAST")
class ElementsBridge(
    private val activity: ElementsActivity,
    private val closeEvents: Set<ElementsEvent>,
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

            val event = ElementsEvent.valueOf(type)
            activity.callbacks?.eventListener?.invoke(event, data)
            activity.callbacks?.onEventDispatch?.invoke(event, data)

            when (event) {

                ElementsEvent.vaultSaveSuccess -> {
                    activity.closeSubWebView()
                    activity.callbacks?.onSuccess?.invoke(data)
                }

                ElementsEvent.vaultSaveError -> {
                    activity.closeSubWebView()
                    val error = ElementsError.fromJson(
                        type = ElementsError.Type.VAULT_SAVE_ERROR,
                        data = data
                    )
                    if (error != null) {
                        activity.callbacks?.onError?.invoke(error)
                    }
                }

                ElementsEvent.vaultClosed -> {
                    activity.onCanceledByUser()
                    closeWebView(activity.sdkInstanceId!!)
                }

                else -> {}
            }

            if (closeEvents.contains(event)) {
                closeWebView(activity.sdkInstanceId!!)
            }
        } catch (_: IllegalArgumentException) {
        } catch (e: Exception) {
            DeunaLogs.debug("ElementsBridge JSONException: $e")
        }
    }
}