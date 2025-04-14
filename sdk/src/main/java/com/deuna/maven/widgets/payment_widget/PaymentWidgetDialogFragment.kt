package com.deuna.maven.widgets.payment_widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.dialog_fragments.base.DeunaDialogFragment


class PaymentWidgetDialogFragment(
    private val url: String,
    val callbacks: PaymentWidgetCallbacks,
) : DeunaDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        deunaWidget.bridge = PaymentWidgetBridge(
            deunaWidget = deunaWidget,
            callbacks = callbacks,
            onCloseByUser = {
                callbacks.onClosed?.invoke(CloseAction.userAction)
                dismiss()
            },
        )
        baseWebView.loadUrl(url)
        return view
    }
}