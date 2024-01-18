package com.deuna.maven.checkout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.R
import com.deuna.maven.checkout.domain.DeUnaBridge
import com.deuna.maven.checkout.domain.DeunaErrorMessage
import com.deuna.maven.client.sendOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL


// Activity for Deuna.
class DeunaActivity : AppCompatActivity() {
    lateinit var instance: DeunaActivity
    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val ORDER_TOKEN = "order_token"
        const val API_KEY = "api_key"
        const val LOGGING_ENABLED = "logging_enabled"
        const val BASE_URL = "BASE_URL"
        const val CLOSE_ON_EVENTS = ""
        var callbacks: Callbacks? = null

        fun setCallback(callback: Callbacks?) {
            this.callbacks = callback
        }
    }

    private val closeAllReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    // Called when the activity is starting
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deuna)
        instance = this
        showProgressBar(true)

        scope.launch {
            getOrderApi(
                intent.getStringExtra(BASE_URL)!!,
                intent.getStringExtra(ORDER_TOKEN)!!,
                intent.getStringExtra(API_KEY)!!,
                intent.getStringArrayListExtra(CLOSE_ON_EVENTS)
            )
        }

        registerReceiver(closeAllReceiver, IntentFilter("com.deuna.maven.CLOSE_CHECKOUT"))
    }

    // Called when the activity is destroyed.
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(closeAllReceiver)
    }

    // Render the checkout in a WebView.
    private fun launchActivity(url: String, closeOnEvents: ArrayList<String>? = null) {
        val webView: WebView = findViewById(R.id.deuna_webview)
        setupWebView(webView, url, closeOnEvents)
        loadUrlWithNetworkCheck(webView, this, url)
    }

    // Setup the WebView with necessary settings and JavascriptInterface.
    private fun setupWebView(webView: WebView, url: String, closeOnEvents: ArrayList<String>? = null) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }
        webView.addJavascriptInterface(DeUnaBridge(this, callbacks!!, closeOnEvents), "android") // Add JavascriptInterface

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // When the page finishes loading, the Web View is shown and the loader is hidden
                view?.visibility = View.VISIBLE
                showProgressBar(false)
            }
        }

        setupWebChromeClient(webView, url)

    }

    // Setup the WebChromeClient to handle creation of new windows.
    private fun setupWebChromeClient(webView: WebView, targetUrl: String) {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
                val newWebView = WebView(this@DeunaActivity).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                }

                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                // Custom WebViewClient to handle external URLs and loading URLs in a new WebView or the current WebView.
                val webViewClient = CustomWebViewClient(webviewCallback, newWebView)
                newWebView.webViewClient = webViewClient

                return true
            }
        }
    }

    // Clean the URL to avoid double slashes.
    fun cleanUrl(url: String): String {
        val protocolEndIndex = url.indexOf("//") + 2
        val protocol = url.substring(0, protocolEndIndex)
        val restOfUrl = url.substring(protocolEndIndex).replace("//", "/")
        return "$protocol$restOfUrl"
    }

    // Send the order to the API and launch the checkout.
    private fun getOrderApi(baseUrl: String, orderToken: String, apiKey: String, closeOnEvents: ArrayList<String>? = null) {
        sendOrder(baseUrl, orderToken, apiKey, object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val responseBody = response.body() as? Map<*, *>
                    val orderMap = responseBody?.get("order") as? Map<*, *>
                    if (orderMap != null) {
                        val parsedUrl = URL(orderMap.get("payment_link").toString())
                        launchActivity(cleanUrl(parsedUrl.toString()), closeOnEvents)
                    }
                } else {
                    Toast.makeText(
                        this@DeunaActivity,
                        "Error al obtener la orden",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(
                    this@DeunaActivity,
                    "Error al obtener la orden",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    // Load a URL if there is an active internet connection.
    private fun loadUrlWithNetworkCheck(view: WebView, context: Context, url: String) {
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
            callbacks?.onError?.invoke(
                DeunaErrorMessage(
                    "No internet connection",
                    "",
                    null,
                    null
                )
            )
        }
    }

    // Log a message if logging is enabled.
    private fun log(message: String) {
        val loggingEnabled = intent.getBooleanExtra(LOGGING_ENABLED, false)
        if (loggingEnabled) {
            Log.d("[DeunaSDK]: ", message)
        }
    }

    // Custom WebViewClient to handle external URLs and loading URLs in a new WebView or the current WebView.
    class CustomWebViewClient(private val callback: WebViewCallback, private val newWebView: WebView) : WebViewClient() {

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

    // Interface for handling external URLs and loading URLs in a new WebView.
    interface WebViewCallback {
        fun onExternalUrl(webView: WebView, url: String)
        fun onLoadUrl(webView: WebView, newWebView: WebView, url: String)
    }

    // Handle a URL that should be opened in an external browser.
    val webviewCallback = object : WebViewCallback {
        override fun onExternalUrl(webView: WebView, url: String) {
            Log.d("WebViewCallback", "External URL: $url")
            openInExternalBrowser(url)
        }

        override fun onLoadUrl(webView: WebView, newWebView: WebView, url: String) {
            Log.d("WebViewCallback", "Load URL: $url")
            webView.loadUrl(url)

            newWebView.webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
                    return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                }
            }


            // Hide the current WebView
            webView.visibility = View.GONE

            // The new WebView should be added and visible
            val layout = findViewById<RelativeLayout>(R.id.deuna_layout)
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

    // Show or Hide progress bar (loader)
    private fun showProgressBar(show: Boolean) {
        val loader: ProgressBar = findViewById(R.id.loader)
        val layout: RelativeLayout = findViewById(R.id.progressLayout)

        loader.visibility = if (show) View.VISIBLE else View.INVISIBLE
        layout.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }
}