package com.deuna.maven.web_views.base

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.deuna.maven.R
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toMap
import com.deuna.maven.web_views.file_downloaders.runOnUiThread
import org.json.JSONObject


open class BaseWebView(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {


    val webView: WebView
        get() = controller!!.webView

    var loader: ProgressBar
    var listener: Listener? = null


    var controller: WebViewController? = null


    init {
        inflate(context, R.layout.embedded_webview, this)
        val webView = findViewById<WebView>(R.id.embedded_web_view)
        loader = findViewById<ProgressBar>(R.id.embedded_loader)

        controller = WebViewController(context, webView)
        controller?.listener = object : WebViewController.Listener {
            override fun onWebViewLoaded() {
                webView.visibility = View.VISIBLE
                loader.visibility = View.GONE
                listener?.onWebViewLoaded()
            }

            override fun onWebViewError() {
                listener?.onWebViewError()
            }

            override fun onOpenExternalUrl(url: String) {
                listener?.onOpenExternalUrl(url)
            }

            override fun onDownloadFile(url: String) {
                listener?.onDownloadFile(url)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String, jsToInjectCallback: (() -> String)? = null) {
        controller?.loadUrl(url, jsToInjectCallback)
    }


    interface Listener {
        fun onWebViewLoaded()
        fun onWebViewError()
        fun onOpenExternalUrl(url: String)
        fun onDownloadFile(url: String)
    }

    /**
     * Removes the WebView from the view hierarchy and destroys it
     */
    open fun destroy() {
        controller?.destroy()
    }

}