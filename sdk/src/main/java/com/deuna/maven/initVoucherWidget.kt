package com.deuna.maven

import android.content.Context
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration
import com.deuna.maven.widgets.voucher.VoucherCallbacks
import com.deuna.maven.widgets.voucher.VoucherDialogFragment

fun DeunaSDK.initVoucherWidget(
    context: Context,
    orderToken: String,
    callbacks: VoucherCallbacks,
    language: String? = null,
    domain: String? = null,
) {

    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }

    dialogFragment = VoucherDialogFragment(
        context,
        widgetConfiguration = VoucherWidgetConfiguration(
            sdkInstance = this,
            orderToken = orderToken,
            callbacks = callbacks,
            language = language,
            widgetIntegration = WidgetIntegration.MODAL,
            domain = domain,
        )
    )
    dialogFragment?.show()
}