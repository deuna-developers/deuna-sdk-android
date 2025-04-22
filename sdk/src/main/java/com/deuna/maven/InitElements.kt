package com.deuna.maven

import android.content.Context
import com.deuna.maven.shared.*
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.shared.extensions.findFragmentActivity
import com.deuna.maven.widgets.elements_widget.ElementsWidgetDialogFragment
import com.deuna.maven.widgets.elements_widget.ElementsEvent
import com.deuna.maven.widgets.elements_widget.buildElementsWidgetUrl
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
    behavior: Json? = null,
    widgetExperience: ElementsWidgetExperience? = null
) {

    val elementUrl = buildElementsWidgetUrl(
        userToken = userToken,
        userInfo = userInfo,
        styleFile = styleFile,
        types = types,
        language = language,
        orderToken = orderToken,
        widgetExperience = widgetExperience,
        behavior = behavior,
        widgetIntegration = WidgetIntegration.MODAL,
    )

    val fragmentActivity = context.findFragmentActivity() ?: return

    dialogFragment = ElementsWidgetDialogFragment(
        url = elementUrl,
        callbacks = callbacks,
        closeEvents = closeEvents
    )
    dialogFragment?.show(fragmentActivity.supportFragmentManager, "ElementsWidgetDialogFragment")
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