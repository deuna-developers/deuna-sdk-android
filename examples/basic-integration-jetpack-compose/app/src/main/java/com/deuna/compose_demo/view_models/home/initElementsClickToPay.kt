package com.deuna.compose_demo.view_models.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.deuna.compose_demo.screens.ElementsResult
import com.deuna.maven.element.domain.ElementsError
import com.deuna.maven.initElements
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.ElementsWidget
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import kotlinx.coroutines.launch

/**
 * Show the widget that processes a payment using Click to Pay.
 */
fun HomeViewModel.clickToPay(
    context: Context,
    completion: (ElementsResult) -> Unit,
) {
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
            onSuccess = {
                deunaSDK.close()
                viewModelScope.launch {
                    completion(ElementsResult.Success(it["metadata"] as Json))
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
    )
}