package com.deuna.maven.payment_widget.domain

import com.deuna.maven.checkout.domain.CheckoutEvent
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.OnClosed
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.VoidCallback

typealias OnSuccess = (data: Json) -> Unit
typealias OnError = (type: PaymentsError) -> Unit
typealias OnCardBinDetected = (Json?) -> Unit
typealias OnInstallmentSelected = (Json?) -> Unit
typealias OnEventDispatch<S, E> = (E, S) -> Unit

// Class defining the different callbacks that can be invoked by the payment widget
class PaymentWidgetCallbacks {
    var onSuccess: OnSuccess? = null
    var onError: OnError? = null
    var onClosed: OnClosed? = null
    var onCardBinDetected: OnCardBinDetected? = null
    var onInstallmentSelected: OnInstallmentSelected? = null
    var onPaymentProcessing: VoidCallback? = null
    var onEventDispatch: OnEventDispatch<Json, CheckoutEvent>? = null
}