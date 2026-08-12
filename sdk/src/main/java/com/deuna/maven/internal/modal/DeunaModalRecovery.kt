package com.deuna.maven.internal.modal

import android.os.Handler
import android.os.Looper
import com.deuna.maven.client.sendOrder
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import com.deuna.maven.widgets.configuration.NextActionWidgetConfiguration
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal object DeunaModalRecovery {
    private const val MAX_RECOVERY_ATTEMPTS = 12
    private const val RECOVERY_RETRY_DELAY_MS = 3_000L
    private val mainHandler = Handler(Looper.getMainLooper())
    private val recoveryAttempts = mutableMapOf<String, Int>()

    fun tryRecoverSuccessOnSystemClose(
        widgetConfiguration: DeunaWidgetConfiguration,
        closeAction: CloseAction,
    ) {
        if (closeAction != CloseAction.systemAction) {
            return
        }
        if (widgetConfiguration.hasReportedSuccess) {
            return
        }

        val orderToken = when (widgetConfiguration) {
            is PaymentWidgetConfiguration -> widgetConfiguration.orderToken
            is CheckoutWidgetConfiguration -> widgetConfiguration.orderToken
            is NextActionWidgetConfiguration -> widgetConfiguration.orderToken
            is VoucherWidgetConfiguration -> widgetConfiguration.orderToken
            else -> null
        } ?: return

        attemptRecovery(widgetConfiguration, orderToken)
    }

    private fun attemptRecovery(
        widgetConfiguration: DeunaWidgetConfiguration,
        orderToken: String,
    ) {
        if (widgetConfiguration.hasReportedSuccess) {
            clearRecoveryState(orderToken)
            return
        }

        val attempt = (recoveryAttempts[orderToken] ?: 0) + 1
        recoveryAttempts[orderToken] = attempt

        sendOrder(
            baseUrl = widgetConfiguration.sdkInstance.environment.checkoutBaseUrl,
            orderToken = orderToken,
            apiKey = widgetConfiguration.sdkInstance.publicApiKey,
            callback = object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (!response.isSuccessful) {
                        retryIfNeeded(widgetConfiguration, orderToken, attempt)
                        return
                    }

                    val body = response.body() as? Map<*, *> ?: run {
                        retryIfNeeded(widgetConfiguration, orderToken, attempt)
                        return
                    }
                    val order = body["order"] as? Map<*, *> ?: run {
                        retryIfNeeded(widgetConfiguration, orderToken, attempt)
                        return
                    }
                    val normalizedOrder = order.toJsonMap()

                    if (!isSuccessfulOrder(normalizedOrder)) {
                        retryIfNeeded(widgetConfiguration, orderToken, attempt)
                        return
                    }
                    widgetConfiguration.hasReportedSuccess = true
                    clearRecoveryState(orderToken)

                    when (widgetConfiguration) {
                        is PaymentWidgetConfiguration -> widgetConfiguration.callbacks.onSuccess?.invoke(
                            normalizedOrder
                        )

                        is CheckoutWidgetConfiguration -> widgetConfiguration.callbacks.onSuccess?.invoke(
                            normalizedOrder
                        )

                        is NextActionWidgetConfiguration -> widgetConfiguration.callbacks.onSuccess?.invoke(
                            normalizedOrder
                        )

                        is VoucherWidgetConfiguration -> widgetConfiguration.callbacks.onSuccess?.invoke(
                            normalizedOrder
                        )

                        else -> Unit
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    retryIfNeeded(widgetConfiguration, orderToken, attempt)
                }
            }
        )
    }

    private fun retryIfNeeded(
        widgetConfiguration: DeunaWidgetConfiguration,
        orderToken: String,
        attempt: Int,
    ) {
        if (attempt >= MAX_RECOVERY_ATTEMPTS) {
            clearRecoveryState(orderToken)
            return
        }

        mainHandler.postDelayed(
            { attemptRecovery(widgetConfiguration, orderToken) },
            RECOVERY_RETRY_DELAY_MS
        )
    }

    private fun clearRecoveryState(orderToken: String) {
        recoveryAttempts.remove(orderToken)
    }

    private fun isSuccessfulOrder(order: Json): Boolean {
        val status = (order["status"] as? String)?.lowercase()
        val paymentStatus = (order["payment_status"] as? String)?.lowercase()
        val paid = order["paid"] as? Boolean

        val successValues = setOf("approved", "paid", "completed", "success", "succeeded")

        return paid == true || status in successValues || paymentStatus in successValues
    }

    private fun Map<*, *>.toJsonMap(): Json {
        return entries.associate { (key, value) ->
            key.toString() to value.normalizeValue()
        }.toMutableMap()
    }

    private fun Any?.normalizeValue(): Any? {
        return when (this) {
            is Map<*, *> -> this.toJsonMap()
            is List<*> -> this.map { it.normalizeValue() }
            else -> this
        }
    }
}
