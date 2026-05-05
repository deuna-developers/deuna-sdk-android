package com.deuna.sdkexample.ui.screens.main.view_model.extensions

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.deuna.maven.ElementsWidgetExperience
import com.deuna.maven.initElements
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.sdkexample.shared.ElementsResult
import com.deuna.sdkexample.ui.screens.main.view_model.DEBUG_TAG
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel
import kotlinx.coroutines.launch

/**
 * Initiates the process of saving the card information.
 * @param context The activity context.
 * @param completion Callback to be invoked upon completion of the card saving process.
 */
fun MainViewModel.saveCard(
    context: Context,
    completion: (ElementsResult) -> Unit,
) {
    // Get domain from environment variable for e2e-preproduction
    // Elements uses elements-link, not checkout-base
    val customDomain = System.getenv("DEUNA_ELEMENTS_LINK_DOMAIN")?.takeIf { it.isNotBlank() }

    deunaSDK.initElements(
        context = context,
        userToken = userToken,
        orderToken = orderToken,
        userInfo = UserInfo(
            firstName = "Darwin", lastName = "Morocho", email = "3797270.qa@deuna.com"
        ),
//        behavior = mapOf(
//            "paymentMethods" to mapOf(
//                "creditCard" to mapOf(
//                    "splitPayments" to mapOf(
//                        "maxCards" to 2
//                    ),
//                    "flow" to "purchase"
//                )
//            )
//        ),
        callbacks = ElementsCallbacks().apply {
            onSuccess = { response ->
                deunaSDK.close()
                viewModelScope.launch {
                    Log.d(
                        "✅ DeunaSDK: user_agent",
                        response["user_agent"] as String? ?: "null"
                    )
                    Log.d(
                        "✅ DeunaSDK: fraud_id",
                        response["fraud_id"] as String? ?: "null"
                    )
                    completion(
                        ElementsResult.Success(
                            (response["metadata"] as Json)["createdCard"] as Json
                        )
                    )
                }
            }
            onError = { error ->
                deunaSDK.close()
                viewModelScope.launch {
                    completion(ElementsResult.Error(error))
                }
            }
            onClosed = { action ->
                Log.i(DEBUG_TAG, "closeAction: $action")
                if (action == CloseAction.userAction) { // The operation was canceled
                    viewModelScope.launch {
                        completion(ElementsResult.Canceled)
                    }
                }
            }
            onEventDispatch = { event, data ->
                Log.d(DEBUG_TAG, "onEventDispatch ${event.name}: $data")
            }
            onInstallmentSelected = { metadata ->
                logElementsMetadata(
                    eventName = "onInstallmentSelected",
                    metadata = metadata,
                    keys = listOf("cardBin", "installments")
                )
            }
            onCardBinDetected = { metadata ->
                logElementsMetadata(
                    eventName = "onCardBinDetected",
                    metadata = metadata,
                    keys = listOf("cardBin", "cardBrand")
                )
            }
        },
        widgetExperience = ElementsWidgetExperience(
            userExperience = ElementsWidgetExperience.UserExperience(
                showSavedCardFlow = false, defaultCardFlow = false
            )
        ),
        domain = customDomain  // ← Use elements-link domain for e2e-preproduction
    )
}

private fun logElementsMetadata(
    eventName: String,
    metadata: Json?,
    keys: List<String>
) {
    if (metadata.isNullOrEmpty()) {
        Log.d(DEBUG_TAG, "$eventName metadata is null or empty")
        return
    }

    val content = keys.joinToString(separator = ", ") { key ->
        "$key=${metadata[key] ?: "null"}"
    }
    Log.d(DEBUG_TAG, "$eventName metadata: $content")
}
