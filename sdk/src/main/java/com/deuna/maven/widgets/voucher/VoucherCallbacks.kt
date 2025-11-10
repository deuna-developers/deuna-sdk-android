package com.deuna.maven.widgets.voucher

import com.deuna.maven.shared.Json
import com.deuna.maven.shared.OnClosed
import com.deuna.maven.widgets.checkout_widget.CheckoutEvent
import com.deuna.maven.widgets.payment_widget.OnError
import com.deuna.maven.widgets.payment_widget.OnEventDispatch
import com.deuna.maven.widgets.payment_widget.OnSuccess

class VoucherCallbacks() {
    var onSuccess: OnSuccess? = null
    var onError: OnError? = null
    var onClosed: OnClosed? = null
    var onEventDispatch: OnEventDispatch<Json, CheckoutEvent>? = null
}