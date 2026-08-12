package com.deuna.explore

import android.app.Application
import android.util.Log
import android.webkit.WebView
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import java.util.concurrent.Executor

private const val TAG = "ExploreApplication"

class ExploreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(true)
        applyWebViewProxyOverrideForPreprod()
    }

    // Routes WebView external traffic through deuna-squid-nginx during preprod e2e tests.
    // Chromium does not honor the Android OS proxy set via -http-proxy on the emulator;
    // without this, real external URLs opened in WebView (3DS challenge pages, voucher
    // checkouts) fail with net::ERR_CONNECTION_REFUSED inside the Docker sandbox.
    // No-op outside tests: activates only when test infra writes dynamic_preprod_endpoint
    // to SharedPreferences; that key is never written in production.
    private fun applyWebViewProxyOverrideForPreprod() {
        val endpoint = getSharedPreferences("explore_config", MODE_PRIVATE)
            .getString("dynamic_preprod_endpoint", null)
        if (endpoint.isNullOrBlank()) return

        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            Log.w(TAG, "WebView proxy override not supported on this device — external redirect pages may fail")
            return
        }

        val proxyConfig = ProxyConfig.Builder()
            .addProxyRule("deuna-squid-nginx:8080")
            .addBypassRule("localhost")
            .addBypassRule("127.0.0.1")
            .addBypassRule("10.0.2.2")
            .build()

        ProxyController.getInstance().setProxyOverride(
            proxyConfig,
            Executor { command -> command.run() },
            Runnable {
                Log.d(TAG, "WebView proxy override applied -> deuna-squid-nginx:8080 (bypass: localhost, 127.0.0.1, 10.0.2.2)")
            },
        )
    }
}
