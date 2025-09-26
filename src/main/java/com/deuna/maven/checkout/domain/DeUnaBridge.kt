package com.deuna.maven.checkout.domain

import OrderResponse
import android.app.Activity
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.checkout.Callbacks
import com.deuna.maven.checkout.CheckoutEvents
import com.deuna.maven.element.domain.ElementEvent
import org.json.JSONException
import org.json.JSONObject

/**
 * The DeUnaBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class DeUnaBridge(
    private val activity: Activity,
    private val callbacks: Callbacks,
    private val closeOnEvents: ArrayList<String>? = null
) {
    /**
     * Called when the activity is starting.
     */
    @JavascriptInterface
    fun postMessage(message: String) {
        val eventData: OrderResponse?
        try {
            val json = JSONObject(message)
            eventData = OrderResponse.fromJson(json)
            callbacks.eventListener?.invoke(eventData, eventData.type)
            when (eventData.type) {
                CheckoutEvents.purchase, CheckoutEvents.apmSuccess -> {
                    handleSuccess(eventData)
                }
                CheckoutEvents.purchaseRejected -> {
                    handleError("An error ocurred while processing payment","purchaseRejected", eventData)
                }
                CheckoutEvents.linkFailed, CheckoutEvents.purchaseError -> {
                    handleError("Failed to initialize the checkout","checkoutError", eventData)
                }
                CheckoutEvents.changeAddress -> {
                    handleCloseActivity(eventData, eventData.type)
                }
                else -> {
                    Log.d("DeUnaBridge", "Unhandled event: $eventData")
                    eventData.let {
                        if (closeOnEvents?.contains(it.type.value) == true) {
                            callbacks.onClose?.invoke(activity)
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            Log.d("DeUnaBridge", "JSONException: $e")
        }
    }

    private fun handleCloseActivity(data: OrderResponse, type: CheckoutEvents) {
        callbacks.eventListener?.invoke(data, type)
    }

    private fun handleError(message: String, type: String, response: OrderResponse) {
        callbacks.onError?.invoke(
            DeunaErrorMessage(
                message,
                type, // Internet Connection // Checkout failed
                response.data.order,
                response.data.user
            )
        )
    }

    private fun handleSuccess(data: OrderResponse) {
        callbacks.onSuccess?.invoke(
            data
        )
    }

}