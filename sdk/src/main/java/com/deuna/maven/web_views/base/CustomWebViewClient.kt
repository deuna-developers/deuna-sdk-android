package com.deuna.maven.web_views.base

import android.webkit.*


// Interface for handling external URLs and loading URLs in a new WebView.
interface WebViewCallback {
    fun onExternalUrl(webView: WebView, url: String)
    fun onLoadUrl(webView: WebView, newWebView: WebView, url: String)
}

// Custom WebViewClient to handle external URLs and loading URLs in a new WebView or the current WebView.
class CustomWebViewClient(private val callback: WebViewCallback, private val newWebView: WebView) :
    WebViewClient() {

    // Keywords to recognize a url that should be opened externally in a browser.
    private val keysForExternalUrls = arrayOf("vapormicuenta")

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val newUrl = request?.url.toString()

        // Check if the url contains the declared keywords, if so, open the url in Browser and not in the app's webview
        if (keysForExternalUrls.any { newUrl.contains(it) }) {
            callback.onExternalUrl(view!!, newUrl)
            return true
        }

        callback.onLoadUrl(view!!, newWebView, newUrl)
        return true

    }
}
