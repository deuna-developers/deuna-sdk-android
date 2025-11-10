package com.deuna.maven.widgets.checkout_widget

import android.content.Context
import com.deuna.maven.web_views.dialog_fragments.base.DeunaDialogFragment
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration

class CheckoutWidgetDialogFragment(
    context: Context,
    widgetConfiguration: CheckoutWidgetConfiguration,
) : DeunaDialogFragment(
    context,
    widgetConfiguration = widgetConfiguration
)