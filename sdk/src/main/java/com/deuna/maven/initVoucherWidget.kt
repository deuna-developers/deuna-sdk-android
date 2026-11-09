package com.deuna.maven

import android.content.Context
import com.deuna.maven.internal.modal.DeunaWidgetModalLauncher
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration
import com.deuna.maven.widgets.voucher.VoucherCallbacks

fun DeunaSDK.initVoucherWidget(
    context: Context,
    orderToken: String,
    callbacks: VoucherCallbacks,
    language: String? = null,
    fraudCredentials: Json? = null,
    customUserAgent: String? = null,
    domain: String? = null,
) {

    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }

    DeunaWidgetModalLauncher.launch(
        context = context,
        widgetConfiguration = VoucherWidgetConfiguration(
            sdkInstance = this,
            orderToken = orderToken,
            callbacks = callbacks,
            language = language,
            widgetIntegration = WidgetIntegration.MODAL,
            fraudCredentials = fraudCredentials,
            customUserAgent = customUserAgent,
            domain = domain,
        ),
    )
}
