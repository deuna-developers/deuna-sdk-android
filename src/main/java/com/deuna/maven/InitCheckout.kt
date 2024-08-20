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
 */
fun DeunaSDK.initCheckout(
    context: Context,
    orderToken: String,
    callbacks: CheckoutCallbacks,
    closeEvents: Set<CheckoutEvent> = emptySet(),
    userToken: String? = null
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
fun DeunaSDK.closeCheckout() {
    closeCheckout(sdkInstanceId = sdkInstanceId)
}

/**
 * Global function used to send a broadcast event to close the checkout view
 */
fun closeCheckout(sdkInstanceId: Int) {
    BaseWebViewActivity.closeWebView(sdkInstanceId)
}