package com.deuna.maven

import android.content.Context
import android.content.Intent
import com.deuna.maven.element.domain.*
import com.deuna.maven.shared.*
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.web_views.*
import com.deuna.maven.web_views.base.*
import java.lang.IllegalStateException

/**
 * Launch the Elements View
 *
 * @param userToken The user token
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeEvents (Optional) An array of CheckoutEvent values specifying when to close the elements activity automatically.
 * @param userInfo: (Optional) The basic user information. Pass this parameter if the userToken parameter is null.
 * @param cssFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom CSS file.
 * @throws IllegalStateException if the passed userToken is not valid
 */
fun DeunaSDK.initElements(
    context: Context,
    callbacks: ElementsCallbacks,
    closeEvents: Set<ElementsEvent> = emptySet(),
    userToken: String? = null,
    userInfo: UserInfo? = null,
    cssFile: String? = null
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
        queryParameters[QueryParameters.FIRST_NAME.value] = userInfo.firstName
        queryParameters[QueryParameters.LAST_NAME.value] = userInfo.lastName
        queryParameters[QueryParameters.EMAIL.value] = userInfo.email
    } else {
        // if the user token is not passed or is empty the userInfo must be passed
        DeunaLogs.error(ElementsErrorMessages.MISSING_USER_TOKEN_OR_USER_INFO.message)
        callbacks.onError?.invoke(ElementsErrors.missingUserTokenOrUserInfo)
    }

    if (!cssFile.isNullOrEmpty()) {
        queryParameters[QueryParameters.CSS_FILE.value] = cssFile
    }

    val elementUrl = Utils.buildUrl(baseUrl = "$baseUrl/vault", queryParams = queryParameters)

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
@Deprecated(
    message = "This function will be removed in the future. Use close instead",
    replaceWith = ReplaceWith("close()")
)
fun DeunaSDK.closeElements() {
    close()
}