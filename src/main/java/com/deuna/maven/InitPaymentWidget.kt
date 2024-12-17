package com.deuna.maven

import android.content.Context
import android.content.Intent
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.toBase64
import com.deuna.maven.web_views.widgets.PaymentWidgetActivity
import com.deuna.maven.web_views.DeunaWebViewActivity
import org.json.JSONObject

/**
 * Launch the payment widget View
 *
 * @param orderToken The order token that will be used to show the payment widget
 * @param context The application or activity context
 * @param callbacks An instance of PaymentWidgetCallbacks to receive event notifications.
 * @param userToken (Optional) A user authentication token that allows skipping the OTP flow and shows the user's saved cards.
 * @param styleFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom style file.
 * @param paymentMethods (Optional) A list of allowed payment methods. This parameter determines what type of widget should be rendered.
 * @param checkoutModules (Optional) A list  display the payment widget with new patterns or with different functionalities
 */
fun DeunaSDK.initPaymentWidget(
    context: Context,
    orderToken: String,
    callbacks: PaymentWidgetCallbacks,
    userToken: String? = null,
    styleFile: String? = null,
    paymentMethods: List<Json> = emptyList(),
    checkoutModules: List<Json> = emptyList(),
) {

    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }

    val baseUrl = this.environment.paymentWidgetBaseUrl

    PaymentWidgetActivity.setCallbacks(sdkInstanceId = sdkInstanceId, callbacks = callbacks)

    val queryParameters = mutableMapOf<String, String>()
    queryParameters[QueryParameters.MODE] = QueryParameters.WIDGET

    if (!userToken.isNullOrEmpty()) {
        queryParameters[QueryParameters.USER_TOKEN] = userToken
    }

    if (!styleFile.isNullOrEmpty()) {
        queryParameters[QueryParameters.STYLE_FILE] = styleFile
    }

    val xpropsB64 = mutableMapOf<String, Any>()
    xpropsB64[QueryParameters.PUBLIC_API_KEY] = publicApiKey


    if (paymentMethods.isNotEmpty()) {
        xpropsB64[QueryParameters.PAYMENT_METHODS] = paymentMethods
    }

    if (checkoutModules.isNotEmpty()) {
        xpropsB64[QueryParameters.CHECKOUT_MODULES] = checkoutModules
    }

    queryParameters[QueryParameters.XPROPS_B64] = xpropsB64.toBase64()

    val paymentUrl = Utils.buildUrl(
        baseUrl = "$baseUrl/now/$orderToken",
        queryParams = queryParameters,
    )

    val intent = Intent(context, PaymentWidgetActivity::class.java).apply {
        putExtra(PaymentWidgetActivity.EXTRA_URL, paymentUrl)
        putExtra(DeunaWebViewActivity.EXTRA_SDK_INSTANCE_ID, sdkInstanceId)
    }
    context.startActivity(intent)
}

/**
 * Sends a re-fetch order request and handles the response.
 *
 * @param callback A callback function to be invoked when the request completes. The callback receives a `Json` object containing the order data or `null` if the request fails.
 */
fun DeunaSDK.refetchOrder(callback: (Json?) -> Unit) {
    PaymentWidgetActivity.refetchOrder(sdkInstanceId = sdkInstanceId, callback = callback);
}

/**
 * Set custom css on the payment widget.
 * This function must be only called inside the onCardBinDetected callback
 *
 * @param data The JSON data to update the payment widget UI
 */
@Deprecated(
    message = "This function will be removed in the future. Use setCustomStyle instead",
    replaceWith = ReplaceWith("setCustomStyle(data)")
)
fun DeunaSDK.setCustomCss(data: Map<String, Any>) {
    PaymentWidgetActivity.sendCustomCss(
        sdkInstanceId = sdkInstanceId, dataAsJsonString = JSONObject(data).toString()
    )
}


/**
 * Closes the payment widget if it's currently running.
 *
 */
@Deprecated(
    message = "This function will be removed in the future. Use close instead",
    replaceWith = ReplaceWith("close()")
)
fun DeunaSDK.closePaymentWidget() {
    close()
}