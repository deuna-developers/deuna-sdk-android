package com.deuna.maven.widgets.elements_widget

import android.content.Context
import com.deuna.maven.web_views.dialog_fragments.base.DeunaDialogFragment
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration

class ElementsWidgetDialogFragment(
    context: Context,
    widgetConfiguration: ElementsWidgetConfiguration,
) : DeunaDialogFragment(
    context,
    widgetConfiguration = widgetConfiguration
)