package com.deuna.maven.widgets.next_action

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.dialog_fragments.base.DeunaDialogFragment

class NextActionDialogFragment(
    private val url: String,
    val callbacks: NextActionCallbacks,
) : DeunaDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        deunaWidget.bridge = NextActionBridge(
            deunaWidget = deunaWidget,
            callbacks = callbacks,
            onCloseByUser = {
                dismiss()
            },
        )
        baseWebView.loadUrl(url)
        return view
    }

    override fun onDetach() {
        super.onDetach()
        callbacks.onClosed?.invoke(deunaWidget.closeAction)
    }
}