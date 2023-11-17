package com.deuna.maven

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.deuna.maven.checkout.Callbacks
import com.deuna.maven.checkout.CheckoutEvents
import com.deuna.maven.checkout.DeunaActivity
import com.deuna.maven.checkout.domain.ElementType
import com.deuna.maven.checkout.domain.Environment
import com.deuna.maven.element.DeunaElementActivity
import com.deuna.maven.element.domain.ElementCallbacks
import java.util.Locale


open class DeUnaSdk {
    private lateinit var apiKey: String
    private lateinit var orderToken: String
    private lateinit var environment: Environment
    private lateinit var userToken: String
    private var baseUrl: String = ""
    private var elementUrl: String = "https://elements.deuna.io"
    private var closeOnEvents: Array<CheckoutEvents>? = null
    private var loggingEnabled: Boolean? = false
    private var context: Context? = null
    private var callbacks: Callbacks? = null
    private var elementCallbacks: ElementCallbacks? = null
    private var showCloseButton: Boolean? = null
    private var apigatewayUrl: String = "https://api.dev.deuna.io"

    companion object {
        private lateinit var instance: DeUnaSdk

        /**
         * Configure the DeUna SDK with the given parameters.
         * @param apiKey The API key to use for the DeUna SDK.
         * @param orderToken The order token to use for the DeUna SDK.
         * @param environment The environment to use for the DeUna SDK.
         * @param closeOnEvents The events to close the DeUna SDK on.
         * @param loggingEnabled Whether to enable logging for the DeUna SDK.
         * @param context The context to use for the DeUna SDK.
         * @throws IllegalStateException if the SDK has already been configured.
         */
        fun config(
            apiKey: String? = null,
            orderToken: String? = null,
            userToken: String? = null,
            environment: Environment,
            closeOnEvents: Array<CheckoutEvents>? = null,
            context: Context,
            callbacks: Callbacks? = null,
            elementCallbacks: ElementCallbacks? = null,
            showCloseButton: Boolean? = null
        ) {
            instance = DeUnaSdk().apply {

                if (showCloseButton != null) {
                    this.showCloseButton = showCloseButton
                }

                if (callbacks != null) {
                    this.callbacks = callbacks
                }

                if (elementCallbacks != null) {
                    this.elementCallbacks = elementCallbacks
                }

                if (apiKey != null) {
                    this.apiKey = apiKey
                }

                if (orderToken != null) {
                    this.orderToken = orderToken
                }

                this.context = context

                if (closeOnEvents != null) {
                    this.closeOnEvents = closeOnEvents
                }

                this.environment = environment

                if (environment == Environment.DEVELOPMENT) {
                    this.loggingEnabled = true
                }

                if (environment == Environment.STAGING) {
                    this.apigatewayUrl = "https://api.stg.deuna.io"
                } else if (environment == Environment.PRODUCTION) {
                    this.apigatewayUrl = "https://api.deuna.io"
                } else if (environment == Environment.SANDBOX) {
                    this.apigatewayUrl = "https://api.sbx.deuna.io"
                } else {
                    this.apigatewayUrl = "https://api.dev.deuna.io"
                }

                if (userToken != null && apiKey != null) {
                    var url = when (this.environment) {
                        Environment.DEVELOPMENT -> "https://elements.dev.deuna.io/{type}"
                        Environment.STAGING -> "https://elements.stg.deuna.io/{type}"
                        Environment.PRODUCTION -> "https://elements.deuna.io/{type}"
                        Environment.SANDBOX -> "https://elements.sbx.deuna.io/{type}"
                    }
                    val builder = Uri.parse(url).buildUpon()
                    builder.appendQueryParameter("userToken", userToken)
                    builder.appendQueryParameter("publicApiKey", apiKey)
                    if (showCloseButton != null) {
                        builder.appendQueryParameter("mode", "widget")
                    }
                    this.elementUrl = builder.build().toString()
                }

                if (userToken != null || apiKey != null) {
                    val url = this.baseUrl
                    val builder = Uri.parse(url).buildUpon()
                    if (userToken != null) {
                        builder.appendQueryParameter("userToken", userToken)
                    }
                    this.baseUrl = builder.build().toString()
                }
            }
        }

        /**
         * Close the DeUna SDK.
         */
        fun close() {
            instance.context?.sendBroadcast(Intent("com.deuna.maven.CLOSE_ALL"))
        }

        /**
         * Initialize the DeUna SDK Checkout with the configured parameters.
         * @throws IllegalStateException if the SDK has not been configured.
         */
        fun initCheckout(
        ) {
            DeunaActivity.setCallback(instance.callbacks)
            Intent(instance.context!!, DeunaActivity::class.java).apply {
                putExtra(DeunaActivity.ORDER_TOKEN, instance.orderToken)
                putExtra(DeunaActivity.API_KEY, instance.apiKey)
                putExtra(DeunaActivity.BASE_URL, instance.apigatewayUrl)
                putExtra(DeunaActivity.LOGGING_ENABLED, instance.loggingEnabled)
                startActivity(instance.context!!, this, null)
            }
        }

        /**
         * Initialize the DeUna SDK Elements with the configured parameters.
         * @param element The element to initialize.
         * @throws IllegalStateException if the SDK has not been configured.
         */
        fun initElements(element: ElementType) {
            DeunaElementActivity.setCallback(instance.elementCallbacks)
            var newUrl = instance.elementUrl
                .replace("{type}", element.toString().lowercase(Locale.ROOT))
            Log.d("elementUrl", newUrl)
            Intent(instance.context!!, DeunaElementActivity::class.java).apply {
                putExtra(DeunaElementActivity.EXTRA_URL, newUrl)
                putExtra(DeunaElementActivity.LOGGING_ENABLED, instance.loggingEnabled)
                startActivity(instance.context!!, this, null)
            }
        }
    }
}