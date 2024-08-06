package com.deuna.maven

import android.content.Context
import android.content.Intent
import com.deuna.maven.element.domain.*
import com.deuna.maven.shared.*
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.web_views.*
import com.deuna.maven.web_views.base.*
import java.lang.IllegalStateException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Launch the Elements View
 *
 * @param userToken The user token
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeEvents (Optional) An array of CheckoutEvent values specifying when to close the elements activity automatically.
 * @param userInfo: (Optional) The basic user information. Pass this parameter if the userToken parameter is null.
 *
 * @throws IllegalStateException if the passed userToken is not valid
 */
fun DeunaSDK.initElements(
    context: Context,
    callbacks: ElementsCallbacks,
    closeEvents: Set<ElementsEvent> = emptySet(),
    userToken: String? = null,
    userInfo: UserInfo? = null
) {
    val baseUrl = this.environment.elementsBaseUrl

    ElementsActivity.setCallbacks(sdkInstanceId = sdkInstanceId, callbacks = callbacks)

    val queryParameters = mutableMapOf<String, String>()
    queryParameters[QueryParameters.MODE.value] = QueryParameters.WIDGET.value
    queryParameters[QueryParameters.PUBLIC_API_KEY.value] = publicApiKey

    if (!userToken.isNullOrEmpty()) {
        queryParameters[QueryParameters.USER_TOKEN.value] = userToken
    } else if (userInfo != null) {
        if (!userInfo.isValid()) {
            // if the user token is not passed or is empty the userInfo must be passed
            DeunaLogs.error(ElementsErrorMessages.INVALID_USER_INFO.message)
            callbacks.onError?.invoke(ElementsErrors.invalidUserInfo)
            return
        }
        queryParameters[QueryParameters.FIRST_NAME.value] = URLEncoder.encode(
            userInfo.firstName, StandardCharsets.UTF_8.toString()
        )
        queryParameters[QueryParameters.LAST_NAME.value] = URLEncoder.encode(
            userInfo.lastName, StandardCharsets.UTF_8.toString()
        )
    } else {
        // if the user token is not passed or is empty the userInfo must be passed
        DeunaLogs.error(ElementsErrorMessages.MISSING_USER_TOKEN_OR_USER_INFO.message)
        callbacks.onError?.invoke(ElementsErrors.missingUserTokenOrUserInfo)
    }

    var elementUrl = Utils.buildUrl(baseUrl = "$baseUrl/vault", queryParams = queryParameters)

    if (userInfo != null) {
        elementUrl += "&${QueryParameters.EMAIL.value}=${
            URLEncoder.encode(
                userInfo.email, StandardCharsets.UTF_8.toString()
            )
        }"
    }

    val intent = Intent(context, ElementsActivity::class.java).apply {
        putExtra(ElementsActivity.EXTRA_URL, elementUrl)
        putExtra(BaseWebViewActivity.EXTRA_SDK_INSTANCE_ID, sdkInstanceId)
        putStringArrayListExtra(
            BaseWebViewActivity.EXTRA_CLOSE_EVENTS,
            ArrayList(closeEvents.map { it.name })
        )
    }
    context.startActivity(intent)
}


/**
 * Closes the elements activity if it's currently running.
 */
fun DeunaSDK.closeElements() {
    closeElements(sdkInstanceId = sdkInstanceId)
}

/**
 * Global function used to send a broadcast event to close the elements view
 */
fun closeElements(sdkInstanceId: Int) {
    BaseWebViewActivity.closeWebView(sdkInstanceId)
}