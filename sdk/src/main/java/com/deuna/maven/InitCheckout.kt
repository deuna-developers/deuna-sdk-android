package com.deuna.maven

import android.content.Context
import com.deuna.maven.shared.*
import com.deuna.maven.shared.extensions.findFragmentActivity
import com.deuna.maven.widgets.checkout_widget.CheckoutWidgetDialogFragment
import com.deuna.maven.widgets.checkout_widget.CheckoutEvent
import com.deuna.maven.widgets.checkout_widget.buildCheckoutWidgetUrl

/**
 * Launch the Checkout View
 *
 * @param orderToken The order token that will be used to show the Checkout
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeEvents (Optional) A Set of CheckoutEvent values specifying when to close the checkout activity automatically.
 * @param userToken (Optional) A user authentication token that allows skipping the OTP flow and shows the user's saved cards.
 * @param styleFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom style file
 */
fun DeunaSDK.initCheckout(
    context: Context,
    orderToken: String,
    callbacks: CheckoutCallbacks,
    closeEvents: Set<CheckoutEvent> = emptySet(),
    userToken: String? = null,
    styleFile: String? = null,
    language: String? = null
) {
    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }


    val fragmentActivity = context.findFragmentActivity() ?: return

    dialogFragment = CheckoutWidgetDialogFragment(
        callbacks = callbacks,
        closeEvents = closeEvents
    )
    dialogFragment?.show(fragmentActivity.supportFragmentManager, "CheckoutWidgetDialogFragment")

    buildCheckoutWidgetUrl(
        orderToken = orderToken,
        userToken = userToken,
        styleFile = styleFile,
        language = language,
        widgetIntegration = WidgetIntegration.MODAL
    ) { error, url ->
        if (error != null) {
            callbacks.onError?.invoke(error)
        } else {
            val fragment = dialogFragment;
            if (fragment is CheckoutWidgetDialogFragment) {
                fragment.loadUrl(url!!)
            }
        }
    }
}