package com.deuna.maven.web_views.dialog_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.R
import com.deuna.maven.web_views.dialog_fragments.base.BaseDialogFragment
import com.deuna.maven.web_views.external_url.ExternalUrlWebView

class ExternalUrlDialogFragment(private val url: String, val onDialogDestroyed: () -> Unit) : BaseDialogFragment() {

    val webView: ExternalUrlWebView
        get() = baseWebView as ExternalUrlWebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.external_url_webview_container, container, false)
        baseWebView = view.findViewById(R.id.new_tab_webview)
        webView.loadUrl(url)
        webView.onRemoteCloseCalled = { dismiss() }
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