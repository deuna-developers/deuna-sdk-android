package com.example.demoandroid

import android.util.Log
import com.deuna.maven.initPaymentWidget
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.shared.PaymentsError
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.shared.toMap
import org.json.JSONObject


/**
 * Show the payment widget that processes a payment request
 */
fun MainActivity.showPaymentWidget() {
    deunaSdk.initPaymentWidget(
        context = this, orderToken = orderToken,
        styleFile = "YOUR_THEME_UUID", // optional
        callbacks = PaymentWidgetCallbacks().apply {
            onSuccess = { order ->
                deunaSdk.close()
                handlePaymentSuccess(order)
            }
            onClosed = { action ->
                if (action == CloseAction.userAction) {
                    Log.d(DEBUG_TAG, "Payment was canceled by user")
                }
            }
            onCardBinDetected = { cardBinMetadata ->
                Log.d(DEBUG_TAG, "cardBinMetadata: $cardBinMetadata")
                if (cardBinMetadata != null) {

                    deunaSdk.setCustomStyle(
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

                }
            }
            onInstallmentSelected = { metadata ->
                Log.d(DEBUG_TAG, "installmentMetadata: $metadata")
            }
            onError = { error ->
                Log.e(DEBUG_TAG, "Error type: ${error.type}, metadata: ${error.metadata}")
                when (error.type) {
                    PaymentsError.Type.INITIALIZATION_FAILED, PaymentsError.Type.NO_INTERNET_CONNECTION -> {
                        deunaSdk.close()
                        if (error.metadata != null) {
                            showPaymentErrorAlertDialog(error.metadata!!)
                        }
                    }

                    else -> {}
                }
            }
            onEventDispatch = { type, data ->
                Log.d(DEBUG_TAG, "onEventDispatch ${type.name}: $data")
            }
        },
        userToken = userToken,
//        paymentMethods = listOf(
//            mapOf(
//                "paymentMethod" to "voucher",
//                "processors" to listOf("payu_oxxo_cash")
//            )
//        )
    )
}