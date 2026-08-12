package com.deuna.maven.internal.modal

import android.content.Context
import android.content.Intent
import com.deuna.maven.web_views.activities.DeunaWidgetActivity
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration

internal object DeunaWidgetModalLauncher {
    fun launch(
        context: Context,
        widgetConfiguration: DeunaWidgetConfiguration,
    ) {
        val id = DeunaWidgetModalRegistry.register(widgetConfiguration)

        val intent = Intent(context, DeunaWidgetActivity::class.java).apply {
            putExtra(DeunaWidgetActivity.EXTRA_WIDGET_CONFIG_ID, id)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (context !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        context.startActivity(intent)
    }
}
