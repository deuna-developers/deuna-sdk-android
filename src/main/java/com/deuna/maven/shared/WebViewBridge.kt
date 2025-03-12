package com.deuna.maven.shared

import android.util.*
import android.webkit.*


// Abstract class to handle communication between the WebView and the native Android code
abstract class WebViewBridge(val name: String) {
    /**
     * The postMessage function is called when a message is received from JavaScript code in a WebView.
     * The message is parsed and the corresponding callbacks are called based on the event type.
     */
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            handleEvent(message)
        } catch (e: Exception) {
            Log.d("WebViewBridge", "postMessage: $e")
        }
    }

    abstract fun handleEvent(message: String)
}