package com.deuna.maven.checkout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.R
import com.deuna.maven.checkout.domain.DeUnaBridge
import com.deuna.maven.checkout.domain.DeunaErrorMessage
import com.deuna.maven.client.sendOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

/**
 * Activity for Deuna.
 */
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

    /**
     * Called when the activity is starting.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deuna)
        instance = this
        setProgressBarVisibilityBar(true)


        scope.launch {
            delay(3000L)
            getOrderApi(
                intent.getStringExtra(BASE_URL)!!,
                intent.getStringExtra(ORDER_TOKEN)!!,
                intent.getStringExtra(API_KEY)!!,
                intent.getStringArrayListExtra(CLOSE_ON_EVENTS)
            )
            setProgressBarVisibilityBar(false)
        }

        registerReceiver(closeAllReceiver, IntentFilter("com.deuna.maven.CLOSE_CHECKOUT"))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(closeAllReceiver)
    }

    private fun launchActivity(url: String, closeOnEvents: ArrayList<String>? = null) {
        val webView: WebView = findViewById(R.id.deuna_webview)
        webView.visibility = View.VISIBLE
        setupWebView(webView, url, closeOnEvents)
        loadUrlWithNetworkCheck(webView, this, url)
    }

    /**
     * Setup the WebView with necessary settings and JavascriptInterface.
     */
    private fun setupWebView(
        webView: WebView,
        url: String,
        closeOnEvents: ArrayList<String>? = null
    ) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }
        webView.addJavascriptInterface(
            DeUnaBridge(this, callbacks!!, closeOnEvents),
            "android"
        ) // Add JavascriptInterface
        setupWebChromeClient(webView, url)

    }

    /**
     * Setup the WebChromeClient to handle creation of new windows.
     */
    private fun setupWebChromeClient(webView: WebView, targetUrl: String) {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message
            ): Boolean {
                val newWebView = WebView(this@DeunaActivity).apply {
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
                        // Aquí puedes agregar la misma lógica para manejar nuevas ventanas
                        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                    }
                }

                // Oculta el WebView existente
                webView.visibility = View.GONE

                // Agrega el nuevo WebView a tu layout y lo hace visible
                val layout =
                    findViewById<RelativeLayout>(R.id.deuna_layout) // Reemplaza 'your_layout_id' con el ID de tu RelativeLayout
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

    fun cleanUrl(url: String): String {
        val protocolEndIndex = url.indexOf("//") + 2
        val protocol = url.substring(0, protocolEndIndex)
        val restOfUrl = url.substring(protocolEndIndex).replace("//", "/")
        return "$protocol$restOfUrl"
    }

    private fun getOrderApi(
        baseUrl: String,
        orderToken: String,
        apiKey: String,
        closeOnEvents: ArrayList<String>? = null
    ) {

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


    private fun setVisibilityProgressBar(isVisible: Boolean) {
        val progressBar: ProgressBar = findViewById(R.id.progress_circular)
        progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
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

    fun setProgressBarVisibilityBar(visible: Boolean) {
        val progressBar: ProgressBar = findViewById(R.id.progress_circular)
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Log a message if logging is enabled.
     */
    private fun log(message: String) {
        val loggingEnabled = intent.getBooleanExtra(LOGGING_ENABLED, false)
        if (loggingEnabled) {
            Log.d("[DeunaSDK]: ", message)
        }
    }
}