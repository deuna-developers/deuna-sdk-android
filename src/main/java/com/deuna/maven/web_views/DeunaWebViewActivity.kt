package com.deuna.maven.web_views

import android.annotation.*
import android.content.*
import android.os.*
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.base.BaseWebViewActivity
import com.deuna.maven.web_views.file_downloaders.*

/**
 * This abstract class provides a foundation for activities that display web content
 * using a WebView. It handles common tasks like checking internet connectivity,
 * registering broadcast receivers, handling back button presses, and loading URLs.
 */
abstract class DeunaWebViewActivity : BaseWebViewActivity() {

    companion object {
        const val EXTRA_CLOSE_EVENTS = "CLOSE_EVENTS"
        const val EXTRA_SDK_INSTANCE_ID = "SDK_INSTANCE_ID"

        /**
         * Due to multiples instances of DeunaSDK can be created
         * we need to ensure that only the authorized instance can
         * close the widgets and call the callbacks
         */
        var activities = mutableMapOf<Int, DeunaWebViewActivity>()

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

    var sdkInstanceId: Int? = null

    private var externalUrl: String? = null

    /// When this var is false the close feature is disabled
    private var closeEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdkInstanceId = intent.getIntExtra(EXTRA_SDK_INSTANCE_ID, 0)
        activities[sdkInstanceId!!] = this
        initialize()
    }

    // Check internet connection and initialize other components
    private fun initialize() {
        if (!NetworkUtils(this).hasInternet) {
            onNoInternet()
            return
        }
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
    override fun loadUrl(url: String, javascriptToInject: String?) {

        // Add JavascriptInterface
        val bridge = getBridge()
        webView.addJavascriptInterface(bridge, bridge.name)

        super.loadUrl(
            url, """
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
            ${javascriptToInject ?: ""}
        """.trimIndent()
        )

    }

    /// Closes the sub web view
    fun closeSubWebView() {
        externalUrl = null
        NewTabWebViewActivity.closeWebView(sdkInstanceId!!)
    }

    // Abstract methods to be implemented by subclasses
    abstract fun getBridge(): WebViewBridge

    abstract fun onNoInternet()

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

    override fun onDownloadFile(url: String) {
        downloadFile(url)
    }

    override fun onWebViewLoaded() {}

    override fun onOpenInNewTab(url: String) {
        if (externalUrl != null) {
            return
        }
        externalUrl = url
        val intent = Intent(this, NewTabWebViewActivity::class.java).apply {
            putExtra(NewTabWebViewActivity.EXTRA_URL, url)
            putExtra(NewTabWebViewActivity.EXTRA_SDK_INSTANCE_ID, sdkInstanceId)
        }
        startActivityForResult(intent, NewTabWebViewActivity.SUB_WEB_VIEW_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != NewTabWebViewActivity.SUB_WEB_VIEW_REQUEST_CODE) {
            return
        }
        if (externalUrl != null) {
            webView.evaluateJavascript(
                """
                if(!window.onEmbedEvent){
                   console.log('window.onEmbedEvent is not defined');
                } else {
                   window.onEmbedEvent('${OnEmbedEvents.APM_CLOSED}');
                }
            """.trimIndent(), null
            );
        }
        externalUrl = null
    }
}