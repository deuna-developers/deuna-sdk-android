package com.deuna.maven

import android.content.Context
import com.deuna.maven.shared.*
import com.deuna.maven.widgets.checkout_widget.CheckoutWidgetDialogFragment
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration

/**
 * Launch the Checkout View
 *
 * @param orderToken The order token that will be used to show the Checkout
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param userToken (Optional) A user authentication token that allows skipping the OTP flow and shows the user's saved cards.
 * @param styleFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom style file
 */
fun DeunaSDK.initCheckout(
    context: Context,
    orderToken: String,
    callbacks: CheckoutCallbacks,
    userToken: String? = null,
    styleFile: String? = null,
    language: String? = null,
    domain: String? = null,
) {
    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }

    dialogFragment = CheckoutWidgetDialogFragment(
        context,
        widgetConfiguration = CheckoutWidgetConfiguration(
            sdkInstance = this,
            orderToken = orderToken,
            callbacks = callbacks,
            userToken = userToken,
            styleFile = styleFile,
            language = language,
            widgetIntegration = WidgetIntegration.MODAL,
            domain = domain,
        )
    )
    dialogFragment?.show()
}