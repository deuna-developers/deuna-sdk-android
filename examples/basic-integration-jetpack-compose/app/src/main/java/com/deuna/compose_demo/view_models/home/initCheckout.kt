package com.deuna.compose_demo.view_models.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.deuna.compose_demo.screens.CheckoutResult
import com.deuna.maven.checkout.domain.CheckoutEvent
import com.deuna.maven.initCheckout
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.enums.CloseAction
import kotlinx.coroutines.launch


/**
 * Initiates the payment process.
 * @param context The activity context.
 * @param completion Callback to be invoked upon completion of the payment process.
 */
fun HomeViewModel.showCheckout(
    context: Context,
    completion: (CheckoutResult) -> Unit,
) {
    deunaSDK.initCheckout(
        context = context,
        orderToken = orderToken.value.trim(),
        callbacks = CheckoutCallbacks().apply {
            onSuccess = { order ->
                deunaSDK.close()
                viewModelScope.launch {
                    completion(
                        CheckoutResult.Success(
                            order
                        )
                    )
                }
            }
            onError = { error ->
                Log.e(ERROR_TAG, "on error ${error.type} , ${error.metadata}")
                when (error.type) {
                    PaymentsError.Type.INITIALIZATION_FAILED, PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED, PaymentsError.Type.NO_INTERNET_CONNECTION -> {
                        deunaSDK.close()
                        completion(CheckoutResult.Error(error))
                    }

                    else -> {}
                }
            }
            onClosed = { action ->
                Log.e(DEBUG_TAG, "closeAction: $action")
                if (action == CloseAction.userAction) { // The operation was canceled
                    viewModelScope.launch {
                        completion(CheckoutResult.Canceled)
                    }
                }
            }
            onEventDispatch = { event, data ->
                Log.d(DEBUG_TAG, "onEventDispatch ${event.name}: $data")
                when (event) {
                    CheckoutEvent.changeCart, CheckoutEvent.changeAddress -> {
                        deunaSDK.close()
                        viewModelScope.launch {
                            completion(CheckoutResult.Canceled)
                        }
                    }

                    else -> Log.d(DEBUG_TAG, "on event ${event.value}")
                }
            }
        },
        userToken = userTokenValue,
    )
}