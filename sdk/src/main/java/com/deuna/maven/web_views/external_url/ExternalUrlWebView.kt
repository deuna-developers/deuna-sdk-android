package com.deuna.maven.web_views.external_url

import android.content.Context
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.web_views.base.BaseWebView
import com.deuna.maven.web_views.file_downloaders.downloadFile


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
                launch(url)
            }

            override fun onDownloadFile(url: String) {
                downloadFile(url)
            }

        }
    }

    fun launch(url: String) {
        super.loadUrl(url) {
            return@loadUrl """
            window.close = function() {
               windowClose.onCloseWindowCalled();
            };
            """.trimIndent()
        }
    }


    inner class LocalBridge {
        @JavascriptInterface
        fun onCloseWindowCalled() {
            DeunaLogs.info("window.close()")
            onRemoteCloseCalled?.invoke()
        }
    }
}
