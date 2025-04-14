package com.deuna.maven.widgets.checkout_widget

import com.deuna.maven.DeunaSDK
import com.deuna.maven.client.sendOrder
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.WidgetIntegration
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fetches the order details from the server using the provided credentials.
 * Parses the response to extract the payment link and load it in the WebView.
 */
fun DeunaSDK.buildCheckoutWidgetUrl(
    orderToken: String,
    userToken: String? = null,
    styleFile: String? = null,
    language: String? = null,
    widgetIntegration: WidgetIntegration = WidgetIntegration.EMBEDDED,
    completion: (error: PaymentsError?, url: String?) -> Unit,
) {
    val apiKey = this.publicApiKey
    val baseUrl = this.environment.checkoutBaseUrl

    sendOrder(baseUrl, orderToken, apiKey, object : Callback<Any> {

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

                completion(
                    null,
                    Utils.buildUrl(baseUrl = paymentLink, queryParams = queryParameters)
                )
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

/**
 * This method is called when the order details are not found on the server.
 * It invokes the onError callback with a CheckoutError of type ORDER_NOT_FOUND.
 */
private fun DeunaSDK.orderCouldNotBeRetrieved(): PaymentsError {
    return PaymentsError(
        type = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED,
        metadata = PaymentsError.Metadata(
            code = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED.name,
            message = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED.message,
        )
    )
}