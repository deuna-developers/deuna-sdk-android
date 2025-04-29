package com.deuna.maven.widgets.elements_widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.dialog_fragments.base.DeunaDialogFragment

class ElementsWidgetDialogFragment(
    private val url: String,
    val callbacks: ElementsCallbacks,
    val closeEvents: Set<ElementsEvent> = emptySet(),
) : DeunaDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        deunaWidget.bridge = ElementsBridge(
            deunaWidget = deunaWidget,
            callbacks = callbacks,
            onCloseByUser = {
                dismiss()
            },
            closeEvents = closeEvents,
            onCloseByEvent = {
                dismiss()
            }
        )
        baseWebView.loadUrl(url)
        return view
    }

    override fun onDetach() {
        super.onDetach()
        callbacks.onClosed?.invoke(deunaWidget.closeAction)
    }
}