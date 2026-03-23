package com.deuna.sdkexample.ui.screens.main.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import com.deuna.sdkexample.shared.CheckoutResult
import com.deuna.sdkexample.shared.ElementsResult
import com.deuna.sdkexample.shared.PaymentWidgetResult
import com.deuna.sdkexample.testing.TestEvent
import com.deuna.sdkexample.testing.TestEventBroadcaster
import com.deuna.sdkexample.ui.screens.main.WidgetToShow
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.clickToPay
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.initVoucher
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.launchNextAction
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.saveCard
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.showCheckout
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.showPaymentWidget
import org.json.JSONObject


fun showWidgetInModal(
    context: Context,
    viewModel: MainViewModel,
    widgetToShow: WidgetToShow,
    navController: NavController
) {
    when (widgetToShow) {
        WidgetToShow.PAYMENT_WIDGET -> {
            viewModel.showPaymentWidget(context = context, completion = { result ->
                when (result) {
                    is PaymentWidgetResult.Canceled -> Log.d("PAYMENT", "Canceled")
                    is PaymentWidgetResult.Error -> {
                        Log.d("PAYMENT", "Error")
                        TestEventBroadcaster.broadcast(TestEvent.PAYMENT_ERROR)
                    }
                    is PaymentWidgetResult.Success -> {
                        Log.d("PAYMENT", "Success")
                        TestEventBroadcaster.broadcast(TestEvent.PAYMENT_SUCCESS)
                        val orderStr = Uri.encode(JSONObject(result.order).toString())
                        navController.navigate(
                            "payment-success/$orderStr"
                        )
                    }
                }
            })
        }

        WidgetToShow.NEXT_ACTION_WIDGET -> {
            viewModel.launchNextAction(context = context, completion = { result ->
                when (result) {
                    is PaymentWidgetResult.Canceled -> Log.d("PAYMENT", "Canceled")
                    is PaymentWidgetResult.Error -> Log.d("PAYMENT", "Error")
                    is PaymentWidgetResult.Success -> {
                        Log.d("PAYMENT", "Success")
                        val orderStr = Uri.encode(JSONObject(result.order).toString())
                        navController.navigate(
                            "payment-success/$orderStr"
                        )
                    }
                }
            })
        }

        WidgetToShow.VOUCHER_WIDGET -> {
            viewModel.initVoucher(context = context, completion = { result ->
                when (result) {
                    is PaymentWidgetResult.Canceled -> Log.d("PAYMENT", "Canceled")
                    is PaymentWidgetResult.Error -> Log.d("PAYMENT", "Error")
                    is PaymentWidgetResult.Success -> {
                        Log.d("PAYMENT", "Success")
                        val orderStr = Uri.encode(JSONObject(result.order).toString())
                        navController.navigate(
                            "payment-success/$orderStr"
                        )
                    }
                }
            })
        }

        WidgetToShow.CHECKOUT_WIDGET -> {
            viewModel.showCheckout(
                context = context,
                completion = { result ->
                    when (result) {
                        is CheckoutResult.Canceled -> Log.d("CHECKOUT", "Canceled")
                        is CheckoutResult.Error -> Log.d("CHECKOUT", "Error")
                        is CheckoutResult.Success -> {
                            Log.d("CHECKOUT", "Success")
                            val orderStr = Uri.encode(JSONObject(result.order).toString())
                            navController.navigate(
                                "payment-success/$orderStr"
                            )
                        }
                    }
                }
            )
        }

        WidgetToShow.VAULT_WIDGET -> {
            viewModel.saveCard(
                context = context,
                completion = { result ->
                    when (result) {
                        is ElementsResult.Canceled -> Log.d("VAULT", "Canceled")
                        is ElementsResult.Error -> Log.d("VAULT", "Error")
                        is ElementsResult.Success -> {
                            Log.d("VAULT", "Success")
                            val savedCardStr = Uri.encode(JSONObject(result.savedCard).toString())
                            navController.navigate(
                                "card-saved-success/$savedCardStr"
                            )
                        }
                    }
                }
            )
        }

        WidgetToShow.CLICK_TO_PAY_WIDGET -> {
            viewModel.clickToPay(
                context = context,
                completion = { result ->
                    when (result) {
                        is ElementsResult.Canceled -> Log.d("CLICK_TO_PAY", "Canceled")
                        is ElementsResult.Error -> Log.d("CLICK_TO_PAY", "Error")
                        is ElementsResult.Success -> {
                            Log.d("CLICK_TO_PAY", "Success")
                            val savedCardStr = Uri.encode(JSONObject(result.savedCard).toString())
                            navController.navigate(
                                "card-saved-success/$savedCardStr"
                            )
                        }
                    }
                }
            )
        }
    }
}