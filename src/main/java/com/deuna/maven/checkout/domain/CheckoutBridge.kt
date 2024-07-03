package com.deuna.maven.checkout.domain

import CheckoutResponse
import android.content.Context
import com.deuna.maven.*
import com.deuna.maven.shared.*
import org.json.*

class CheckoutBridge(
    private val context: Context,
    private val callbacks: CheckoutCallbacks?,
    private val closeEvents: Set<CheckoutEvent>,
) : WebViewBridge() {
    override fun handleEvent(message: String) {

        try {
            val json = JSONObject(message)
            val eventData = CheckoutResponse.fromJson(json)
            callbacks?.eventListener?.invoke(eventData.type, eventData)
            when (eventData.type) {
                CheckoutEvent.purchase, CheckoutEvent.apmSuccess -> {
                    callbacks?.onSuccess?.invoke(eventData)
                }

                CheckoutEvent.purchaseRejected -> {
                    handleError(
                        CheckoutErrorType.PAYMENT_ERROR,
                        eventData
                    )
                }

                CheckoutEvent.linkFailed, CheckoutEvent.linkCriticalError, CheckoutEvent.purchaseError -> {
                    handleError(CheckoutErrorType.CHECKOUT_INITIALIZATION_FAILED, eventData)
                }

                CheckoutEvent.linkClose -> {
                    closeCheckout()
                    callbacks?.onCanceled?.invoke()
                }

                CheckoutEvent.paymentMethods3dsInitiated, CheckoutEvent.apmClickRedirect -> {
                    // No action required for these events
                }

                else -> {
                    DeunaLogs.debug("CheckoutBridge Unhandled event: $eventData")
                }
            }

            eventData.let {
                if (closeEvents.contains(it.type)) {
                    closeCheckout()
                }
            }
        } catch (e: JSONException) {
            DeunaLogs.debug("CheckoutBridge JSONException: $e")
        }
    }

    private fun handleError(type: CheckoutErrorType, response: CheckoutResponse) {
        callbacks?.onError?.invoke(
            CheckoutError(
                type,
                response.data.order,
                response.data.user
            )
        )
    }
}
