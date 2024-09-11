package com.deuna.maven.web_views

import android.os.Bundle
import com.deuna.maven.payment_widget.domain.PaymentWidgetBridge
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.WebViewBridge
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.base.BaseWebViewActivity

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
}