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
        const val EXTRA_SDK_INSTANCE_ID = "SDK_INSTANCE_ID"

        /**
         * Due to multiples instances of DeunaSDK can be created
         * we need to ensure that only the authorized instance can
         * close the widgets and call the callbacks
         */
        var activities = mutableMapOf<Int, BaseWebViewActivity>()

        fun closeWebView(sdkInstanceId: Int) {
            activities[sdkInstanceId]?.finish()
        }

        /// send the custom style to the payment link
        fun sendCustomStyle(sdkInstanceId: Int, dataAsJsonString: String) {
            val activity = activities[sdkInstanceId] ?: return
            activity.runOnUiThread {
                activity.webView.evaluateJavascript(
                    "setCustomStyle($dataAsJsonString);",
                    null
                );
            }
        }
    }

    lateinit var loader: ProgressBar
    lateinit var webView: WebView
    var sdkInstanceId: Int? = null

    /// When this var is false the close feature is disabled
    private var closeEnabled = true

    private var pageLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdkInstanceId = intent.getIntExtra(EXTRA_SDK_INSTANCE_ID, 0)
        activities[sdkInstanceId!!] = this
        setContentView(R.layout.webview_activity)
        initialize()
    }

    // Check internet connection and initialize other components
    private fun initialize() {
        if (!NetworkUtils(this).hasInternet) {
            onNoInternet()
            return
        }

        loader = findViewById(R.id.deuna_loader)
        webView = findViewById(R.id.deuna_webview)

    }

    // Handle back button press
    override fun onBackPressed() {
        // Disable the back button when a payment is processing
        if (!closeEnabled) {
            return
        }
        onCanceledByUser()
        super.onBackPressed()
    }

    // Load the URL in the WebView
    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String, javascriptToInject: String? = null) {
        val cleanedUrl = cleanUrl(url)
        DeunaLogs.info(cleanedUrl)

        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }

        // Add JavascriptInterface
        val bridge = getBridge()
        webView.addJavascriptInterface(bridge, bridge.name)
        webView.addJavascriptInterface(LocalBridge(),"local")

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pageLoaded = true

                webView.evaluateJavascript(
                    """
                        console.log = function(message) {
                           android.consoleLog(message);
                        };
                        
                        window.open = function(url, target, features) {
                           local.openInNewTab(url);
                        };
                        
                        window.xprops = {
                            onEventDispatch : function (event) {
                                android.postMessage(JSON.stringify(event));
                            },
                            onCustomCssSubscribe: function (setCustomCSS)  {
                                window.setCustomCss = setCustomCSS;
                            },
                            onCustomStyleSubscribe: function (setCustomStyle)  {
                                window.setCustomStyle = setCustomStyle;
                            },
                            onRefetchOrderSubscribe: function (refetchOrder) {
                                window.deunaRefetchOrder = refetchOrder;
                            },
                        };
                """.trimIndent(), null
                )

                if (javascriptToInject != null) {
                    webView.evaluateJavascript(javascriptToInject, null)
                }

                // When the page finishes loading, the Web View is shown and the loader is hidden
                view?.visibility = View.VISIBLE
                loader.visibility = View.GONE
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
                    onError()
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
                            openExternalUrl(url)
                        }

                        override fun onLoadUrl(webView: WebView, newWebView: WebView, url: String) {
                            openExternalUrl(url)
                        }
                    }, newWebView)
                    newWebView.webViewClient = webViewClient
                }
                return true
            }
        }
        webView.loadUrl(cleanedUrl)
    }


    /// Open the url in a new web view, for example for 3Ds auth
    private fun openExternalUrl(url: String) {
        val intent = Intent(this, SubWebViewActivity::class.java).apply {
            putExtra(SubWebViewActivity.EXTRA_URL, url)
            putExtra(SubWebViewActivity.EXTRA_SDK_INSTANCE_ID, sdkInstanceId)
        }
        startActivity(intent)
    }

    /// Closes the sub web view
    fun closeSubWebView() {
        SubWebViewActivity.closeWebView(sdkInstanceId!!)
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

    abstract fun onError()

    abstract fun onCanceledByUser()

    /**
     * Configures whether the widget close action is enabled or disabled,
     * Useful for automatic redirects like 3Ds
     */
    fun updateCloseEnabled(enabled: Boolean) {
        closeEnabled = enabled
    }

    // Unregister the broadcast receiver when the activity is destroyed
    override fun onDestroy() {
        closeSubWebView()
        activities.remove(sdkInstanceId!!)
        super.onDestroy()
    }

    /**
     * Intercepts window.open and launches a new activity with a new web view
     */
    inner class LocalBridge() {
        @JavascriptInterface
        fun openInNewTab(url: String) {
            openExternalUrl(url)
        }
    }
}