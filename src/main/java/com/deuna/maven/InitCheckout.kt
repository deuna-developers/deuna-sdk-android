package com.deuna.maven

import android.content.Context
import android.content.Intent
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.DeunaWebViewActivity
import com.deuna.maven.web_views.widgets.CheckoutActivity

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

    val apiKey = this.publicApiKey
    val baseUrl = this.environment.checkoutBaseUrl
    CheckoutActivity.setCallbacks(sdkInstanceId = sdkInstanceId, callbacks = callbacks)
    val intent = Intent(context, CheckoutActivity::class.java).apply {
        if (!userToken.isNullOrEmpty()) {
            putExtra(CheckoutActivity.EXTRA_USER_TOKEN, userToken)
        }
        if (!styleFile.isNullOrEmpty()) {
            putExtra(CheckoutActivity.EXTRA_STYLE_FILE, styleFile)
        }
        if (!language.isNullOrEmpty()) {
            putExtra(CheckoutActivity.EXTRA_LANGUAGE, language)
        }
        putExtra(CheckoutActivity.EXTRA_ORDER_TOKEN, orderToken)
        putExtra(CheckoutActivity.EXTRA_API_KEY, apiKey)
        putExtra(CheckoutActivity.EXTRA_BASE_URL, baseUrl)
        putExtra(DeunaWebViewActivity.EXTRA_SDK_INSTANCE_ID, sdkInstanceId)
        putStringArrayListExtra(
            DeunaWebViewActivity.EXTRA_CLOSE_EVENTS,
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