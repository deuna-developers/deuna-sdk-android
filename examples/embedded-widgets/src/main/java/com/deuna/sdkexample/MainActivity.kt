package com.deuna.sdkexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Environment
import com.deuna.maven.web_views.ExternalUrlHelper
import com.deuna.sdkexample.navigation.AppNavigation

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Register for activity result callbacks, needed to wait until the custom chrome tab is closed
         */

        ExternalUrlHelper.registerForActivityResult(this)

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