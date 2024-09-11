package com.deuna.maven.web_views.base

import android.annotation.*
import android.app.Activity
import android.os.*
import android.view.*
import android.webkit.*
import android.widget.*
import com.deuna.maven.*


class SubWebViewActivity : Activity() {
    companion object {
        const val EXTRA_URL = "SUB_WEB_VIEW_URL"
        const val EXTRA_SDK_INSTANCE_ID = "SDK_INSTANCE_ID"

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

        loadUrl(intent.getStringExtra(EXTRA_URL)!!)
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadUrl(url: String) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
        }

        webView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.visibility = View.VISIBLE
                loader.visibility = View.GONE
            }
        }
        webView.loadUrl(url)
    }

    override fun onDestroy() {
        activities.remove(sdkInstanceId)
        super.onDestroy()
    }
}