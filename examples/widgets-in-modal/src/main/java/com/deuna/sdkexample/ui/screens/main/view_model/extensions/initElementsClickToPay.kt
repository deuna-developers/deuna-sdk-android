package com.deuna.sdkexample.ui.screens.main.view_model.extensions

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.deuna.maven.widgets.elements_widget.ElementsError
import com.deuna.maven.initElements
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.ElementsWidget
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.sdkexample.shared.ElementsResult
import com.deuna.sdkexample.ui.screens.main.view_model.DEBUG_TAG
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel
import kotlinx.coroutines.launch

/**
 * Show the widget that processes a payment using Click to Pay.
 */
fun MainViewModel.clickToPay(
    context: Context,
    completion: (ElementsResult) -> Unit,
) {
    // Get domain from environment variable for e2e-preproduction
    // Elements uses elements-link, not checkout-base
    val customDomain = System.getenv("DEUNA_ELEMENTS_LINK_DOMAIN")
    
    deunaSDK.initElements(
        context = context,
        userInfo = UserInfo(
            firstName = "Darwin", lastName = "Morocho", email = "dmorocho+10@deuna.com"
        ),
        types = listOf(
            mapOf(
                "name" to ElementsWidget.CLICK_TO_PAY
            )
        ),
        callbacks = ElementsCallbacks().apply {
            onSuccess = { response ->
                deunaSDK.close()
                viewModelScope.launch {
                    completion(
                        ElementsResult.Success(
                            (response["metadata"] as Json)["createdCard"] as Json
                        )
                    )
                }
            }
            onError = {
                if (it.type == ElementsError.Type.INITIALIZATION_FAILED) {
                    deunaSDK.close()
                    viewModelScope.launch {
                        completion(ElementsResult.Error(it))
                    }
                }
            }
            onEventDispatch = { event, data ->
                Log.d(DEBUG_TAG, "onEventDispatch ${event.name}: $data")
            }
        },
        domain = customDomain  // ← Use elements-link domain for e2e-preproduction
    )
}