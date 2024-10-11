package com.deuna.maven


import com.deuna.maven.shared.Environment
import com.deuna.maven.web_views.base.BaseWebViewActivity
import org.json.JSONObject
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
    
    val sdkInstanceId: Int
        get() = hashCode()

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
    fun setCustomStyle(data: Map<String, Any>) {
        BaseWebViewActivity.sendCustomStyle(
            sdkInstanceId = sdkInstanceId, dataAsJsonString = JSONObject(data).toString()
        )
    }

    /**
     * Close the active DEUNA widget
     */
    fun close() {
        closeWebView(sdkInstanceId)
    }
}

/**
 * Global function used to send a broadcast event to close the elements view
 */
fun closeWebView(sdkInstanceId: Int) {
    BaseWebViewActivity.closeWebView(sdkInstanceId)
}