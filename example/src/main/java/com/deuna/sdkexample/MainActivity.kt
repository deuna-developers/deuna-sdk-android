package com.deuna.sdkexample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Environment
import com.deuna.sdkexample.navigation.AppNavigation

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deunaSDK = DeunaSDK(
            environment = Environment.SANDBOX,
            publicApiKey = "YOUR_PUBLIC_API_KEY"
        )

        setContent {
            AppNavigation(
                deunaSDK = deunaSDK
            )
        }
    }
}