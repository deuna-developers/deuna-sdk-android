package com.deuna.maven

import android.content.Context
import com.deuna.maven.widgets.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration
import com.deuna.maven.widgets.payment_widget.PaymentWidgetDialogFragment

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
    language: String? = null,
    behavior: Json? = null,
) {

    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }


    dialogFragment = PaymentWidgetDialogFragment(
        context,
        widgetConfiguration = PaymentWidgetConfiguration(
            sdkInstance = this,
            orderToken = orderToken,
            callbacks = callbacks,
            userToken = userToken,
            styleFile = styleFile,
            paymentMethods = paymentMethods,
            checkoutModules = checkoutModules,
            language = language,
            behavior = behavior,
            widgetIntegration = WidgetIntegration.MODAL
        )
    )
    dialogFragment?.show()
}

