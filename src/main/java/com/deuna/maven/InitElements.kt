package com.deuna.maven

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.deuna.maven.element.domain.*
import com.deuna.maven.shared.*
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
 *
 * @throws IllegalStateException if the passed userToken is not valid
 */
fun DeunaSDK.initElements(
    context: Context,
    userToken: String,
    callbacks: ElementsCallbacks,
    closeEvents: Set<ElementsEvent> = emptySet(),
) {
    if (userToken.isEmpty()) {
        callbacks.onError?.invoke(
            ElementsError(ElementsErrorType.INVALID_USER_TOKEN, null),
        )
        return
    }

    val apiKey = this.publicApiKey
    val baseUrl = this.environment.elementsBaseUrl

    ElementsActivity.setCallbacks(callbacks)

    val elementUrl = Uri.parse("$baseUrl/vault")
        .buildUpon()
        .appendQueryParameter("userToken", userToken)
        .appendQueryParameter("publicApiKey", apiKey)
        .appendQueryParameter("mode", "widget")
        .build().toString()


    val intent = Intent(context, ElementsActivity::class.java).apply {
        putExtra(ElementsActivity.EXTRA_URL, elementUrl)
        putStringArrayListExtra(
            BaseWebViewActivity.EXTRA_CLOSE_EVENTS,
            ArrayList(closeEvents.map { it.name })
        )
    }
    context.startActivity(intent)
}


/**
 * Closes the elements activity if it's currently running.
 *
 * @param context The application or activity context
 */
fun DeunaSDK.closeElements(context: Context) {
    closeElements()
}

/**
 * Global function used to send a broadcast event to close the elements view
 */
fun closeElements() {
    BaseWebViewActivity.closeWebView()
}