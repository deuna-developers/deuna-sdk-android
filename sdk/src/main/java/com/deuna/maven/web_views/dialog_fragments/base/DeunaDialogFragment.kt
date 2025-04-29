package com.deuna.maven.web_views.dialog_fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.R
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.deuna.DeunaWidget

abstract class DeunaDialogFragment : BaseDialogFragment(){

    val deunaWidget: DeunaWidget get() = baseWebView as DeunaWidget

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.deuna_webview_container, container, false)
        baseWebView = view.findViewById(R.id.deuna_webview)
        return view
    }

    override fun onBackButtonPressed() {
        if (!deunaWidget.closeEnabled) {
            return
        }
        deunaWidget.closeAction = CloseAction.userAction
        deunaWidget.bridge?.onCloseByUser?.let { it() }
    }
}