package com.deuna.maven.widgets.configuration

import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.widgets.voucher.VoucherCallbacks


class VoucherWidgetConfiguration(
    sdkInstance: DeunaSDK,
    hidePayButton: Boolean = false,
    domain: String? = null,
    val orderToken: String,
    val callbacks: VoucherCallbacks,
    val language: String? = null,
    val widgetIntegration: WidgetIntegration = WidgetIntegration.EMBEDDED,
    fraudCredentials: Json? = null,
    customUserAgent: String? = null,
) : DeunaWidgetConfiguration(
    sdkInstance = sdkInstance,
    hidePayButton = hidePayButton,
    fraudCredentials = fraudCredentials,
    customUserAgent = customUserAgent,
    domain = domain,
) {
    override val link: String
        get() {
            var baseUrl = sdkInstance.environment.paymentWidgetBaseUrl

            domain?.let {
                baseUrl = overrideBaseUrl(baseUrl, it)
            }

            val queryParameters = mutableMapOf(
                QueryParameters.ORDER_TOKEN to orderToken,
                QueryParameters.MODE to QueryParameters.WIDGET,
                QueryParameters.INT to widgetIntegration.value
            )

            if (!language.isNullOrEmpty()) {
                queryParameters[QueryParameters.LANGUAGE] = language
            }

            return Utils.buildUrl(
                baseUrl = "$baseUrl/voucher",
                queryParams = queryParameters,
            )
        }
}
