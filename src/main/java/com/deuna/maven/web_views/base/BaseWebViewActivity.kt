package com.deuna.maven.web_views.base

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Message
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.deuna.maven.R
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.web_views.file_downloaders.*

abstract class BaseWebViewActivity : Activity() {


    private var pageLoaded = false
    lateinit var loader: ProgressBar
    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_activity)
        loader = findViewById(R.id.deuna_loader)
        webView = findViewById(R.id.deuna_webview)
    }


    @SuppressLint("SetJavaScriptEnabled")
    open fun loadUrl(url: String, javascriptToInject: String? = null) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }

        webView.addJavascriptInterface(LocalBridge(), "local")

        /// Client to listen errors and content loaded
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pageLoaded = true

                val js = """
                     window.open = function(url, target, features) {
                         local.openInNewTab(url);
                     };
                """.trimIndent()
                webView.evaluateJavascript(js, null)

                if (javascriptToInject != null) {
                    webView.evaluateJavascript(javascriptToInject, null)
                }

                // When the page finishes loading, the Web View is shown and the loader is hidden
                view?.visibility = View.VISIBLE
                loader.visibility = View.GONE

                onWebViewLoaded()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                // ignore errors when the page is already loaded
                if (pageLoaded) {
                    return
                }
                if (error != null) {
                    onWebViewError()
                }
            }
        }


        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?,
            ): Boolean {
                if (!isDialog) {
                    val newWebView = WebView(this@BaseWebViewActivity).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                    }

                    val transport = resultMsg?.obj as WebView.WebViewTransport
                    transport.webView = newWebView
                    resultMsg.sendToTarget()

                    // Custom WebViewClient to handle external URLs and loading URLs in a new WebView
                    // for example when a link is clicked
                    val webViewClient = CustomWebViewClient(object : WebViewCallback {
                        override fun onExternalUrl(webView: WebView, url: String) {
                            if (url.isFileDownloadUrl) {
                                onDownloadFile(url)
                                return
                            }
                            onOpenInNewTab(url)
                        }

                        override fun onLoadUrl(webView: WebView, newWebView: WebView, url: String) {
                            if (url.isFileDownloadUrl) {
                                onDownloadFile(url)
                                return
                            }
                            onOpenInNewTab(url)
                        }
                    }, newWebView)
                    newWebView.webViewClient = webViewClient
                }
                return true
            }
        }

        val cleanedUrl = cleanUrl(url)
        DeunaLogs.info("Loading url: $cleanedUrl")
        webView.loadUrl(cleanedUrl)
    }


    // Remove unnecessary slashes from the URL
    private fun cleanUrl(url: String): String {
        val protocolEndIndex = url.indexOf("//") + 2
        val protocol = url.substring(0, protocolEndIndex)
        val restOfUrl = url.substring(protocolEndIndex).replace("//", "/")
        return "$protocol$restOfUrl"
    }


    /**
     * Intercepts window.open and launches a new activity with a new web view
     */
    inner class LocalBridge() {
        @JavascriptInterface
        fun openInNewTab(url: String) {
            if (url.isFileDownloadUrl) {
                onDownloadFile(url)
                return
            }
            onOpenInNewTab(url)
        }
    }


    abstract fun onWebViewLoaded()
    abstract fun onWebViewError()
    abstract fun onOpenInNewTab(url: String)
    abstract fun onDownloadFile(url: String)
}


