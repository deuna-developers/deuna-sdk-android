package com.deuna.explore.presentation.screens.widgets

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.build
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoResizeScreen(
    widgetConfig: DeunaWidgetConfiguration?,
    onBack: () -> Unit,
    onReload: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val widgetRef = remember { mutableStateOf<DeunaWidget?>(null) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(context) {
        val activity = context as? android.app.Activity
        val rootView = activity?.window?.decorView
        if (rootView == null) return@DisposableEffect onDispose {}
        val rect = Rect()
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            rootView.getWindowVisibleDisplayFrame(rect)
            val keyboardHeight = rootView.height - rect.bottom
            if (keyboardHeight > 150) {
                widgetRef.value?.onKeyboardHeightChanged(keyboardHeight)
            }
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Resize") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onReload) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(0.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Upper content",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (widgetConfig != null) {
                key(widgetConfig) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth(),
                        factory = { context ->
                            DeunaWidget(context).apply {
                                widgetConfiguration = widgetConfig
                                build()
                                widgetRef.value = this
                            }
                        },
                        update = { widget ->
                            widget.setOnScrollByCallback { amountPx ->
                                coroutineScope.launch {
                                    scrollState.animateScrollBy(amountPx)
                                }
                            }
                        },
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(0.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Lower content",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
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
