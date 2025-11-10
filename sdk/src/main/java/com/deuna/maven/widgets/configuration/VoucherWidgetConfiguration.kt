package com.deuna.maven.widgets.configuration

import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.widgets.voucher.VoucherCallbacks


class VoucherWidgetConfiguration(
    sdkInstance: DeunaSDK,
    hidePayButton: Boolean = false,
    val orderToken: String,
    val callbacks: VoucherCallbacks,
    val language: String? = null,
    val widgetIntegration: WidgetIntegration = WidgetIntegration.EMBEDDED,
) : DeunaWidgetConfiguration(
    sdkInstance = sdkInstance,
    hidePayButton = hidePayButton,
) {
    override val link: String
        get() {
            val baseUrl = sdkInstance.environment.paymentWidgetBaseUrl

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