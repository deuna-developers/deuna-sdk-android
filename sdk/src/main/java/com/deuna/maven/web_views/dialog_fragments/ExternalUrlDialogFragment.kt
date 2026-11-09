package com.deuna.maven.web_views.dialog_fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.R
import com.deuna.maven.web_views.dialog_fragments.base.BaseDialogFragment
import com.deuna.maven.web_views.external_url.ExternalUrlWebView

class ExternalUrlDialogFragment(
    context: Context,
    private val url: String, val onDialogDestroyed: () -> Unit
) : BaseDialogFragment(context) {

    val webView: ExternalUrlWebView
        get() = baseWebView as ExternalUrlWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.external_url_webview_container)

        baseWebView = findViewById(R.id.new_tab_webview)
        webView.launch(url)
        webView.onRemoteCloseCalled = { dismiss() }
    }

    override fun onBackButtonPressed() {
        dismiss()
    }

    override fun onStop() {
        onDialogDestroyed()
        super.onStop()
    }
}