package com.deuna.maven.widgets.configuration

import com.deuna.maven.DeunaSDK
import com.deuna.maven.client.sendOrder
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.widgets.checkout_widget.CheckoutEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckoutWidgetConfiguration(
    sdkInstance: DeunaSDK,
    hidePayButton: Boolean = false,
    domain: String? = null,
    val orderToken: String,
    val callbacks: CheckoutCallbacks,
    val userToken: String? = null,
    val styleFile: String? = null,
    val language: String? = null,
    val widgetIntegration: WidgetIntegration = WidgetIntegration.EMBEDDED,
) : DeunaWidgetConfiguration(
    sdkInstance = sdkInstance,
    hidePayButton = hidePayButton,
    domain = domain,
) {

    private var paymentLink: String? = null

    /**
     * Sends a request to the DEUNA API to retrieve the payment link for the order.
     * The link is then used to construct the final URL for the widget.
     */
    fun getPaymentLink(
        completion: (error: PaymentsError?, url: String?) -> Unit
    ) {
        sendOrder(
            baseUrl = sdkInstance.environment.checkoutBaseUrl,
            orderToken = orderToken,
            apiKey = sdkInstance.publicApiKey, object : Callback<Any> {

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body() as? Map<*, *>
                        val orderMap = responseBody?.get("order") as? Map<*, *>

                        if (orderMap == null) {
                            completion(PaymentWidgetErrors.linkCouldNotBeGenerated, null)
                            return
                        }

                        val paymentLink = orderMap["payment_link"] as String?

                        if (paymentLink.isNullOrEmpty()) {
                            completion(PaymentWidgetErrors.linkCouldNotBeGenerated, null)
                            return
                        }

                        val queryParameters = mutableMapOf(
                            QueryParameters.MODE to QueryParameters.WIDGET,
                            QueryParameters.INT to widgetIntegration.value
                        )

                        if (userToken != null) {
                            queryParameters[QueryParameters.USER_TOKEN] = userToken
                        }

                        if (styleFile != null) {
                            queryParameters[QueryParameters.STYLE_FILE] = styleFile
                        }

                        if (!language.isNullOrEmpty()) {
                            queryParameters[QueryParameters.LANGUAGE] = language
                        }

                        val link = Utils.buildUrl(
                            baseUrl = paymentLink,
                            queryParams = queryParameters,
                        )

                        this@CheckoutWidgetConfiguration.paymentLink = link
                        completion(null, link)
                    } else {
                        // Handle missing order data
                        completion(orderCouldNotBeRetrieved(), null)
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    completion(orderCouldNotBeRetrieved(), null)
                }
            })
    }


    override val link: String
        get() {
            if (paymentLink == null) {
                return "";
            }

            val queryParameters = mutableMapOf(
                QueryParameters.MODE to QueryParameters.WIDGET,
                QueryParameters.INT to widgetIntegration.value
            )

            if (userToken != null) {
                queryParameters[QueryParameters.USER_TOKEN] = userToken
            }

            if (styleFile != null) {
                queryParameters[QueryParameters.STYLE_FILE] = styleFile
            }

            if (!language.isNullOrEmpty()) {
                queryParameters[QueryParameters.LANGUAGE] = language
            }

            var baseUrl = paymentLink!!
            domain?.let {
                baseUrl = overrideBaseUrl(baseUrl, it)
            }

            return Utils.buildUrl(baseUrl = baseUrl, queryParams = queryParameters)
        }
}


/**
 * This method is called when the order details are not found on the server.
 * It invokes the onError callback with a CheckoutError of type ORDER_NOT_FOUND.
 */
private fun orderCouldNotBeRetrieved(): PaymentsError {
    return PaymentsError(
        type = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED,
        metadata = PaymentsError.Metadata(
            code = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED.name,
            message = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED.message,
        )
    )
}