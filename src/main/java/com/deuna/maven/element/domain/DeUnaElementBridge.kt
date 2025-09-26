package com.deuna.maven.element.domain

import ElementResponse
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
            val eventData = ElementResponse.fromJson(json)
            Log.d("DeUnaElementBridge", "handleEvent: $json")
            callbacks.eventListener?.invoke(eventData, eventData.type)
            when (eventData.type) {
                ElementEvent.vaultFailed -> eventData.data.metadata?.let {
                    handleError(
                        it.errorMessage,
                        "vaultFailed",
                        eventData
                    )
                }

                ElementEvent.cardCreationError -> eventData.data.metadata?.let {
                    handleError(
                        it.errorMessage,
                        "cardCreationError",
                        eventData
                    )
                }

                ElementEvent.vaultSaveError -> eventData.data.metadata?.let {
                    handleError(
                        it.errorMessage,
                        "vaultSaveError",
                        eventData
                    )
                }

                ElementEvent.vaultSaveSuccess -> handleSuccess(eventData)
                ElementEvent.vaultClosed -> handleCloseEvent()
                ElementEvent.cardSuccessfullyCreated -> handleSuccess(eventData)
                else -> {
                    Log.d("DeUnaElementBridge", "Unhandled event: ${eventData.type}")
                    eventData.let {
                        if (closeOnEvents?.contains(it.type.value) == true) {
                            callbacks.eventListener?.invoke(it, it.type)
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

    private fun handleSuccess(data: ElementResponse) {
        callbacks.onSuccess?.invoke(
            data
        )
    }

    private fun handleError(message: String, type: String, response: ElementResponse) {
        callbacks.onError?.invoke(
            ElementErrorMessage(
                message,
                type, // Internet Connection // Checkout failed
                response.data.order,
                response.data.user
            )
        )
    }


}