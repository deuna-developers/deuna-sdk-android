package com.deuna.maven.widgets.next_action

import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.WidgetIntegration


fun DeunaSDK.buildNextActionUrl(
    orderToken: String,
    language: String? = null,
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

    return Utils.buildUrl(
        baseUrl = "$baseUrl/next-action-purchase/$orderToken",
        queryParams = queryParameters
    )
}