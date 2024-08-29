package com.deuna.maven.payment_widget.domain

import com.deuna.maven.checkout.domain.CheckoutEvent
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.VoidCallback

typealias OnReFetchOrder = (completion: (Json?) -> Unit) -> Unit
typealias OnSuccess = (data: Json) -> Unit
typealias OnError = (type: PaymentsError) -> Unit
typealias OnCardBinDetected = (Json?, OnReFetchOrder) -> Unit
typealias OnInstallmentSelected = (Json?, OnReFetchOrder) -> Unit
typealias OnEventDispatch<S, E> = (E, S) -> Unit

// Class defining the different callbacks that can be invoked by the payment widget
class PaymentWidgetCallbacks {
    var onSuccess: OnSuccess? = null
    var onError: OnError? = null
    var onClosed: VoidCallback? = null
    var onCardBinDetected: OnCardBinDetected? = null
    var onInstallmentSelected: OnInstallmentSelected? = null
    var onCanceled: VoidCallback? = null
    var onPaymentProcessing: VoidCallback? = null
    var onEventDispatch: OnEventDispatch<Json, CheckoutEvent>? = null
}