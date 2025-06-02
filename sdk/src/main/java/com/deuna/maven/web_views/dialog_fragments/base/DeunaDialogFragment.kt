package com.deuna.maven.web_views.dialog_fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.R
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.build
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration
import com.deuna.maven.widgets.configuration.NextActionWidgetConfiguration
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration

abstract class DeunaDialogFragment(
    val widgetConfiguration: DeunaWidgetConfiguration
) : BaseDialogFragment() {

    val deunaWidget: DeunaWidget get() = baseWebView as DeunaWidget

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.deuna_webview_container, container, false)
        baseWebView = view.findViewById(R.id.deuna_webview)

        deunaWidget.widgetConfiguration = widgetConfiguration
        deunaWidget.widgetConfiguration?.onCloseByUser = {
            dismiss()
        }
        deunaWidget.build()
        return view
    }

    override fun onBackButtonPressed() {
        if (!deunaWidget.closeEnabled) {
            return
        }
        deunaWidget.closeAction = CloseAction.userAction
        deunaWidget.bridge?.onCloseByUser?.let { it() }
    }

    override fun onDetach() {
        super.onDetach()
        when (widgetConfiguration) {
            is PaymentWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(
                deunaWidget.closeAction
            )

            is ElementsWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(
                deunaWidget.closeAction
            )

            is VoucherWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(
                deunaWidget.closeAction
            )

            is NextActionWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(
                deunaWidget.closeAction
            )

            is CheckoutWidgetConfiguration -> widgetConfiguration.callbacks.onClosed?.invoke(
                deunaWidget.closeAction
            )
        }
    }
}