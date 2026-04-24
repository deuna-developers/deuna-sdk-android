package com.deuna.maven.web_views.external_url

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.JavascriptInterface
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.web_views.base.BaseWebView
import com.deuna.maven.web_views.file_downloaders.downloadFile
import com.deuna.maven.web_views.file_downloaders.runOnUiThread
import java.net.URL


@Suppress("UNCHECKED_CAST")
class ExternalUrlWebView(
    context: Context,
    attrs: AttributeSet? = null
) : BaseWebView(context, attrs) {

    var onRemoteCloseCalled: (() -> Unit)? = null


    init {
        webView.addJavascriptInterface(LocalBridge(), "windowClose")
        listener = object : Listener {
            override fun onWebViewLoaded() {
                webView.evaluateJavascript(
                    """
                    (function() {
                        setTimeout(function() {
                            var button = document.getElementById("cash_efecty_button_print");
                            if (button) {
                                button.style.display = "none";
                            }
                    }, 500); // time out 500 ms
                    })();
                """, null
                )
            }

            override fun onWebViewError() {}

            override fun onOpenExternalUrl(url: String, userInitiated: Boolean) {
                runOnUiThread {
                    launch(url)
                }
            }

            override fun onDownloadFile(url: String) {
                downloadFile(url)
            }

        }
    }

    fun launch(url: String) {
        runOnUiThread {
            configureRenderingFor(url)
            super.loadUrl(url) {
                return@loadUrl """
                window.close = function() {
                   windowClose.onCloseWindowCalled();
                };
                """.trimIndent()
            }
        }
    }

    private fun configureRenderingFor(url: String) {
        val host = runCatching { URL(url).host.lowercase() }.getOrNull().orEmpty()
        val useSoftwareLayer = host.contains("aplazo.net")
        webView.setLayerType(
            if (useSoftwareLayer) View.LAYER_TYPE_SOFTWARE else View.LAYER_TYPE_NONE,
            null
        )
    }


    inner class LocalBridge {
        @JavascriptInterface
        fun onCloseWindowCalled() {
            DeunaLogs.info("window.close()")
            onRemoteCloseCalled?.invoke()
        }
    }
}
