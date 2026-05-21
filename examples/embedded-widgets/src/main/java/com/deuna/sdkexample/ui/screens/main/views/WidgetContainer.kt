package com.deuna.sdkexample.ui.screens.main.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.build
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration


@Composable
fun WidgetContainer(
    modifier: Modifier,
    config: DeunaWidgetConfiguration?,
    onWidgetCreated: (DeunaWidget) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val widgetRef = remember { mutableStateOf<DeunaWidget?>(null) }

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
                        widgetRef.value = this
                        onWidgetCreated(this)
                    }
                }
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> widgetRef.value?.resume()
                Lifecycle.Event.ON_STOP -> widgetRef.value?.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
