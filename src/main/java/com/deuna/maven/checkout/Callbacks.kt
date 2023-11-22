package com.deuna.maven.checkout

import android.app.Activity
import android.webkit.WebView
import com.deuna.maven.checkout.domain.OrderErrorResponse
import com.deuna.maven.checkout.domain.OrderSuccessResponse

class Callbacks {
    var onSuccess: ((OrderSuccessResponse) -> Unit)? = null
    var onError: ((OrderErrorResponse?, String?, ) -> Unit)? = null
    var onClose: ((Activity) -> Unit)? = null
    var onChangeAddress: ((Activity) -> Unit)? = null
    var onCloseEvents: ((Activity) -> Unit)? = null
}
