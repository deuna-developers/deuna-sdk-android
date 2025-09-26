package com.deuna.maven.checkout

import OrderResponse
import android.app.Activity
import com.deuna.maven.checkout.domain.DeunaErrorMessage

class Callbacks {
    var onSuccess: ((OrderResponse) -> Unit)? = null
    var onError: ((DeunaErrorMessage?) -> Unit)? = null
    var onClose: ((Activity) -> Unit)? = null
    var eventListener: ((OrderResponse, CheckoutEvents) -> Unit)? = null
}
