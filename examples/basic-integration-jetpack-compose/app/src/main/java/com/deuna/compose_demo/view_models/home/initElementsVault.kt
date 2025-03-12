package com.deuna.compose_demo.view_models.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.deuna.compose_demo.screens.ElementsResult
import com.deuna.maven.ElementsWidgetExperience
import com.deuna.maven.initElements
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.shared.enums.CloseAction
import kotlinx.coroutines.launch

/**
 * Initiates the process of saving the card information.
 * @param context The activity context.
 * @param completion Callback to be invoked upon completion of the card saving process.
 */
fun HomeViewModel.saveCard(
    context: Context,
    completion: (ElementsResult) -> Unit,
) {
    deunaSDK.initElements(
        context = context,
        userToken = userTokenValue,
        userInfo = if (userTokenValue == null) UserInfo(
            firstName = "Darwin", lastName = "Morocho", email = "dmorocho@deuna.com"
        ) else null,
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
            onError = { error ->
                deunaSDK.close()
                viewModelScope.launch {
                    completion(ElementsResult.Error(error))
                }
            }
            onClosed = { action ->
                Log.e(DEBUG_TAG, "closeAction: $action")
                if (action == CloseAction.userAction) { // The operation was canceled
                    viewModelScope.launch {
                        completion(ElementsResult.Canceled)
                    }
                }
            }
            onEventDispatch = { event, data ->
                Log.d(DEBUG_TAG, "onEventDispatch ${event.name}: $data")
            }
        },
        widgetExperience = ElementsWidgetExperience(
            userExperience = ElementsWidgetExperience.UserExperience(
                showSavedCardFlow = false, defaultCardFlow = false
            )
        ),
    )
}


