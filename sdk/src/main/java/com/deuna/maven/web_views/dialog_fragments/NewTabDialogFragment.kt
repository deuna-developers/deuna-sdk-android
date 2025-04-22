package com.deuna.maven.web_views.dialog_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.R
import com.deuna.maven.web_views.dialog_fragments.base.BaseDialogFragment
import com.deuna.maven.web_views.new_tab.NewTabWebView

class NewTabDialogFragment(private val url: String, val onDialogDestroyed: () -> Unit) : BaseDialogFragment() {

    val newTabWebView: NewTabWebView
        get() = baseWebView as NewTabWebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.new_tab_webview_container, container, false)
        baseWebView = view.findViewById(R.id.new_tab_webview)
        newTabWebView.loadUrl(url)
        newTabWebView.onRemoteCloseCalled = { dismiss() }
        return view
    }

    override fun onBackButtonPressed() {
        dismiss()
    }

    override fun onDestroyView() {
        onDialogDestroyed()
        super.onDestroyView()
    }
}