package com.example.demoandroid

import android.util.Log
import com.deuna.maven.checkout.domain.CheckoutEvent
import com.deuna.maven.initCheckout
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.PaymentsError


/**
 * Show the checkout widget that processes a payment request
 */
fun MainActivity.startPaymentProcess() {
    deunaSdk.initCheckout(
        context = this,
        orderToken = orderToken,
        callbacks = CheckoutCallbacks().apply {
            onSuccess = { data ->
                Log.d(DEBUG_TAG, "Payment success $data")
                deunaSdk.close()
                handlePaymentSuccess(data)
            }
            onError = { error ->
                Log.e(DEBUG_TAG, "Error type: ${error.type}, metadata: ${error.metadata}")
                when (error.type) {
                    PaymentsError.Type.PAYMENT_ERROR, PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED, PaymentsError.Type.INITIALIZATION_FAILED -> {
                        deunaSdk.close()
                        if (error.metadata != null) {
                            showPaymentErrorAlertDialog(error.metadata!!)
                        }
                    }

                    else -> {}
                }
            }
            onCanceled = {
                Log.d(DEBUG_TAG, "Payment was canceled by user")
            }
            onEventDispatch = { type, data ->
                Log.d(DEBUG_TAG, "onEventDispatch ${type.name}: $data")
                when (type) {
                    CheckoutEvent.changeAddress, CheckoutEvent.changeCart -> {
                        deunaSdk.close()
                    }

                    else -> {}
                }
            }
            onClosed = {
                Log.d(DEBUG_TAG, "Widget was closed")
            }
        },
        userToken = userToken,
    )
}