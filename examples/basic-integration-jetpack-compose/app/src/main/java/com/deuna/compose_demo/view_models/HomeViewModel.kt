package com.deuna.compose_demo.view_models

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.deuna.compose_demo.screens.CheckoutResult
import com.deuna.compose_demo.screens.ElementsResult
import com.deuna.compose_demo.screens.PaymentWidgetResult
import com.deuna.maven.*
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.shared.*
import com.deuna.maven.shared.domain.UserInfo
import kotlinx.coroutines.*
import org.json.JSONObject

const val ERROR_TAG = "❌ DeunaSDK"
const val DEBUG_TAG = "👀 DeunaSDK"

/**
 * ViewModel for the Home screen, responsible for handling user interactions and data manipulation.
 * @param deunaSDK The DeunaSDK instance used for payment and card saving operations.
 */
@Suppress("UNCHECKED_CAST")
class HomeViewModel(private val deunaSDK: DeunaSDK) : ViewModel() {

    // State variables for order token and user token
    val orderToken = mutableStateOf("")
    val userToken = mutableStateOf("")

    val userTokenValue: String?
        get() {
            return userToken.value.ifEmpty { null }
        }


    fun showPaymentWidget(
        context: Context,
        completion: (PaymentWidgetResult) -> Unit,
    ) {
        deunaSDK.initPaymentWidget(
            context = context,
            orderToken = orderToken.value.trim(),
            callbacks = paymentWidgetsCallbacks(completion),
            userToken = userTokenValue,
            cssFile = "YOUR_THEME_UUID", // optional
        )
    }


    private fun checkoutCallbacks(
        completion: (CheckoutResult) -> Unit,
    ): CheckoutCallbacks {
        return CheckoutCallbacks().apply {
            onSuccess = { data ->
                deunaSDK.closeCheckout()
                viewModelScope.launch {
                    completion(
                        CheckoutResult.Success(
                            data["order"] as Json
                        )
                    )
                }
            }
            onError = { error ->
                Log.e(ERROR_TAG, "on error ${error.type} , ${error.metadata}")
                when (error.type) {
                    PaymentsError.Type.PAYMENT_ERROR,
                    PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED,
                    PaymentsError.Type.NO_INTERNET_CONNECTION -> {
                        deunaSDK.closeCheckout()
                        completion(CheckoutResult.Error(error))
                    }

                    else -> {}
                }
            }
            onCanceled = {
                viewModelScope.launch {
                    completion(CheckoutResult.Canceled)
                }
            }
            eventListener = { event, _ ->
                when (event) {
                    CheckoutEvent.changeCart, CheckoutEvent.changeAddress -> {
                        deunaSDK.closeCheckout()
                        viewModelScope.launch {
                            completion(CheckoutResult.Canceled)
                        }
                    }

                    else -> Log.d(DEBUG_TAG, "on event ${event.value}")
                }
            }
        }
    }

    /**
     * Initiates the payment process.
     * @param context The activity context.
     * @param completion Callback to be invoked upon completion of the payment process.
     */
    fun showCheckout(
        context: Context,
        completion: (CheckoutResult) -> Unit,
    ) {
        deunaSDK.initCheckout(
            context = context,
            orderToken = orderToken.value.trim(),
            callbacks = checkoutCallbacks(completion),
            userToken = userTokenValue,
        )
    }


    private fun paymentWidgetsCallbacks(
        completion: (PaymentWidgetResult) -> Unit,
    ): PaymentWidgetCallbacks {
        return PaymentWidgetCallbacks().apply {
            onSuccess = { data ->
                deunaSDK.closePaymentWidget()
                viewModelScope.launch {
                    completion(
                        PaymentWidgetResult.Success(
                            data["order"] as Json
                        )
                    )
                }
            }
            onCanceled = {
                viewModelScope.launch {
                    completion(PaymentWidgetResult.Canceled)
                }
            }
            onError = { error ->
                Log.e(ERROR_TAG, "on error ${error.type} , ${error.metadata}")
                when (error.type) {
                    PaymentsError.Type.INITIALIZATION_FAILED,
                    PaymentsError.Type.NO_INTERNET_CONNECTION -> {
                        deunaSDK.closePaymentWidget()
                        completion(PaymentWidgetResult.Error(error))
                    }

                    else -> {}
                }

            }
            onClosed = {

            }
            onCardBinDetected = { cardBinMetadata, refetchOrder ->
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
            }
        }
    }


    /**
     * Initiates the process of saving the card information.
     * @param context The activity context.
     * @param completion Callback to be invoked upon completion of the card saving process.
     */
    fun saveCard(
        context: Context,
        completion: (ElementsResult) -> Unit,
    ) {
        deunaSDK.initElements(
            context = context,
            userToken = userTokenValue,
            userInfo = if (userTokenValue == null) UserInfo(
                firstName = "Darwin",
                lastName = "Morocho",
                email = "dmorocho@deuna.com"
            ) else null,
            callbacks = elementsCallbacks(completion)
        )
    }


    private fun elementsCallbacks(
        completion: (ElementsResult) -> Unit,
    ): ElementsCallbacks {
        return ElementsCallbacks().apply {
            onSuccess = { response ->
                deunaSDK.closeElements()
                viewModelScope.launch {
                    completion(
                        ElementsResult.Success(
                            (response["metadata"] as Json)["createdCard"] as Json
                        )
                    )
                }
            }
            onError = { error ->
                deunaSDK.closeElements()
                viewModelScope.launch {
                    completion(ElementsResult.Error(error))
                }
            }
            onCanceled = {
                viewModelScope.launch {
                    completion(ElementsResult.Canceled)
                }
            }
            eventListener = { event, _ ->
                Log.d("DeunaSDK", "on event ${event.value}")
            }
        }
    }
}

