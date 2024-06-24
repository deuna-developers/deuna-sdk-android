package com.deuna.maven.web_views

import android.os.*
import com.deuna.maven.element.domain.*
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.base.*

/**
 * This activity displays a web page loaded from a provided URL. It utilizes the ElementsBridge
 * for communication between the WebView and the native Android code. It also interacts with the
 * ElementsCallbacks interface to report success, errors, or cancellation.
 */
class ElementsActivity() : BaseWebViewActivity() {

    companion object {
        const val EXTRA_URL = "EXTRA_URL"
        private var callbacks: ElementsCallbacks? = null

        /**
         * Set the callbacks object to receive element events.
         */
        fun setCallbacks(callbacks: ElementsCallbacks) {
            this.callbacks = callbacks
        }
    }

    // Set of ElementsEvents indicating when to close the activity
    private lateinit var closeEvents: Set<ElementsEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract the URL from the intent
        val url = intent.getStringExtra(EXTRA_URL)!!

        // Parse close events from intent (if any)
        val closeEventAsStrings = intent.getStringArrayListExtra(EXTRA_CLOSE_EVENTS) ?: emptyList<String>()
        closeEvents = parseCloseEvents<ElementsEvent>(closeEventAsStrings)

        // Load the provided URL
        loadUrl(url)
    }

    override fun getBridge(): WebViewBridge {
        return ElementsBridge(
            context = this,
            callbacks = callbacks,
            closeEvents = closeEvents
        )
    }


    // Notify callbacks about no internet connection
    override fun onNoInternet() {
        callbacks?.onError?.invoke(
            ElementsError(
                ElementsErrorType.NO_INTERNET_CONNECTION, null
            )
        )
    }

    override fun onCanceledByUser() {
        callbacks?.onCanceled?.invoke()
    }

    override fun onDestroy() {
        // Notify callbacks about activity closure
        callbacks?.onClosed?.invoke()
        super.onDestroy()
    }

}