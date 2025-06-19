package com.deuna.sdkexample.ui.screens.main.view_model.extensions

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.deuna.maven.initVoucherWidget
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.widgets.voucher.VoucherCallbacks
import com.deuna.sdkexample.shared.PaymentWidgetResult
import com.deuna.sdkexample.ui.screens.main.view_model.DEBUG_TAG
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel
import kotlinx.coroutines.launch

fun MainViewModel.initVoucher(
    context: Context,
    completion: (PaymentWidgetResult) -> Unit,
) {
    deunaSDK.initVoucherWidget(
        context = context,
        orderToken = orderToken,
        callbacks = VoucherCallbacks().apply {
            onSuccess = { order ->
                deunaSDK.close()
                viewModelScope.launch {
                    completion(
                        PaymentWidgetResult.Success(
                            order
                        )
                    )
                }
            }
            onError = { error ->
                when (error.type) {
                    // The widget could not be loaded
                    PaymentsError.Type.INITIALIZATION_FAILED -> {
                        deunaSDK.close()
                        completion(PaymentWidgetResult.Error(error))
                    }

                    // The payment was failed
                    PaymentsError.Type.PAYMENT_ERROR -> {
                        // YOUR CODE HERE
                        Log.i(DEBUG_TAG, error.type.name)
                    }

                    else -> {}
                }
            }
            onClosed = { action ->
                Log.i(DEBUG_TAG, "closeAction: $action")
                if (action == CloseAction.userAction) { // payment process was canceled
                    viewModelScope.launch {
                        completion(PaymentWidgetResult.Canceled)
                    }
                }

            }
            onEventDispatch = { event, data ->
                Log.d(DEBUG_TAG, "onEventDispatch ${event.name}: $data")
            }
        },
    )
}


