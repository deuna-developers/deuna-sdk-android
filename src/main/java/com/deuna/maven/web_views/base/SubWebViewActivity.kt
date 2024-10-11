package com.deuna.maven.web_views.base

import android.annotation.*
import android.app.Activity
import android.os.*
import android.view.*
import android.webkit.*
import android.widget.*
import com.deuna.maven.*
import com.deuna.maven.shared.DeunaLogs
import java.util.Timer


class SubWebViewActivity : Activity() {
    companion object {
        const val EXTRA_URL = "SUB_WEB_VIEW_URL"
        const val EXTRA_SDK_INSTANCE_ID = "SDK_INSTANCE_ID"
        const val SUB_WEB_VIEW_REQUEST_CODE = 20000

        var activities = mutableMapOf<Int, SubWebViewActivity>()

        fun closeWebView(sdkInstanceId: Int) {
            activities[sdkInstanceId]?.finish()
        }
    }

    lateinit var loader: ProgressBar
    private lateinit var webView: WebView
    var sdkInstanceId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkInstanceId = intent.getIntExtra(EXTRA_SDK_INSTANCE_ID, 0)
        activities[sdkInstanceId!!] = this

        setContentView(R.layout.webview_activity)

        loader = findViewById(R.id.deuna_loader)
        webView = findViewById(R.id.deuna_webview)

        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
        }

        webView.addJavascriptInterface(LocalBridge(), "local")

        loadUrl(intent.getStringExtra(EXTRA_URL)!!)
    }

    inner class LocalBridge {
        @JavascriptInterface
        fun closeWindow() {
            DeunaLogs.info("window.close()")
            finish()
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadUrl(url: String) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.visibility = View.VISIBLE
                loader.visibility = View.GONE

                webView.evaluateJavascript(
                    """
                    window.close = function() {
                       local.closeWindow();
                    };
                """.trimIndent(), null
                )
            }
        }

        webView.loadUrl(url)
    }

    override fun onDestroy() {
        webView.destroy()
        activities.remove(sdkInstanceId)
        super.onDestroy()
    }
}

