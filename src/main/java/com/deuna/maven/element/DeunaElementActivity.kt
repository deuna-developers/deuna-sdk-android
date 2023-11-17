package com.deuna.maven.element

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.deuna.maven.R
import com.deuna.maven.checkout.Callbacks
import com.deuna.maven.checkout.DeunaActivity
import com.deuna.maven.element.domain.DeUnaElementBridge
import com.deuna.maven.element.domain.ElementCallbacks

class DeunaElementActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val LOGGING_ENABLED = "logging_enabled"
        var callbacks: ElementCallbacks? = null
        fun setCallback(callback: ElementCallbacks?) {
            this.callbacks = callback
        }
    }

    private val closeAllReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(closeAllReceiver)
    }

    /**
     * Called when the activity is starting.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deuna_element)
        val url = intent.getStringExtra(EXTRA_URL)
        val webView: WebView = findViewById(R.id.deuna_webview_element)
        setupWebView(webView)
        if (url != null) {
            loadUrlWithNetworkCheck(webView, this, url)
            registerReceiver(closeAllReceiver, IntentFilter("com.deuna.maven.CLOSE_ALL"))
        }
    }

    /**
     * Setup the WebView with necessary settings and JavascriptInterface.
     */
    private fun setupWebView(webView: WebView) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }
        webView.addJavascriptInterface(DeUnaElementBridge(callbacks!!, this), "android") // Add JavascriptInterface
        setupWebChromeClient(webView)

    }

    /**
     * Setup the WebChromeClient to handle creation of new windows.
     */
    private fun setupWebChromeClient(webView: WebView) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
//                setVisibilityProgressBar(false)
                webView.visibility = View.VISIBLE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
//                setVisibilityProgressBar(true)
            }
        }
    }

    /**
     * Load a URL if there is an active internet connection.
     */
    private fun loadUrlWithNetworkCheck(
        view: WebView,
        context: Context,
        url: String
    ) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if ((networkCapabilities != null) && networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        ) {
            view.loadUrl(url)
        } else {
            log("No internet connection")
        }
    }

    private fun setVisibilityProgressBar(isVisible: Boolean) {
        val progressBar: ProgressBar = findViewById(R.id.progress_circular_element)
        progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
//        webView.visibility = View.VISIBLE
    }

    /**
     * Log a message if logging is enabled.
     */
    private fun log(message: String) {
        val loggingEnabled = intent.getBooleanExtra(DeunaActivity.LOGGING_ENABLED, false)
        if (loggingEnabled) {
            Log.d("[DeunaSDK]: ", message)
        }
    }
}