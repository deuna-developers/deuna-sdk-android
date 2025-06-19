package com.deuna.maven.widgets.configuration

import com.deuna.maven.DeunaSDK
import com.deuna.maven.ElementsWidgetExperience
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.ElementsErrorMessages
import com.deuna.maven.shared.ElementsTypeKey
import com.deuna.maven.shared.ElementsWidget
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.shared.toBase64

class ElementsWidgetConfiguration(
    sdkInstance: DeunaSDK,
    hidePayButton: Boolean = false,
    val callbacks: ElementsCallbacks,
    val userToken: String? = null,
    val userInfo: UserInfo? = null,
    val styleFile: String? = null,
    val types: List<Json> = emptyList(),
    val language: String? = null,
    val orderToken: String? = null,
    val behavior: Json? = null,
    val widgetExperience: ElementsWidgetExperience? = null,
    val widgetIntegration: WidgetIntegration = WidgetIntegration.EMBEDDED,
) : DeunaWidgetConfiguration(
    sdkInstance = sdkInstance,
    hidePayButton = hidePayButton,
) {
    override val link: String
        get() {
            val baseUrl = sdkInstance.environment.elementsBaseUrl

            val queryParameters = mutableMapOf(
                QueryParameters.MODE to QueryParameters.WIDGET,
                QueryParameters.PUBLIC_API_KEY to sdkInstance.publicApiKey,
                QueryParameters.INT to widgetIntegration.value
            )

            when {
                !userToken.isNullOrEmpty() -> queryParameters[QueryParameters.USER_TOKEN] =
                    userToken

                userInfo != null && userInfo.isValid() -> {
                    queryParameters.apply {
                        put(QueryParameters.FIRST_NAME, userInfo.firstName)
                        put(QueryParameters.LAST_NAME, userInfo.lastName)
                        put(QueryParameters.EMAIL, userInfo.email)
                    }
                }

                else -> {
                    DeunaLogs.error(ElementsErrorMessages.MISSING_USER_TOKEN_OR_USER_INFO)
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
                queryParameters[QueryParameters.CSS_FILE] =
                    it // should be removed in future versions
                queryParameters[QueryParameters.STYLE_FILE] = it
            }


            val xpropsB64 = mutableMapOf<String, Any>()

            behavior?.let {
                if (it.keys.isNotEmpty()) {
                    xpropsB64[QueryParameters.BEHAVIOR] = it
                }
            }

            queryParameters[QueryParameters.XPROPS_B64] = xpropsB64.toBase64()


            // Construct the base URL for elements and the URL string
            // by default the VAULT widget is showed if the types list is empty
            val widgetName =
                types.firstOrNull()?.get(ElementsTypeKey.NAME)
                    ?.takeIf { it is String && it.isNotEmpty() }
                    ?: ElementsWidget.VAULT

            return Utils.buildUrl(baseUrl = "$baseUrl/$widgetName", queryParams = queryParameters)
        }
}

