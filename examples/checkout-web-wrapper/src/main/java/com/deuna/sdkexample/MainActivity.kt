package com.deuna.sdkexample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.deuna.sdkexample.screens.WebViewScreen
import com.deuna.sdkexample.web_view.ExternalUrlHelper


class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ExternalUrlHelper.registerForActivityResult(this)

        setContent {
            WebViewScreen(
                url = "https://explore.deuna.io"
            )
        }
    }

}