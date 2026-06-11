package com.deuna.maven.web_views.deuna.extensions

import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.widgets.checkout_widget.CheckoutBridge
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration
import com.deuna.maven.widgets.configuration.NextActionWidgetConfiguration
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration
import com.deuna.maven.widgets.elements_widget.ElementsBridge
import com.deuna.maven.widgets.next_action.NextActionBridge
import com.deuna.maven.widgets.payment_widget.PaymentWidgetBridge
import com.deuna.maven.widgets.voucher.VoucherBridge


fun DeunaWidget.buildBridge() {
    widgetConfiguration?.let {
        bridge = when (it) {
            is PaymentWidgetConfiguration -> PaymentWidgetBridge(
                deunaWidget = this,
                callbacks = it.callbacks,
                onCloseByUser = {
                    widgetConfiguration?.onCloseByUser?.invoke()
                },
            )

            is CheckoutWidgetConfiguration -> CheckoutBridge(
                deunaWidget = this,
                callbacks = it.callbacks,
                onCloseByUser = {
                    widgetConfiguration?.onCloseByUser?.invoke()
                },
            )
            is ElementsWidgetConfiguration -> ElementsBridge(
                deunaWidget = this,
                callbacks = it.callbacks,
                onCloseByUser = {
                    widgetConfiguration?.onCloseByUser?.invoke()
                },
            )

            is NextActionWidgetConfiguration -> NextActionBridge(
                deunaWidget = this,
                callbacks = it.callbacks,
                onCloseByUser = {
                    widgetConfiguration?.onCloseByUser?.invoke()
                },
            )
            is VoucherWidgetConfiguration -> VoucherBridge(
                deunaWidget = this,
                callbacks = it.callbacks,
                onCloseByUser = {
                    widgetConfiguration?.onCloseByUser?.invoke()
                },
            )
        }
    }
}