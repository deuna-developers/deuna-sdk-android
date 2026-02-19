package com.deuna.maven.web_views.deuna

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.activity.result.contract.ActivityResultContracts
import com.deuna.maven.DeunaSDK
import com.deuna.maven.generateFraudId
import com.deuna.maven.widgets.checkout_widget.CheckoutBridge
import com.deuna.maven.widgets.elements_widget.ElementsBridge
import com.deuna.maven.widgets.payment_widget.PaymentWidgetBridge
import com.deuna.maven.shared.DeunaBridge
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.ElementsErrors
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.NetworkUtils
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.shared.extensions.findFragmentActivity
import com.deuna.maven.web_views.ExternalUrlBrowser
import com.deuna.maven.web_views.ExternalUrlHelper
import com.deuna.maven.web_views.base.BaseWebView
import com.deuna.maven.web_views.deuna.extensions.buildBridge
import com.deuna.maven.web_views.deuna.extensions.getExternalUrlBrowser
import com.deuna.maven.web_views.file_downloaders.TakeSnapshotBridge
import com.deuna.maven.web_views.file_downloaders.downloadFile
import com.deuna.maven.web_views.file_downloaders.runOnUiThread
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import org.json.JSONObject


@Suppress("UNCHECKED_CAST")
class DeunaWidget(context: Context, attrs: AttributeSet? = null) : BaseWebView(context, attrs) {

    /// When this var is false the close feature is disabled
    var closeEnabled = true

    var isWebViewLoaded = false

    val takeSnapshotBridge = TakeSnapshotBridge("paymentWidgetTakeSnapshotBridge")

    var widgetConfiguration: DeunaWidgetConfiguration? = null

    // Enum used to save what action closes the widget in modal
    var closeAction = CloseAction.systemAction

    var bridge: DeunaBridge? = null

    var deunaFraudId: String? = null

    // Stores automatic redirects already opened during this widget session
    // to avoid reopening the same browser tab after returning via deep link.
    private val openedAutomaticExternalUrls = mutableSetOf<String>()

    private var fraudCredentials: Json? = null
    private var customUserAgent: String? = null

    fun setFraudCredentials(fraudCredentials: Json?) {
        this.fraudCredentials = fraudCredentials
    }

    fun setCustomUserAgent(customUserAgent: String?) {
        this.customUserAgent = customUserAgent
    }

    fun buildSuccessPayload(payload: Json): Json {
        val enrichedPayload = payload.toMutableMap<String, Any?>()
        val userAgent = webView.settings.userAgentString
        if (!userAgent.isNullOrBlank()) {
            enrichedPayload["user_agent"] = userAgent
        }
        val fraudId = deunaFraudId
        enrichedPayload["fraud_id"] = if (fraudId.isNullOrBlank()) null else fraudId
        return enrichedPayload
    }


    // Load the URL in the WebView
    @SuppressLint("SetJavaScriptEnabled")
    fun launch(url: String, javascriptToInject: String? = null) {
        openedAutomaticExternalUrls.clear()

        fraudCredentials?.let {
            widgetConfiguration?.sdkInstance?.generateFraudId(
                context = context,
                params = it,
                callback = { fraudId ->
                    deunaFraudId = fraudId

                    if (isWebViewLoaded) {
                        webView.evaluateJavascript(
                            "window.getFraudId = function() { return '${deunaFraudId ?: ""}'; };",
                            null
                        )
                    }
                }
            )
        }


        webView.addJavascriptInterface(takeSnapshotBridge, takeSnapshotBridge.name)
        buildBridge()
        initialize()

        // Add JavascriptInterface
        bridge?.let {
            DeunaLogs.info("Adding bridge ${it.name}")
            webView.addJavascriptInterface(it, it.name)
        }
        if (!customUserAgent.isNullOrBlank()) {
            webView.settings.userAgentString = customUserAgent
        }

        fun jsToInjectCallback(): String {
            var js = """
        console.log = function(message) {
            android.consoleLog(message);
        };
         
         window.xprops = {
             hidePayButton: ${widgetConfiguration?.hidePayButton ?: false},
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
             onGetStateSubscribe: function (state){
               window.deunaWidgetState = state;
             },
             isValid: function(fn){
                window.isValid = fn;
             },
             onSubmit: function(fn){
                window.submit = fn;
             },
             getFraudId: function(){
                if(typeof window.getFraudId === 'function'){
                    return window.getFraudId();
                }
                return "";
             }
         };
            ${javascriptToInject ?: ""}
        """.trimIndent()


            if (!deunaFraudId.isNullOrEmpty()) {
                js += """
                window.getFraudId = function() {
                    return '$deunaFraudId';
                };
                """.trimIndent()
            }
            return js
        }


        super.loadUrl(
            url
        ) { return@loadUrl jsToInjectCallback() }

        listener = object : Listener {
            override fun onWebViewLoaded() {
                isWebViewLoaded = true
            }

            override fun onWebViewError() {
                bridge?.let {
                    runOnUiThread {
                        when (it) {
                            is PaymentWidgetBridge -> it.callbacks.onError?.invoke(
                                PaymentWidgetErrors.initializationFailed
                            )

                            is CheckoutBridge -> it.callbacks.onError?.invoke(PaymentWidgetErrors.initializationFailed)
                            is ElementsBridge -> it.callbacks.onError?.invoke(ElementsErrors.initializationFailed)
                            else -> {}
                        }
                    }
                }
            }

            override fun onOpenExternalUrl(url: String, userInitiated: Boolean) {
                DeunaLogs.info("Opening external url: $url, userInitiated: $userInitiated")
                runOnUiThread {
                    if (!userInitiated) {
                        if (openedAutomaticExternalUrls.contains(url)) {
                            return@runOnUiThread
                        }
                        openedAutomaticExternalUrls.add(url)
                    }

                    ExternalUrlHelper.openUrl(
                        context = this@DeunaWidget.context,
                        url = url,
                        browser = getExternalUrlBrowser(url),
                        onExternalUrlClosed = {
                            closeEnabled = true
                        }
                    )
                }
            }

            override fun onDownloadFile(url: String) {
                downloadFile(url)
            }

        }
    }

    // Check internet connection and initialize other components
    private fun initialize() {
        if (!NetworkUtils(context).hasInternet) {
            bridge?.let {
                runOnUiThread {
                    when (it) {
                        is PaymentWidgetBridge -> it.callbacks.onError?.invoke(PaymentWidgetErrors.noInternetConnection)
                        is CheckoutBridge -> it.callbacks.onError?.invoke(PaymentWidgetErrors.noInternetConnection)
                        is ElementsBridge -> it.callbacks.onError?.invoke(ElementsErrors.noInternetConnection)
                        else -> {}
                    }
                }
            }
            return
        }
    }


    /**
     * Configures whether the widget close action is enabled or disabled,
     * Useful for automatic redirects like 3Ds
     */
    fun updateCloseEnabled(enabled: Boolean) {
        closeEnabled = enabled
    }

    /// Closes the sub web view
    fun closeSubWebView() {
        ExternalUrlHelper.close()
    }

    override fun destroy() {
        closeSubWebView()
        super.destroy()
    }

    fun waitUntilExternalUrlIsClosed(callback: () -> Unit) {
        ExternalUrlHelper.waitUntilChromeTabIsClosed(callback)
    }

}
