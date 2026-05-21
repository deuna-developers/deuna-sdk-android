package com.deuna.maven.internal.modal

import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration
import com.deuna.maven.widgets.configuration.NextActionWidgetConfiguration
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration

internal fun dispatchOnClosed(
    widgetConfiguration: DeunaWidgetConfiguration,
    closeAction: CloseAction,
) {
    when (widgetConfiguration) {
        is PaymentWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(closeAction)
        is ElementsWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(closeAction)
        is VoucherWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(closeAction)
        is NextActionWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(closeAction)
        is CheckoutWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(closeAction)
    }
}
