package com.deuna.sdkexample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Environment
import com.deuna.maven.web_views.ExternalUrlHelper
import com.deuna.sdkexample.navigation.AppNavigation

/**
 * Change this values to try the DEUNA SDK
 */
object Constants {
    val DEUNA_ENV: Environment = Environment.SANDBOX
    const val DEUNA_API_KEY: String = "YOUR_PUBLIC_API_KEY"
}

class MainActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_DEUNA_ENV = "DEUNA_ENV"
        const val EXTRA_DEUNA_API_KEY = "DEUNA_API_KEY"
        const val EXTRA_ORDER_TOKEN = "ORDER_TOKEN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Register for activity result callbacks, needed to wait until the custom chrome tab is closed
         */
        ExternalUrlHelper.registerForActivityResult(this)

        val initialOrderToken = intent.getStringExtra(EXTRA_ORDER_TOKEN)

        val deunaSDK = DeunaSDK(
            environment = getEnvironment() ?: Constants.DEUNA_ENV,
            publicApiKey = getApiKey() ?: Constants.DEUNA_API_KEY
        )

        setContent {
            AppNavigation(
                deunaSDK = deunaSDK,
                initialOrderToken = initialOrderToken
            )
        }
    }

    /**
     * For Integration testing get environment from intent extras
     */
    private fun getEnvironment(): Environment? {
        val envString = intent.getStringExtra(EXTRA_DEUNA_ENV) ?: return null
        return when (envString.lowercase()) {
            "development" -> Environment.DEVELOPMENT
            "production" -> Environment.PRODUCTION
            "staging" -> Environment.STAGING
            "sandbox" -> Environment.SANDBOX
            else -> null
        }
    }

    /**
     * For Integration testing get public api key from intent extras
     */
    private fun getApiKey(): String? {
        return intent.getStringExtra(EXTRA_DEUNA_API_KEY)
    }
}