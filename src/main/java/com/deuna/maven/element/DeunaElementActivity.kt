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
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.R
import com.deuna.maven.checkout.DeunaActivity
import com.deuna.maven.element.domain.DeUnaElementBridge
import com.deuna.maven.element.domain.ElementCallbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeunaElementActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val EXTRA_URL = "extra_url"
        const val LOGGING_ENABLED = "logging_enabled"
        const val CLOSE_ON_EVENTS = ""
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

        setProgressBarVisibilityBar(true)

        val url = intent.getStringExtra(EXTRA_URL)
        val webView: WebView = findViewById(R.id.deuna_webview_element)

        scope.launch {
            setupWebView(webView,  intent.getStringArrayListExtra(DeunaActivity.CLOSE_ON_EVENTS))
            if (url != null) {
                webView.visibility = View.VISIBLE
                setProgressBarVisibilityBar(false)
                loadUrlWithNetworkCheck(webView, this@DeunaElementActivity, url)
                registerReceiver(closeAllReceiver, IntentFilter("com.deuna.maven.CLOSE_ELEMENTS"))
            }
        }

    }

    private fun setProgressBarVisibilityBar(visible: Boolean) {
        val progressBar: ProgressBar = findViewById(R.id.progress_circular_element)
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Setup the WebView with necessary settings and JavascriptInterface.
     */
    private fun setupWebView(webView: WebView, closeOnEvents: ArrayList<String>? = null) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }
        webView.addJavascriptInterface(DeUnaElementBridge(callbacks!!, this, closeOnEvents), "android") // Add JavascriptInterface
        setupWebChromeClient(webView)

    }

    /**
     * Setup the WebChromeClient to handle creation of new windows.
     */
    private fun setupWebChromeClient(webView: WebView) {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message
            ): Boolean {
                val newWebView = WebView(this@DeunaElementActivity).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                }

                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val newUrl = request?.url.toString()
                        view?.loadUrl(newUrl)
                        return true
                    }
                }

                newWebView.webChromeClient = object : WebChromeClient() {
                    override fun onCreateWindow(
                        view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message
                    ): Boolean {
                        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                    }
                }

                webView.visibility = View.GONE

                val layout =
                    findViewById<RelativeLayout>(R.id.deuna_layout_element) // Reemplaza 'your_layout_id' con el ID de tu RelativeLayout
                layout.addView(newWebView)
                newWebView.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
                newWebView.visibility = View.VISIBLE

                return true
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
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if ((networkCapabilities != null) && networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        ) {
            view.loadUrl(url)
        } else {
            log("No internet connection")
        }
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