package com.deuna.maven.web_views.dialog_fragments.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
    context: Context,
    val widgetConfiguration: DeunaWidgetConfiguration
) : BaseDialogFragment(context) {

    val deunaWidget: DeunaWidget get() = baseWebView as DeunaWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.deuna_webview_container)

        baseWebView = findViewById(R.id.deuna_webview)

        deunaWidget.widgetConfiguration = widgetConfiguration
        deunaWidget.widgetConfiguration?.onCloseByUser = {
            dismiss()
        }
        deunaWidget.build()
    }


    override fun onBackButtonPressed() {
        if (!deunaWidget.closeEnabled) {
            return
        }
        deunaWidget.closeAction = CloseAction.userAction
        deunaWidget.bridge?.onCloseByUser?.let { it() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
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