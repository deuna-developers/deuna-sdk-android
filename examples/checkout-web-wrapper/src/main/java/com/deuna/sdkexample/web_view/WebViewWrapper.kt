package com.deuna.sdkexample.web_view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.deuna.sdkexample.R

open class WebViewWrapper(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {


    val webView: WebView
        get() = controller.webView

    var loader: ProgressBar

    private var controller: WebViewController

    var javascriptMessageListener: (javascriptMessage: JavascriptMessage) -> Unit = {}

    init {
        inflate(context, R.layout.webview_layout, this)
        val webView = findViewById<WebView>(R.id.web_view)
        controller = WebViewController(context, webView)
        loader = findViewById(R.id.loader)
        controller.listener = object : WebViewController.Listener {
            override fun onWebViewLoaded() {
                loader.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }

            override fun onWebViewError() {
                loader.visibility = View.GONE
                // show an Error here
            }

            override fun onOpenExternalUrl(url: String) {
                ExternalUrlHelper.openUrl(url)
            }

            override fun onJavascriptMessage(message: JavascriptMessage) {
                javascriptMessageListener(message)
            }
        }
    }


    fun loadUrl(url: String) {
        controller.loadUrl(url)
    }

    fun dispose() {
        webView.stopLoading()
        webView.clearHistory()
        webView.clearCache(true)
        webView.destroy()
    }

}