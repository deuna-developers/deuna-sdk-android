package com.deuna.sdkexample.web_view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import com.deuna.sdkexample.extensions.findActivity

class ExternalUrlHelper {

    companion object {
        private var isChromeTabOpen = false
        private var listeners = mutableSetOf<() -> Unit>()
        private var chromeTabLauncher: ActivityResultLauncher<Intent>? = null

        /**
         * Registers an activity result launcher for custom tabs.
         * This is needed to wait until the custom chrome tab is closed.
         */
        fun registerForActivityResult(
            context: Context
        ) {
            val activity = context.findActivity() ?: return
            chromeTabLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { _ ->
                isChromeTabOpen = false
                if (!activity.isDestroyed) {
                    listeners.forEach {
                        it.invoke()
                        listeners.remove(it)
                    }
                }
            }
        }


        /**
         * Opens a url in a custom chrome tab.
         */
        fun openUrl(url: String) {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            val intent = customTabsIntent.intent.apply {
                data = Uri.parse(url)
            }

            chromeTabLauncher?.launch(intent) ?: run {
                Log.e(
                    "ExternalUrlHelper",
                    "chromeTabLauncher is null - did you call registerForActivityResult()?"
                )
            }
            isChromeTabOpen = true
        }


        /**
         * Waits until the custom chrome tab is closed.
         * IMPORTANT: This function must be called before UI updates (Navigation, Dialogs) to prevent app crashes.
         * @param cb The callback to be invoked when the custom chrome tab is closed.
         */
        fun waitUntilChromeTabIsClosed(
            cb: () -> Unit
        ) {
            if (isChromeTabOpen) {
                listeners.add(cb)
            } else {
                cb.invoke()
            }
        }
    }
}