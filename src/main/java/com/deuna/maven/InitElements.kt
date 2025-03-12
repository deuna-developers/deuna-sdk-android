package com.deuna.maven

import android.content.Context
import android.content.Intent
import com.deuna.maven.element.domain.*
import com.deuna.maven.shared.*
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.web_views.DeunaWebViewActivity
import com.deuna.maven.web_views.widgets.ElementsActivity
import java.lang.IllegalStateException


class ElementsWidgetExperience(val userExperience: UserExperience) {
    class UserExperience(
        val showSavedCardFlow: Boolean? = null,
        val defaultCardFlow: Boolean? = null,
    )
}

/**
 * Launch the Elements View
 *
 * @param userToken The user token
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeEvents (Optional) An array of CheckoutEvent values specifying when to close the elements activity automatically.
 * @param userInfo: (Optional) The basic user information. Pass this parameter if the userToken parameter is null.
 * @param styleFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom style file.
 * @param types (Optional) A list of the widgets to be rendered.
 * Example:
 * ```
 * types = listOf(
 *    mapOf( "name" to ElementsWidget.VAULT)
 * )
 * ```
 * @param orderToken (Optional) The orderToken is a unique token generated for the payment order. This token is generated through the DEUNA API and you must implement the corresponding endpoint in your backend to obtain this information.
 * @param widgetExperience (Optional)  An instance of ElementsWidgetExperience that contains a custom configurations for the widget.
 *  The currently supported configurations are:
 *   - `userExperience.showSavedCardFlow`: (Bool) Shows the saved cards toggle.
 *   - `userExperience.defaultCardFlow`: (Bool) Shows the toggle to save the card as default.
 * @throws IllegalStateException if the passed userToken is not valid
 */
fun DeunaSDK.initElements(
    context: Context,
    callbacks: ElementsCallbacks,
    closeEvents: Set<ElementsEvent> = emptySet(),
    userToken: String? = null,
    userInfo: UserInfo? = null,
    styleFile: String? = null,
    types: List<Json> = emptyList(),
    language: String? = null,
    orderToken: String? = null,
    widgetExperience: ElementsWidgetExperience? = null
) {
    val baseUrl = this.environment.elementsBaseUrl

    ElementsActivity.setCallbacks(sdkInstanceId = sdkInstanceId, callbacks = callbacks)

    val queryParameters = mutableMapOf(
        QueryParameters.MODE to QueryParameters.WIDGET,
        QueryParameters.PUBLIC_API_KEY to publicApiKey
    )

    when {
        !userToken.isNullOrEmpty() -> queryParameters[QueryParameters.USER_TOKEN] = userToken
        userInfo != null && userInfo.isValid() -> {
            queryParameters.apply {
                put(QueryParameters.FIRST_NAME, userInfo.firstName)
                put(QueryParameters.LAST_NAME, userInfo.lastName)
                put(QueryParameters.EMAIL, userInfo.email)
            }
        }

        else -> {
            DeunaLogs.error(ElementsErrorMessages.MISSING_USER_TOKEN_OR_USER_INFO)
            callbacks.onError?.invoke(ElementsErrors.missingUserTokenOrUserInfo)
            return
        }
    }

    if (!language.isNullOrEmpty()) {
        queryParameters[QueryParameters.LANGUAGE] = language
    }

    if (!orderToken.isNullOrEmpty()) {
        queryParameters[QueryParameters.ORDER_TOKEN] = orderToken
    }

    widgetExperience?.let {
        it.userExperience.showSavedCardFlow?.let { value ->
            queryParameters[QueryParameters.SHOW_SAVED_CARD_FLOW] = "$value"
        }
        it.userExperience.defaultCardFlow?.let { value ->
            queryParameters[QueryParameters.DEFAULT_CARD_FLOW] = "$value"
        }
    }

    styleFile?.let {
        queryParameters[QueryParameters.CSS_FILE] = it // should be removed in future versions
        queryParameters[QueryParameters.STYLE_FILE] = it
    }

    // Construct the base URL for elements and the URL string
    // by default the VAULT widget is showed if the types list is empty
    val widgetName =
        types.firstOrNull()?.get(ElementsTypeKey.NAME)?.takeIf { it is String && it.isNotEmpty() }
            ?: ElementsWidget.VAULT

    val elementUrl = Utils.buildUrl(baseUrl = "$baseUrl/$widgetName", queryParams = queryParameters)

    val intent = Intent(context, ElementsActivity::class.java).apply {
        putExtra(ElementsActivity.EXTRA_URL, elementUrl)
        putExtra(DeunaWebViewActivity.EXTRA_SDK_INSTANCE_ID, sdkInstanceId)
        putStringArrayListExtra(
            DeunaWebViewActivity.EXTRA_CLOSE_EVENTS, ArrayList(closeEvents.map { it.name })
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