package com.deuna.compose_demo.view_models.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.deuna.compose_demo.screens.PaymentWidgetResult
import com.deuna.maven.initPaymentWidget
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.refetchOrder
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.shared.toMap
import kotlinx.coroutines.launch
import org.json.JSONObject


fun HomeViewModel.showPaymentWidget(
    context: Context,
    completion: (PaymentWidgetResult) -> Unit,
) {
    deunaSDK.initPaymentWidget(
        context = context,
        orderToken = orderToken.value.trim(),
        callbacks = PaymentWidgetCallbacks().apply {
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
                Log.e(ERROR_TAG, "on error ${error.type} , ${error.metadata}")
                when (error.type) {
                    PaymentsError.Type.INITIALIZATION_FAILED, PaymentsError.Type.NO_INTERNET_CONNECTION -> {
                        deunaSDK.close()
                        completion(PaymentWidgetResult.Error(error))
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
        userToken = userTokenValue,
        styleFile = "YOUR_THEME_UUID", // optional
//        paymentMethods = listOf(
//            mapOf(
//                "paymentMethod" to "pse",
//                "processors" to listOf("mercadopago")
//            )
//        )
    )
}


