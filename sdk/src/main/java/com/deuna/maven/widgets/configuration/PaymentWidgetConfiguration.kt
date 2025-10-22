package com.deuna.maven.widgets.configuration

import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.shared.toBase64
import com.deuna.maven.widgets.payment_widget.PaymentWidgetCallbacks

class PaymentWidgetConfiguration(
    sdkInstance: DeunaSDK,
    hidePayButton: Boolean = false,
    val orderToken: String,
    val callbacks: PaymentWidgetCallbacks,
    val userToken: String? = null,
    val styleFile: String? = null,
    val paymentMethods: List<Json> = emptyList(),
    val checkoutModules: List<Json> = emptyList(),
    val language: String? = null,
    val behavior: Json? = null,
    val widgetIntegration: WidgetIntegration = WidgetIntegration.EMBEDDED,
    fraudCredentials: Json? = null,
) : DeunaWidgetConfiguration(
    sdkInstance = sdkInstance,
    hidePayButton = hidePayButton,
    fraudCredentials = fraudCredentials,
) {

    override val link: String
        get() {
            val baseUrl = sdkInstance.environment.paymentWidgetBaseUrl

            val queryParameters = mutableMapOf(
                QueryParameters.MODE to QueryParameters.WIDGET,
                QueryParameters.INT to widgetIntegration.value
            )

            if (!language.isNullOrEmpty()) {
                queryParameters[QueryParameters.LANGUAGE] = language
            }

            if (!userToken.isNullOrEmpty()) {
                queryParameters[QueryParameters.USER_TOKEN] = userToken
            }

            if (!styleFile.isNullOrEmpty()) {
                queryParameters[QueryParameters.STYLE_FILE] = styleFile
            }

            val xpropsB64 = mutableMapOf<String, Any>()
            xpropsB64[QueryParameters.PUBLIC_API_KEY] = sdkInstance.publicApiKey


            if (paymentMethods.isNotEmpty()) {
                xpropsB64[QueryParameters.PAYMENT_METHODS] = paymentMethods
            }

            if (checkoutModules.isNotEmpty()) {
                xpropsB64[QueryParameters.CHECKOUT_MODULES] = checkoutModules
            }

            behavior?.let {
                if (it.keys.isNotEmpty()) {
                    xpropsB64[QueryParameters.BEHAVIOR] = it
                }
            }

            queryParameters[QueryParameters.XPROPS_B64] = xpropsB64.toBase64()

            return Utils.buildUrl(
                baseUrl = "$baseUrl/now/$orderToken",
                queryParams = queryParameters,
            )
        }
}
