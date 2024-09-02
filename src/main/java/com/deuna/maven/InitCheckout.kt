package com.deuna.maven

import android.content.Context
import android.content.Intent
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.*
import com.deuna.maven.web_views.base.*

/**
 * Launch the Checkout View
 *
 * @param orderToken The order token that will be used to show the Checkout
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeEvents (Optional) A Set of CheckoutEvent values specifying when to close the checkout activity automatically.
 * @param userToken (Optional) A user authentication token that allows skipping the OTP flow and shows the user's saved cards.
 * @param cssFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom CSS file
 */
fun DeunaSDK.initCheckout(
    context: Context,
    orderToken: String,
    callbacks: CheckoutCallbacks,
    closeEvents: Set<CheckoutEvent> = emptySet(),
    userToken: String? = null,
    cssFile: String? = null,
) {
    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }

    val apiKey = this.publicApiKey
    val baseUrl = this.environment.checkoutBaseUrl
    CheckoutActivity.setCallbacks(sdkInstanceId = sdkInstanceId, callbacks = callbacks)
    val intent = Intent(context, CheckoutActivity::class.java).apply {
        if (!userToken.isNullOrEmpty()) {
            putExtra(CheckoutActivity.EXTRA_USER_TOKEN, userToken)
        }
        if (!cssFile.isNullOrEmpty()) {
            putExtra(CheckoutActivity.EXTRA_CSS_FILE, cssFile)
        }
        putExtra(CheckoutActivity.EXTRA_ORDER_TOKEN, orderToken)
        putExtra(CheckoutActivity.EXTRA_API_KEY, apiKey)
        putExtra(CheckoutActivity.EXTRA_BASE_URL, baseUrl)
        putExtra(BaseWebViewActivity.EXTRA_SDK_INSTANCE_ID, sdkInstanceId)
        putStringArrayListExtra(
            BaseWebViewActivity.EXTRA_CLOSE_EVENTS,
            ArrayList(closeEvents.map { it.name })
        )
    }
    context.startActivity(intent)
}

/**
 * Closes the checkout activity if it's currently running.
 */
@Deprecated(
    message = "This function will be removed in the future. Use close instead",
    replaceWith = ReplaceWith("close()")
)
fun DeunaSDK.closeCheckout() {
    close()
}