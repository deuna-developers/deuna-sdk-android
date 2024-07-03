package com.deuna.maven.web_views.base

import android.annotation.*
import android.app.Activity
import android.content.*
import android.net.*
import android.os.*
import android.view.*
import android.webkit.*
import android.widget.*
import com.deuna.maven.R
import com.deuna.maven.shared.*

/**
 * This abstract class provides a foundation for activities that display web content
 * using a WebView. It handles common tasks like checking internet connectivity,
 * registering broadcast receivers, handling back button presses, and loading URLs.
 */
abstract class BaseWebViewActivity : Activity() {

    companion object {
        const val EXTRA_CLOSE_EVENTS = "CLOSE_EVENTS"

        @SuppressLint("StaticFieldLeak")
        var activity: Activity? = null

        fun closeWebView() {
            activity?.finish()
        }
    }

    lateinit var loader: ProgressBar
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        activity = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_activity)
        initialize()
    }

    // Check internet connection and initialize other components
    private fun initialize() {
        if (!NetworkUtils(this).hasInternet) {
            DeunaLogs.debug("No internet connection")
            onNoInternet()
            return
        }

        loader = findViewById(R.id.deuna_loader)
        webView = findViewById(R.id.deuna_webview)

    }

    // Handle back button press
    override fun onBackPressed() {
        DeunaLogs.debug("Canceled by user")
        onCanceledByUser()
        super.onBackPressed()
    }

    // Load the URL in the WebView
    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String) {
        val cleanedUrl = cleanUrl(url)
        DeunaLogs.info(cleanedUrl)
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }

        // Add JavascriptInterface
        webView.addJavascriptInterface(getBridge(), "android")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // When the page finishes loading, the Web View is shown and the loader is hidden
                view?.visibility = View.VISIBLE
                loader.visibility = View.GONE
            }
        }
        setupWebChromeClient(webView)

        webView.loadUrl(cleanedUrl)
    }

    // Setup the WebChromeClient to handle creation of new windows.
    private fun setupWebChromeClient(webView: WebView) {
        webView.webChromeClient = object : WebChromeClient() {
            @SuppressLint("SetJavaScriptEnabled")
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message,
            ): Boolean {
                val newWebView = WebView(this@BaseWebViewActivity).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                }

                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                // Custom WebViewClient to handle external URLs and loading URLs in a new WebView or the current WebView.
                val webViewClient = CustomWebViewClient(webViewCallback, newWebView)
                newWebView.webViewClient = webViewClient

                return true
            }
        }
    }


    // Handle a URL that should be opened in an external browser.
    val webViewCallback = object : WebViewCallback {
        override fun onExternalUrl(webView: WebView, url: String) {
            openInExternalBrowser(url)
        }

        override fun onLoadUrl(webView: WebView, newWebView: WebView, url: String) {

            webView.loadUrl(url)

            newWebView.webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message,
                ): Boolean {
                    return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                }
            }

            // The new WebView should be added and visible
            val layout = findViewById<RelativeLayout>(R.id.deuna_webview_container)
            layout.removeView(webView)
            layout.addView(newWebView)
            newWebView.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )

            newWebView.visibility = View.VISIBLE
        }
    }

    // Open the URL in an external browser.
    private fun openInExternalBrowser(url: String) {
        // Create an Intent to open the URL in an external browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }


    // Remove unnecessary slashes from the URL
    private fun cleanUrl(url: String): String {
        val protocolEndIndex = url.indexOf("//") + 2
        val protocol = url.substring(0, protocolEndIndex)
        val restOfUrl = url.substring(protocolEndIndex).replace("//", "/")
        return "$protocol$restOfUrl"
    }

    // Abstract methods to be implemented by subclasses
    abstract fun getBridge(): WebViewBridge

    abstract fun onNoInternet()

    abstract fun onCanceledByUser()

    // Unregister the broadcast receiver when the activity is destroyed
    override fun onDestroy() {
        activity = null
        super.onDestroy()
    }
}