package com.deuna.maven.checkout.domain

import android.app.Activity
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.DeUnaSdk
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
    private val callbacks: Callbacks
) {
    /**
     * Called when the activity is starting.
     */
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            val json = JSONObject(message)
            val eventType = CheckoutEvents.valueOf(json.getString("type"))
            Log.d("DeUnaBridge", "eventType: $eventType")
            when (eventType) {
                // Flujo sin 3DS
                CheckoutEvents.purchase -> {
                    callbacks.onSuccess?.invoke(
                        OrderSuccessResponse.fromJson(json.getJSONObject("data"))
                    )
                }
                CheckoutEvents.apmSuccess -> {
                    callbacks.onSuccess?.invoke(
                        OrderSuccessResponse.fromJson(json.getJSONObject("data"))
                    )
                }
                CheckoutEvents.purchaseRejected -> {
                    callbacks.onError?.invoke(
                        null,
                        "Purchase was rejected"
                    )
                }
                CheckoutEvents.linkFailed -> {
                    callbacks.onError?.invoke(
                        OrderErrorResponse.fromJson(json.getJSONObject("data")),
                        null
                    )
                }
                CheckoutEvents.purchaseError -> {
                    // TODO: devolver en el onError, el activity para que el merchant cierre el proceso
                    callbacks.onError?.invoke(
                        OrderErrorResponse.fromJson(json.getJSONObject("data")),
                        null
                    )
                }
                CheckoutEvents.changeAddress -> {
                    activity.finish()
                }
                else -> {
                    Log.d("DeUnaBridge", "Unhandled event: $eventType")
                }
            }
        } catch (e: JSONException) {
            Log.d("DeUnaBridge", "JSONException: $e")
        }
    }

//    private fun handleEvent(eventTypeString: String) {
//        val json = JSONObject(eventTypeString)
//        when (val eventType = CheckoutEvents.valueOf(json.getString("type"))) {
//            CheckoutEvents.LINKCLOSE -> handleCloseEvent()
//            CheckoutEvents.CHANGE_ADDRESS -> handleChangeAddressEvent()
//            else -> handleOtherEvent(eventType)
//        }
//    }

//    private fun handleCloseEvent() {
//        webView.post {
//            webView.visibility = View.GONE
//        }
//    }
//
//    private fun handleChangeAddressEvent() {
//        callbacks.onChangeAddress?.invoke(webView)
//    }
//
//    private fun handleOtherEvent(eventType: CheckoutEvents) {
//        if (eventType in (closeOnEvents ?: emptyArray())) {
//            callbacks.onClose?.invoke(webView)
//        }
//    }
}