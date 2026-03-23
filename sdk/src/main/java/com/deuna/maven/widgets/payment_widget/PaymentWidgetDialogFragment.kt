package com.deuna.maven.widgets.payment_widget

import android.content.Context
import com.deuna.maven.web_views.dialog_fragments.base.DeunaDialogFragment
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration


class PaymentWidgetDialogFragment(
    context: Context,
    widgetConfiguration: PaymentWidgetConfiguration,
) : DeunaDialogFragment(
    context,
    widgetConfiguration = widgetConfiguration
)