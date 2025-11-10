package com.deuna.maven.web_views.deuna.extensions

import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration

/**
 * Loads the URL in the WebView to display the DEUNA widget
 */
fun DeunaWidget.build() {
    widgetConfiguration?.let {

        it.fraudCredentials?.let { fraudCredentials ->
            setFraudCredentials(fraudCredentials)
        }

        when (it) {
            is CheckoutWidgetConfiguration -> {
                it.getPaymentLink { error, _ ->
                    if (error != null) {
                        it.callbacks.onError?.invoke(error)
                    } else {
                        launch(it.link)
                    }
                }
            }

            else -> launch(it.link)
        }
    }
}