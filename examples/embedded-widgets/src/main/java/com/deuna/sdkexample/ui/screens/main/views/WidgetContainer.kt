package com.deuna.sdkexample.ui.screens.main.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.build
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration


@Composable
fun WidgetContainer(
    modifier: Modifier,
    config: DeunaWidgetConfiguration?,
    onWidgetCreated: (DeunaWidget) -> Unit
) {


    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (config != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    DeunaWidget(context).apply {
                        this.widgetConfiguration = config
                        this.build()
                        onWidgetCreated(this)
                    }
                }
            )
        }
    }
}