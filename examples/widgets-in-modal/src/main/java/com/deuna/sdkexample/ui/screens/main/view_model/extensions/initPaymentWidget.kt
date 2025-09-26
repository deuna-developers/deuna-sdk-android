package com.deuna.sdkexample.ui.screens.main.view_model.extensions

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.deuna.maven.initPaymentWidget
import com.deuna.maven.widgets.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.shared.toMap
import com.deuna.sdkexample.shared.PaymentWidgetResult
import com.deuna.sdkexample.ui.screens.main.view_model.DEBUG_TAG
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject


fun MainViewModel.showPaymentWidget(
    context: Context,
    completion: (PaymentWidgetResult) -> Unit,
) {
    deunaSDK.initPaymentWidget(
        context = context,
        orderToken = orderToken.trim(),
        callbacks = PaymentWidgetCallbacks().apply {
            onSuccess = { order ->
                deunaSDK.close {
                    viewModelScope.launch {
                        completion(
                            PaymentWidgetResult.Success(
                                order
                            )
                        )
                    }
                }
            }
            onError = { error ->
                when (error.type) {
                    // The widget could not be loaded
                    PaymentsError.Type.INITIALIZATION_FAILED -> {
                        deunaSDK.close {
                            completion(PaymentWidgetResult.Error(error))
                        }
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
            onCardBinDetected = { cardBinMetadata ->
                deunaSDK.setCustomStyle(
                    data = JSONObject(
                        """
                        {
                          "theme": {
                            "colors": {
                              "primaryTextColor": "#023047",
                              "backgroundSecondary": "#8ECAE6",
                              "backgroundPrimary": "#F2F2F2",
                              "buttonPrimaryFill": "#FFB703",
                              "buttonPrimaryHover": "#FFB703",
                              "buttonPrimaryText": "#000000",
                              "buttonPrimaryActive": "#FFB703"
                            }
                          },
                          "HeaderPattern": {
                            "overrides": {
                              "Logo": {
                                "props": {
                                  "url": "https://images-staging.getduna.com/ema/fc78ef09-ffc7-4d04-aec3-4c2a2023b336/test2.png"
                                }
                              }
                            }
                          }
                        }
                        """
                    ).toMap()
                )
                deunaSDK.refetchOrder { order ->
                    if (order != null) {
                        Log.d(DEBUG_TAG, "refetchOrder $order")
                    } else {
                        Log.d(DEBUG_TAG, "refetchOrder has failed")
                    }

                }
            }
            onPaymentProcessing = {
                Log.d(DEBUG_TAG, "onPaymentProcessing")
            }
            onEventDispatch = { event, data ->
                Log.d(DEBUG_TAG, "onEventDispatch ${event.name}: $data")
            }
        },
        userToken = userToken,
//        paymentMethods = listOf(
//            mapOf(
//                "paymentMethod" to "wallet",
//                "processors" to listOf("paypal_wallet"),
//                "configuration" to mapOf(
//                    "express" to true,
//                    "flowType" to mapOf(
//                        "type" to "twoStep"
//                    )
//                )
//            )
//        )
    )
}