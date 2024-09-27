package com.deuna.maven.web_views

import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.payment_widget.domain.PaymentWidgetBridge
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.WebViewBridge
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.shared.toMap
import com.deuna.maven.web_views.base.BaseWebViewActivity
import org.json.JSONObject

class PaymentWidgetActivity() : BaseWebViewActivity() {
    companion object {
        const val EXTRA_URL = "EXTRA_URL"

        /**
         * Due to multiples instances of DeunaSDK can be created
         * we need to ensure that only the authorized instance can
         * call the callbacks for their widgets
         */
        private var callbacksMap = mutableMapOf<Int, PaymentWidgetCallbacks>()

        private var activities = mutableMapOf<Int, PaymentWidgetActivity>()

        /**
         * Set the callbacks object to receive element events.
         */
        fun setCallbacks(sdkInstanceId: Int, callbacks: PaymentWidgetCallbacks) {
            callbacksMap[sdkInstanceId] = callbacks
        }

        /// send the custom css to the payment link
        fun sendCustomCss(sdkInstanceId: Int, dataAsJsonString: String) {
            val activity = activities[sdkInstanceId] ?: return
            activity.runOnUiThread {
                activity.webView.evaluateJavascript(
                    "setCustomCss($dataAsJsonString);",
                    null
                );
            }
        }

        /**
         * Send a re-fetch order request
         */
        fun refetchOrder(sdkInstanceId: Int, callback: (Json?) -> Unit){
            val activity = activities[sdkInstanceId] ?: return
            activity.runOnUiThread {
                activity.refetchOrder(callback)
            }
        }
    }

    val callbacks: PaymentWidgetCallbacks?
        get() {
            return callbacksMap[sdkInstanceId!!]
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activities[sdkInstanceId!!] = this

        // Extract the URL from the intent
        val url = intent.getStringExtra(ElementsActivity.EXTRA_URL)!!

        // Load the provided URL
        loadUrl(url)

        // Add a JS interface to send re-fetch order requests
        webView.addJavascriptInterface(RefetchOrderBridge(),"refecthOrder")
    }

    override fun getBridge(): WebViewBridge {
        return PaymentWidgetBridge(this)
    }

    override fun onNoInternet() {
        callbacks?.onError?.invoke(PaymentWidgetErrors.noInternetConnection)
    }

    override fun onError() {
        callbacks?.onError?.invoke(PaymentWidgetErrors.initializationFailed)
    }

    override fun onCanceledByUser() {
        callbacks?.onClosed?.invoke(CloseAction.userAction)
        callbacks?.onClosed = null
    }

    override fun onDestroy() {
        // Notify callbacks about activity closure
        callbacks?.onClosed?.invoke(CloseAction.systemAction)
        activities.remove(sdkInstanceId!!)
        super.onDestroy()
    }

    /**
     * Sends a re-fetch order request to the WebView and handles the response.
     *
     * @param callback A callback function to be invoked when the request completes. The callback receives a `Json` object containing the order data or `null` if the request fails.
     */
    fun refetchOrder(callback: (Json?) -> Unit) {
        refetchOrderRequestId++
        refetchOrderRequests[refetchOrderRequestId] = callback

        webView.evaluateJavascript(
            """
        (function() {
            function refetchOrder(callback) {
                deunaRefetchOrder()
                    .then(data => {
                        callback({type:"refetchOrder", data: data , requestId: $refetchOrderRequestId });
                    })
                    .catch(error => {
                        callback({type:"refetchOrder", data: null , requestId: $refetchOrderRequestId });
                    });
            }

            refetchOrder(function(result) {
                refecthOrder.onRefetched(JSON.stringify(result));
            });
        })();
            """.trimIndent(), null
        )
    }

    private val refetchOrderRequests = mutableMapOf<Int, (Json?) -> Unit>()
    private var refetchOrderRequestId = 0

    @Suppress("UNCHECKED_CAST")
    inner class RefetchOrderBridge()  {
        @JavascriptInterface
        fun onRefetched(message: String) {
            try {
                val json = JSONObject(message).toMap()
                val requestId = json["requestId"] as? Int
                if (!refetchOrderRequests.contains(requestId)) {
                    return
                }

                refetchOrderRequests[requestId]?.invoke(
                    json["data"] as? Json
                )
                refetchOrderRequests.remove(requestId)
            } catch (e: Exception) {
                Log.d("WebViewBridge", "postMessage: $e")
            }
        }
    }
}