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
        const val EXTRA_USER_TOKEN = "USER_TOKEN"
        const val EXTRA_BASE_URL = "BASE_URL"


        /**
         * Due to multiples instances of DeunaSDK can be created
         * we need to ensure that only the authorized instance can
         * call the callbacks for their widgets
         */
        private var callbacksMap = mutableMapOf<Int, CheckoutCallbacks>()

        /**
         * Set the callbacks object to receive checkout events.
         */
        fun setCallbacks(sdkInstanceId: Int, callbacks: CheckoutCallbacks) {
            callbacksMap[sdkInstanceId] = callbacks
        }
    }

    // Set of CheckoutEvents indicating when to close the activity
    private lateinit var closeEvents: Set<CheckoutEvent>

    val callbacks: CheckoutCallbacks?
        get() {
            return callbacksMap[sdkInstanceId!!]
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Extract data from the intent
        val baseUrl = intent.getStringExtra(EXTRA_BASE_URL)!!
        val orderToken = intent.getStringExtra(EXTRA_ORDER_TOKEN)!!
        val apiKey = intent.getStringExtra(EXTRA_API_KEY)!!
        val userToken = intent.getStringExtra(EXTRA_USER_TOKEN)

        val closeEventAsStrings =
            intent.getStringArrayListExtra(EXTRA_CLOSE_EVENTS) ?: emptyList<String>()
        closeEvents = parseCloseEvents<CheckoutEvent>(closeEventAsStrings)

        // Initiate the checkout process by fetching the order API
        getOrderApi(
            baseUrl = baseUrl, orderToken = orderToken, apiKey = apiKey, userToken = userToken
        )
    }

    /**
     * Fetches the order details from the server using the provided credentials.
     * Parses the response to extract the payment link and load it in the WebView.
     */
    private fun getOrderApi(
        baseUrl: String, orderToken: String, apiKey: String, userToken: String?
    ) {
        sendOrder(baseUrl, orderToken, apiKey, object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val responseBody = response.body() as? Map<*, *>
                    val orderMap = responseBody?.get("order") as? Map<*, *>

                    if (orderMap == null) {
                        callbacks?.onError?.invoke(
                            PaymentWidgetErrors.linkCouldNotBeGenerated
                        )
                        return
                    }

                    val paymentLink = orderMap["payment_link"] as String?

                    if (paymentLink.isNullOrEmpty()) {
                        callbacks?.onError?.invoke(
                            PaymentWidgetErrors.linkCouldNotBeGenerated
                        )
                        return
                    }

                    val queryParameters = mutableMapOf<String, String>()
                    queryParameters[QueryParameters.MODE.value] = QueryParameters.WIDGET.value

                    if (userToken != null) {
                        queryParameters[QueryParameters.USER_TOKEN.value] = userToken
                    }
                    
                    loadUrl(
                        url = Utils.buildUrl(baseUrl = paymentLink, queryParams = queryParameters)
                    )
                } else {
                    // Handle missing order data
                    orderCouldNotBeRetrieved()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                orderCouldNotBeRetrieved()
            }
        })
    }

    /**
     * This method is called when the order details are not found on the server.
     * It invokes the onError callback with a CheckoutError of type ORDER_NOT_FOUND.
     */
    private fun orderCouldNotBeRetrieved() {
        callbacks?.onError?.invoke(
            PaymentsError(
                type = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED,
                metadata = PaymentsError.Metadata(
                    code = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED.name,
                    message = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED.message,
                )
            )
        )
    }

    override fun getBridge(): WebViewBridge {
        return CheckoutBridge(
            activity = this,
            closeEvents = closeEvents,
        )
    }

    override fun onNoInternet() {
        callbacks?.onError?.invoke(
            PaymentWidgetErrors.noInternetConnection
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