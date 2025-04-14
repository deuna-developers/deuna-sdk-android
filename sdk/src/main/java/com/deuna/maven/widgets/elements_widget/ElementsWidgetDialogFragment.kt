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
                callbacks.onClosed?.invoke(CloseAction.userAction)
                dismiss()
            },
            closeEvents = closeEvents,
            onCloseByEvent = {
                callbacks.onClosed?.invoke(CloseAction.systemAction)
                dismiss()
            }
        )
        baseWebView.loadUrl(url)
        return view
    }
}