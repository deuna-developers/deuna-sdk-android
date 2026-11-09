package com.deuna.explore.presentation.screens.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.build
import com.deuna.maven.web_views.deuna.extensions.submit
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration

@Composable
fun EmbeddedScreen(
    widgetConfig: DeunaWidgetConfiguration?,
    showPayNowButton: Boolean,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val widgetRef = remember { mutableStateOf<DeunaWidget?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(0.dp),
        ) {
            if (widgetConfig != null) {
                key(widgetConfig) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            DeunaWidget(context).apply {
                                widgetConfiguration = widgetConfig
                                build()
                                widgetRef.value = this
                            }
                        },
                    )
                }
            }
        }

        if (showPayNowButton) {
            Button(
                onClick = { widgetRef.value?.submit {} },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF147AE8)),
            ) {
                Text("Pay Now")
            }
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
            widgetRef.value?.destroy()
        }
    }
}
