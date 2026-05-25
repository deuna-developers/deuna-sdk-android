package com.deuna.sdkexample.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.deuna.sdkexample.web_view.ExternalUrlHelper
import com.deuna.sdkexample.web_view.JavascriptMessage
import com.deuna.sdkexample.web_view.WebViewWrapper


@Composable
fun WebViewScreen(url: String) {
    val context = LocalContext.current
    val webViewWrapper = remember {
        WebViewWrapper(context)
    }


    DisposableEffect(webViewWrapper) {
        webViewWrapper.loadUrl(url)
        webViewWrapper.javascriptMessageListener = { message ->
            when (message) {
                is JavascriptMessage.OnError -> {
                    Log.d("WebViewScreen", "OnError: $message")
                }

                is JavascriptMessage.OnEventDispatch -> {
                    Log.d("WebViewScreen", "OnEventDispatch: $message")
                }

                is JavascriptMessage.OnSuccess -> {
                    // Check if the custom chrome tab is open before performing the action
                    // like navigation, showing a dialog, or close the current screen.
                    ExternalUrlHelper.waitUntilChromeTabIsClosed {
                        Log.d("WebViewScreen", "OnSuccess: $message")
                    }
                }
            }
        }
        onDispose {
            webViewWrapper.dispose()
        }
    }

    AndroidView(
        factory = { webViewWrapper },
        modifier = Modifier.fillMaxSize()
    )
}