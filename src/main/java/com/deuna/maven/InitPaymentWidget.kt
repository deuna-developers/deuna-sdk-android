package com.deuna.maven

import android.content.Context
import android.content.Intent
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.web_views.PaymentWidgetActivity
import com.deuna.maven.web_views.base.BaseWebViewActivity
import org.json.JSONObject

/**
 * Launch the payment widget View
 *
 * @param orderToken The order token that will be used to show the payment widget
 * @param context The application or activity context
 * @param callbacks An instance of PaymentWidgetCallbacks to receive event notifications.
 * @param userToken (Optional) A user authentication token that allows skipping the OTP flow and shows the user's saved cards.
 * @param cssFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom CSS file.
 */
fun DeunaSDK.initPaymentWidget(
    context: Context,
    orderToken: String,
    callbacks: PaymentWidgetCallbacks,
    userToken: String? = null,
    cssFile: String? = null
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
    queryParameters[QueryParameters.MODE.value] = QueryParameters.WIDGET.value

    if (!userToken.isNullOrEmpty()) {
        queryParameters[QueryParameters.USER_TOKEN.value] = userToken
    }

    if (!cssFile.isNullOrEmpty()) {
        queryParameters[QueryParameters.CSS_FILE.value] = cssFile
    }

    val paymentUrl = Utils.buildUrl(
        baseUrl = "$baseUrl/now/$orderToken",
        queryParams = queryParameters,
    )

    val intent = Intent(context, PaymentWidgetActivity::class.java).apply {
        putExtra(PaymentWidgetActivity.EXTRA_URL, paymentUrl)
        putExtra(BaseWebViewActivity.EXTRA_SDK_INSTANCE_ID, sdkInstanceId)
    }
    context.startActivity(intent)
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
 * Set custom style on the payment widget.
 * This function must be only called inside the next callbacks onCardBinDetected or onInstallmentSelected.
 *
 * @param data The JSON data to update the payment widget UI
 */
fun DeunaSDK.setCustomStyle(data: Map<String, Any>) {
    PaymentWidgetActivity.sendCustomStyle(
        sdkInstanceId = sdkInstanceId, dataAsJsonString = JSONObject(data).toString()
    )
}


/**
 * Closes the payment widget if it's currently running.
 *
 */
fun DeunaSDK.closePaymentWidget() {
    com.deuna.maven.closePaymentWidget(sdkInstanceId = sdkInstanceId)
}

/**
 * Global function used to send a broadcast event to close the payment widget view
 */
fun closePaymentWidget(sdkInstanceId: Int) {
    BaseWebViewActivity.closeWebView(sdkInstanceId)
}