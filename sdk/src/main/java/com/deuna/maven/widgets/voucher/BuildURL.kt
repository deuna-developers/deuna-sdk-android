package com.deuna.maven.widgets.voucher

import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.WidgetIntegration


fun DeunaSDK.buildVoucherUrl(
    orderToken: String,
    language: String? = null,
    widgetIntegration: WidgetIntegration = WidgetIntegration.EMBEDDED
): String {

    val baseUrl = this.environment.paymentWidgetBaseUrl

    val queryParameters = mutableMapOf(
        QueryParameters.ORDER_TOKEN to orderToken,
        QueryParameters.MODE to QueryParameters.WIDGET,
        QueryParameters.INT to widgetIntegration.value
    )

    if (!language.isNullOrEmpty()) {
        queryParameters[QueryParameters.LANGUAGE] = language
    }


//    val xpropsB64 = mutableMapOf<String, Any>()
//    queryParameters[QueryParameters.XPROPS_B64] = xpropsB64.toBase64()


    return Utils.buildUrl(
        baseUrl = "$baseUrl/voucher",
        queryParams = queryParameters,
    )
}