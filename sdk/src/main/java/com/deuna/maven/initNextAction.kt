package com.deuna.maven

import android.content.Context
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.WidgetIntegration
import com.deuna.maven.shared.extensions.findFragmentActivity
import com.deuna.maven.widgets.configuration.NextActionWidgetConfiguration
import com.deuna.maven.widgets.next_action.NextActionCallbacks
import com.deuna.maven.widgets.next_action.NextActionDialogFragment

fun DeunaSDK.initNextAction(
    context: Context,
    orderToken: String,
    callbacks: NextActionCallbacks,
    language: String? = null,
) {
    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }

    val fragmentActivity = context.findFragmentActivity() ?: return

    dialogFragment = NextActionDialogFragment(
        widgetConfiguration = NextActionWidgetConfiguration(
            sdkInstance = this,
            orderToken = orderToken,
            language = language,
            widgetIntegration = WidgetIntegration.MODAL,
            callbacks = callbacks
        )
    )
    dialogFragment?.show(fragmentActivity.supportFragmentManager, "NextActionDialogFragment")
}