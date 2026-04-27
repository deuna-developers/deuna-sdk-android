package com.deuna.maven


import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.VoidCallback
import com.deuna.maven.internal.modal.DeunaModalHost
import com.deuna.maven.web_views.deuna.extensions.refetchOrder
import com.deuna.maven.web_views.deuna.extensions.setCustomStyle
import java.lang.IllegalStateException


/**
 * Class representing the DEUNA SDK.
 *
 * @property environment The DEUNA environment (Environment.PRODUCTION, Environment.SANDBOX, etc).
 * @property publicApiKey The public API key to access DEUNA services (for checkout and elements operations).
 */
open class DeunaSDK(
    val environment: Environment,
    val publicApiKey: String,
) {

    private var modalHost: DeunaModalHost? = null

    init {
        require(publicApiKey.isNotEmpty()) {
            "publicApiKey must not be empty"
        }
    }


    companion object {
        // Unique instance of the DeunaSDK
        private var instance: DeunaSDK? = null

        /**
         * Gets the shared instance of the DEUNA SDK.
         *
         * @throws IllegalStateException if DeunaSDK.initialize is not called before accessing this instance.
         * @return The same instance of DeunaSDK
         */
        val shared: DeunaSDK
            get() {
                return instance ?: throw IllegalStateException(
                    "DeunaSDK.initialize must be called before accessing shared instance"
                )
            }

        /**
         * Registers an unique instance of the Deuna SDK.
         *
         * @param environment The Deuna environment (Environment.PRODUCTION, Environment.SANDBOX, etc).
         * @param publicApiKey The public API key to access Deuna services.
         */
        fun initialize(
            environment: Environment,
            publicApiKey: String,
        ) {
            instance = DeunaSDK(environment, publicApiKey)
        }
    }

    /**
     * Set custom style on the payment widget.
     * This function must be only called inside the next callbacks onCardBinDetected or onInstallmentSelected.
     *
     * @param data The JSON data to update the payment widget UI
     */
    fun setCustomStyle(data: Json) {
        modalHost?.deunaWidget?.setCustomStyle(data)
    }

    /**
     * Sends a re-fetch order request and handles the response.
     *
     * @param callback A callback function to be invoked when the request completes. The callback receives a `Json` object containing the order data or `null` if the request fails.
     */
    fun refetchOrder(callback: (Json?) -> Unit) {
        modalHost?.deunaWidget?.refetchOrder(callback)
    }

    /**
     * Close the active DEUNA widget
     */
    fun close(voidCallback: VoidCallback? = null) {
        val currentHost = modalHost
        val currentWidget = currentHost?.deunaWidget

        if (currentWidget == null) {
            voidCallback?.invoke()
            return
        }

        /**
         * When an external url is opened in a custom tab, we need to wait until the tab is closed
         */
        currentWidget.waitUntilExternalUrlIsClosed {
            currentHost.dismiss()
            voidCallback?.invoke()
        }
    }

    internal fun bindModalHost(host: DeunaModalHost) {
        modalHost = host
    }

    internal fun unbindModalHost(host: DeunaModalHost) {
        if (modalHost === host) {
            modalHost = null
        }
    }
}
