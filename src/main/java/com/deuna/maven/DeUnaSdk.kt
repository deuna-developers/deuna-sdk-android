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
import com.deuna.maven.element.domain.ElementEvent
import com.deuna.maven.shared.ApiGatewayUrl
import com.deuna.maven.shared.ElementUrl
import java.util.Locale


open class DeUnaSdk {
    private lateinit var apiKey: String
    private lateinit var environment: Environment
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
         * @param environment The environment to use for the DeUna SDK.
         * @param closeOnEvents The events to close the DeUna SDK on.
         * @param context The context to use for the DeUna SDK.
         * @param callbacks The callbacks to use for the DeUna SDK.
         * @param elementCallbacks The element callbacks to use for the DeUna SDK.
         * @param showCloseButton Whether to show the close button in the DeUna SDK.
         * @throws IllegalStateException if the SDK has already been configured.
         */
        fun config(
            apiKey: String? = null,
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

                this.context = context

                if (closeOnEvents != null) {
                    this.closeOnEvents = closeOnEvents
                }

                this.environment = environment

                if (environment == Environment.DEVELOPMENT) {
                    this.loggingEnabled = true
                }

                if (environment == Environment.STAGING) {
                    this.apigatewayUrl = ApiGatewayUrl.STAGING.url
                } else if (environment == Environment.PRODUCTION) {
                    this.apigatewayUrl = ApiGatewayUrl.PRODUCTION.url
                } else if (environment == Environment.SANDBOX) {
                    this.apigatewayUrl = ApiGatewayUrl.SANDBOX.url
                } else {
                    this.apigatewayUrl = ApiGatewayUrl.DEVELOPMENT.url
                }
            }
        }

        /**
         * Close the DeUna SDK.
         */
        fun closeCheckout() {
            instance.context?.sendBroadcast(Intent("com.deuna.maven.CLOSE_CHECKOUT"))
        }

        fun closeElements() {
            instance.context?.sendBroadcast(Intent("com.deuna.maven.CLOSE_ELEMENTS"))
        }

        /**
         * Initialize the DeUna SDK Checkout with the configured parameters.
         * @param orderToken The order token to use for the DeUna SDK.
         * @throws IllegalStateException if the SDK has not been configured.
         */
        fun initCheckout(
            orderToken: String
        ) {
            instance.closeOnEvents = instance.closeOnEvents ?: emptyArray()
            DeunaActivity.setCallback(instance.callbacks)
            Intent(instance.context!!, DeunaActivity::class.java).apply {
                putExtra(DeunaActivity.ORDER_TOKEN, orderToken)
                putExtra(DeunaActivity.API_KEY, instance.apiKey)
                putExtra(DeunaActivity.BASE_URL, instance.apigatewayUrl)
                putExtra(DeunaActivity.LOGGING_ENABLED, instance.loggingEnabled)
                putStringArrayListExtra(
                    DeunaActivity.CLOSE_ON_EVENTS,
                    ArrayList(instance.closeOnEvents!!.map { it.name })
                )
                startActivity(instance.context!!, this, null)
            }
        }

        /**
         * Initialize the DeUna SDK Elements with the configured parameters.
         * @param element The element to initialize.
         * @param userToken The user token to use for the DeUna SDK.
         * @throws IllegalStateException if the SDK has not been configured.
         */
        fun initElements(element: ElementType, userToken: String) {
            instance.closeOnEvents = instance.closeOnEvents ?: emptyArray()
            DeunaElementActivity.setCallback(instance.elementCallbacks)
            buildElementUrl(userToken, instance.apiKey, element)
            Log.d("elementUrl", instance.elementUrl)
            Intent(instance.context!!, DeunaElementActivity::class.java).also {
                it.putExtra(DeunaElementActivity.EXTRA_URL, instance.elementUrl)
                it.putExtra(DeunaElementActivity.LOGGING_ENABLED, instance.loggingEnabled)
                it.putStringArrayListExtra(
                    DeunaElementActivity.CLOSE_ON_EVENTS,
                    ArrayList(instance.closeOnEvents!!.map { it.name })
                )
                startActivity(instance.context!!, it, null)
            }
        }

        /**
         * Build the element URL with the given parameters.
         * @param userToken The user token to use for the DeUna SDK.
         * @param apiKey The API key to use for the DeUna SDK.
         * @param element The element to use for the DeUna SDK.
         */
        private fun buildElementUrl(userToken: String, apiKey: String, element: ElementType) {
            val url = when (instance.environment) {
                Environment.DEVELOPMENT -> "${ElementUrl.DEVELOPMENT.url}/{type}"
                Environment.STAGING -> "${ElementUrl.STAGING.url}/{type}"
                Environment.PRODUCTION -> "${ElementUrl.PRODUCTION.url}/{type}"
                Environment.SANDBOX -> "${ElementUrl.SANDBOX.url}/{type}"
            }
            instance.elementUrl = Uri.parse(url).buildUpon().apply {
                appendQueryParameter("userToken", userToken)
                appendQueryParameter("publicApiKey", apiKey)
                if (instance.showCloseButton != null) {
                    appendQueryParameter("mode", "widget")
                }
            }.build().toString().replace("{type}", element.toString().lowercase(Locale.ROOT))
        }
    }
}