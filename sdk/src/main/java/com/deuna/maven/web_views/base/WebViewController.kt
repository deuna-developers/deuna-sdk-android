package com.deuna.maven.web_views.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Message
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toMap
import com.deuna.maven.web_views.file_downloaders.isFileDownloadUrl
import com.deuna.maven.web_views.file_downloaders.runOnUiThread
import org.json.JSONObject

class WebViewController(
    val context: Context,
    val webView: WebView
) {
    var listener: Listener? = null
    var pageLoaded = false


    private val remoteFunctionsRequests = mutableMapOf<Int, (Json) -> Unit>()
    private var remoteFunctionsRequestId = 0


    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String, jsToInjectCallback: (() -> String)? = null) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }

        webView.addJavascriptInterface(LocalBridge(), "local")
        webView.addJavascriptInterface(RemoteJsFunctionBridge(), "remoteJs")

        /// Client to listen errors and content loaded
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pageLoaded = true

                val js = """
                     window.open = function(url, target, features) {
                         local.openExternalUrl(url);
                     };
                """.trimIndent()
                webView.evaluateJavascript(js, null)

                if (jsToInjectCallback != null) {
                    webView.evaluateJavascript(jsToInjectCallback(), null)
                }
                listener?.onWebViewLoaded()
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
                    listener?.onWebViewError()
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
                    val newWebView = WebView(context).apply {
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
                                listener?.onDownloadFile(url)
                                return
                            }
                            listener?.onOpenExternalUrl(url)
                        }

                        override fun onLoadUrl(webView: WebView, newWebView: WebView, url: String) {
                            if (url.isFileDownloadUrl) {
                                listener?.onDownloadFile(url)
                                return
                            }
                            listener?.onOpenExternalUrl(url)
                        }
                    }, newWebView)
                    newWebView.webViewClient = webViewClient
                }
                return true
            }
        }

        DeunaLogs.info("Loading url: $url")
        webView.loadUrl(url)
    }

    interface Listener {
        fun onWebViewLoaded()
        fun onWebViewError()
        fun onOpenExternalUrl(url: String)
        fun onDownloadFile(url: String)
    }


    /**
     * Intercepts window.open and launches a new activity with a new web view
     */
    inner class LocalBridge() {
        @JavascriptInterface
        fun openExternalUrl(url: String) {
            if (url.isFileDownloadUrl) {
                listener?.onDownloadFile(url)
                return
            }
            listener?.onOpenExternalUrl(url)
        }
    }


    /**
     * Build and execute a remote JS function
     */
    fun executeRemoteFunction(
        jsBuilder: (requestId: Int) -> String, callback: (Json) -> Unit
    ) {
        runOnUiThread {
            remoteFunctionsRequestId++
            remoteFunctionsRequests[remoteFunctionsRequestId] = callback
            webView.evaluateJavascript(jsBuilder(remoteFunctionsRequestId), null)
        }
    }


    /**
     * Js Bridge to listen the remote functions responses
     */
    inner class RemoteJsFunctionBridge {
        @JavascriptInterface
        fun onRequestResult(message: String) {
            runOnUiThread {
                try {
                    val json = JSONObject(message).toMap()
                    val requestId = json["requestId"] as? Int
                    if (!remoteFunctionsRequests.contains(requestId)) {
                        return@runOnUiThread
                    }

                    remoteFunctionsRequests[requestId]?.invoke(
                        json["data"] as Json
                    )
                    remoteFunctionsRequests.remove(requestId)
                } catch (e: Exception) {
                    DeunaLogs.error("RemoteJsFunctionBridge error: $e")
                }
            }
        }
    }


    fun destroy() {
        // Remove the WebView from the view hierarchy
        (webView.parent as? ViewGroup)?.removeView(webView)
        // Stop loading and clear cache
        webView.stopLoading()
        webView.clearHistory()
        webView.clearCache(true)

        // Destroy the WebView
        webView.destroy()
    }
}