package com.deuna.maven.widgets.payment_widget

import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.shared.toBase64

/**
 * Build the payment widget url
 */
fun DeunaSDK.buildPaymentWidgetUrl(
    orderToken: String,
    userToken: String? = null,
    styleFile: String? = null,
    paymentMethods: List<Json> = emptyList(),
    checkoutModules: List<Json> = emptyList(),
    language: String? = null,
    behavior: Json? = null,
    widgetIntegration: WidgetIntegration = WidgetIntegration.EMBEDDED
): String {
    val baseUrl = this.environment.paymentWidgetBaseUrl

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
    xpropsB64[QueryParameters.PUBLIC_API_KEY] = publicApiKey


    if (paymentMethods.isNotEmpty()) {
        xpropsB64[QueryParameters.PAYMENT_METHODS] = paymentMethods
    }

    if (checkoutModules.isNotEmpty()) {
        xpropsB64[QueryParameters.CHECKOUT_MODULES] = checkoutModules
    }

    behavior?.let {
       if( it.keys.isNotEmpty() ) {
           xpropsB64[QueryParameters.BEHAVIOR] = it
       }
    }

    queryParameters[QueryParameters.XPROPS_B64] = xpropsB64.toBase64()

    return Utils.buildUrl(
        baseUrl = "$baseUrl/now/$orderToken",
        queryParams = queryParameters,
    )
}