package com.deuna.maven.element.domain

import android.app.Activity
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject

/**
 * The DeUnaElementBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class DeUnaElementBridge(
    private val callbacks: ElementCallbacks,
    private val activity: Activity,
    private val closeOnEvents: ArrayList<String>? = null
) {
    /**
     * The postMessage function is called when a message is received from JavaScript code in a WebView.
     * The message is parsed and the corresponding callbacks are called based on the event type.
     */
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            handleEvent(message)
        } catch (e: Exception) {
            Log.d("DeUnaElementBridge", "postMessage: $e")
        }
    }

    private fun handleEvent(eventTypeString: String) {
        try {
            val json = JSONObject(eventTypeString)
            val eventType = ElementEvent.valueOf(json.getString("type"))
            when (eventType) {
                ElementEvent.vaultFailed -> handleError(json)
                ElementEvent.cardCreationError -> handleError(json)
                ElementEvent.vaultSaveError -> handleError(json)
                ElementEvent.vaultSaveSuccess -> handleSuccess(json)
                ElementEvent.vaultClosed -> handleCloseEvent()
                ElementEvent.cardSuccessfullyCreated -> handleSuccess(json)
                ElementEvent.changeAddress -> handleChangeAddressEvent()
                else -> {
                    Log.d("DeUnaElementBridge", "Unhandled event: $eventType")
                    eventType.let {
                        if (closeOnEvents?.contains(it.name) == true) {
                            handleCloseEvent()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("DeUnaElementBridge", "handleEvent: $e")
        }
    }

    private fun handleCloseEvent() {
        activity.finish()
    }

    private fun handleSuccess(jsonObject: JSONObject) {
        callbacks.onSuccess?.invoke(
            ElementSuccessResponse.fromJson(jsonObject.getJSONObject("data"))
        )
    }

    private fun handleError(jsonObject: JSONObject) {
        callbacks.onError?.invoke(
            ElementErrorResponse.fromJson(jsonObject.getJSONObject("data")),
            null
        )
    }

    private fun handleChangeAddressEvent() {
        callbacks.onChangeAddress?.invoke(activity)
    }

}