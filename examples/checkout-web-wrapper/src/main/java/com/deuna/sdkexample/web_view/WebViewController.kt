package com.deuna.sdkexample.web_view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Message
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.deuna.sdkexample.extensions.toMap
import org.json.JSONObject

class WebViewController(
    private val context: Context,
    val webView: WebView,
) {

    var loaded = false
        private set

    interface Listener {
        fun onWebViewLoaded()
        fun onWebViewError()
        fun onOpenExternalUrl(url: String)
        fun onJavascriptMessage(message: JavascriptMessage)
    }

    var listener: Listener? = null


    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
        }

        webView.addJavascriptInterface(Bridge(), "deunaPayment")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.url?.toString()?.let { url ->
                    // Open url in Custom Chrome Tab
                    listener?.onOpenExternalUrl(url)
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (loaded) {
                    return
                }
                listener?.onWebViewLoaded()
                loaded = true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                listener?.onWebViewError()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport
                val newWebView = WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            listener?.onOpenExternalUrl(request.url.toString())
                            return true
                        }
                    }
                }

                transport?.webView = newWebView
                resultMsg?.sendToTarget()
                return true
            }
        }

        webView.loadUrl(url)
    }


    inner class Bridge {
        @JavascriptInterface
        fun postMessage(jsonString: String) {
            Log.d("WebViewBridge", "postMessage: $jsonString")
            try {
                val json = JSONObject(jsonString).toMap()
                val callbackName = json["callbackName"] as? String ?: return
                val widgetType = json["widgetType"] as? String ?: return
                val payload = json["data"] as? Json ?: return

                when (callbackName){
                    "onSuccess" -> {
                        listener?.onJavascriptMessage(
                            JavascriptMessage.OnSuccess(payload, widgetType)
                        )
                    }
                    "onError" -> {
                        listener?.onJavascriptMessage(
                            JavascriptMessage.OnError(payload, widgetType)
                        )
                    }
                    "onEventDispatch" -> {
                        listener?.onJavascriptMessage(
                            JavascriptMessage.OnEventDispatch(payload, widgetType)
                        )
                    }
                }


            } catch (e: Exception) {
                Log.e("WebViewBridge", "postMessage: $e")
            }
        }
    }
}