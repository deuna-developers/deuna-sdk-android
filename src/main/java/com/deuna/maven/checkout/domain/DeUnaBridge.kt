package com.deuna.maven.checkout.domain

import android.app.Activity
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.checkout.Callbacks
import com.deuna.maven.checkout.CheckoutEvents
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
        var eventType: CheckoutEvents? = null
        try {
            val json = JSONObject(message)
            eventType = CheckoutEvents.valueOf(json.getString("type"))
            Log.d("DeUnaBridge", "eventType: $eventType")
            when (eventType) {
                // Flujo sin 3DS
                CheckoutEvents.purchase, CheckoutEvents.apmSuccess -> {
                    handleSuccess(json.getJSONObject("data"))
                }

                CheckoutEvents.purchaseRejected -> {
                    handleError(json.getJSONObject("data"))
                }

                CheckoutEvents.linkFailed, CheckoutEvents.purchaseError -> {
                    handleError(json.getJSONObject("data"))
                }

                CheckoutEvents.changeAddress -> {
                    handleCloseActivity()
                }

                else -> {
                    Log.d("DeUnaBridge", "Unhandled event: $eventType")
                    eventType.let {
                        if (closeOnEvents?.contains(it.name) == true) {
                            handleCloseActivity()
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            Log.d("DeUnaBridge", "JSONException: $e")
        }
    }

    private fun handleCloseActivity() {
        activity.finish()
    }

    private fun handleError(jsonObject: JSONObject) {
        callbacks.onError?.invoke(
            OrderErrorResponse.fromJson(jsonObject.getJSONObject("data")),
            null
        )
    }

    private fun handleSuccess(jsonObject: JSONObject) {
        callbacks.onSuccess?.invoke(
            OrderSuccessResponse.fromJson(JSONObject())
        )
    }

}