package com.deuna.maven.web_views

import android.os.*
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.client.*
import com.deuna.maven.shared.*
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.web_views.base.*
import retrofit2.*
import java.net.*

/**
 * This activity handles the checkout process for Deuna. It retrieves the payment link
 * from the server and loads it in a WebView. It also communicates with the CheckoutCallbacks
 * interface to report success, errors, or cancellation.
 */
class CheckoutActivity() : BaseWebViewActivity() {

    companion object {
        const val EXTRA_API_KEY = "API_KEY"
        const val EXTRA_ORDER_TOKEN = "ORDER_TOKEN"
        const val EXTRA_BASE_URL = "BASE_URL"
        private var callbacks: CheckoutCallbacks? = null

        /**
         * Set the callbacks object to receive checkout events.
         */
        fun setCallbacks(callbacks: CheckoutCallbacks) {
            this.callbacks = callbacks
        }
    }

    // Set of CheckoutEvents indicating when to close the activity
    private lateinit var closeEvents: Set<CheckoutEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract data from the intent
        val baseUrl = intent.getStringExtra(EXTRA_BASE_URL)!!
        val orderToken = intent.getStringExtra(EXTRA_ORDER_TOKEN)!!
        val apiKey = intent.getStringExtra(EXTRA_API_KEY)!!

        val closeEventAsStrings = intent.getStringArrayListExtra(EXTRA_CLOSE_EVENTS) ?: emptyList<String>()
        closeEvents = parseCloseEvents<CheckoutEvent>(closeEventAsStrings)

        // Initiate the checkout process by fetching the order API
        getOrderApi(baseUrl = baseUrl, orderToken = orderToken, apiKey = apiKey)
    }

    /**
     * Fetches the order details from the server using the provided credentials.
     * Parses the response to extract the payment link and load it in the WebView.
     */
    private fun getOrderApi(baseUrl: String, orderToken: String, apiKey: String) {
        sendOrder(baseUrl, orderToken, apiKey, object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val responseBody = response.body() as? Map<*, *>
                    val orderMap = responseBody?.get("order") as? Map<*, *>
                    if (orderMap != null) {
                        val parsedUrl = URL(orderMap["payment_link"].toString())
                        loadUrl(
                            url = parsedUrl.toString()
                        )
                    } else {
                        // Handle missing order data
                        orderNotFound()
                    }
                } else {
                    // Handle missing order data
                    orderNotFound()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                orderNotFound()
            }
        })
    }

    /**
     * This method is called when the order details are not found on the server.
     * It invokes the onError callback with a CheckoutError of type ORDER_NOT_FOUND.
     */
    private fun orderNotFound() {
        callbacks?.onError?.invoke(
            CheckoutError(
                type = CheckoutErrorType.ORDER_NOT_FOUND,
                order = null,
                user = null
            )
        )
    }

    override fun getBridge(): WebViewBridge {
        return CheckoutBridge(
            context = this,
            callbacks = callbacks,
            closeEvents = closeEvents,
        )
    }

    override fun onNoInternet() {
        callbacks?.onError?.invoke(
            CheckoutError(
                CheckoutErrorType.NO_INTERNET_CONNECTION, null, null
            )
        )
    }

    override fun onCanceledByUser() {
        callbacks?.onCanceled?.invoke()
    }

    override fun onDestroy() {
        callbacks?.onClosed?.invoke()
        super.onDestroy()
    }

}