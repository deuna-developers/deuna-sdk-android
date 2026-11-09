package com.deuna.maven.web_views

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent;
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.extensions.findComponentActivity
import com.deuna.maven.shared.extensions.findFragmentActivity
import com.deuna.maven.web_views.dialog_fragments.ExternalUrlDialogFragment


enum class ExternalUrlBrowser {
    WEB_VIEW,
    CUSTOM_TABS
}


class ExternalUrlHelper {
    companion object {
        private var listeners = mutableSetOf<() -> Unit>()
        private var isChromeTabOpened = false
        private var chromeTabLauncher: ActivityResultLauncher<Intent>? = null

        private var externalUrlDialog: ExternalUrlDialogFragment? = null
        private var browser: ExternalUrlBrowser = ExternalUrlBrowser.WEB_VIEW
        private var onExternalUrlBrowserClosed: (() -> Unit)? = null

        /**
         * Registers an activity result launcher for custom tabs.
         * This is needed to wait until the custom chrome tab is closed.
         */
        fun registerForActivityResult(
            context: Context
        ) {
            val activity = context.findFragmentActivity() ?: context.findComponentActivity()

            if (activity == null) {
                DeunaLogs.error("FragmentActivity or ComponentActivity not found")
                return
            }

            chromeTabLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { _ ->
                if (!activity.isDestroyed) {
                    // DeunaWidgetActivity runs in its own task (see its manifest taskAffinity)
                    // so a host Activity with launchMode="singleTask" and an intent-filter for
                    // the merchant's callback_urls scheme — the setup our docs recommend —
                    // doesn't collapse it when that deep link resolves (ENTP1-18281 follow-up).
                    // But bringing that host task back to front doesn't bring this task along:
                    // it's left alive, just behind it. Bring it forward explicitly.
                    runCatching {
                        val activityManager =
                            activity.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
                        activityManager?.moveTaskToFront(activity.taskId, 0)
                    }
                    listeners.toList().forEach { it.invoke() }
                    listeners.clear()
                }
                if (onExternalUrlBrowserClosed != null && browser == ExternalUrlBrowser.CUSTOM_TABS) {
                    onExternalUrlBrowserClosed?.invoke()
                    onExternalUrlBrowserClosed = null
                }
                isChromeTabOpened = false
            }
        }


        fun waitUntilChromeTabIsClosed(
            cb: () -> Unit
        ) {
            if (isChromeTabOpened && browser == ExternalUrlBrowser.CUSTOM_TABS) {
                listeners.add(cb)
            } else {
                cb.invoke()
            }
        }


        fun openUrl(
            context: Context,
            url: String,
            browser: ExternalUrlBrowser,
            onExternalUrlClosed: () -> Unit
        ) {
            if (url.isEmpty()) {
                return
            }

            this.browser = browser
            this.onExternalUrlBrowserClosed = onExternalUrlClosed

            when (browser) {
                ExternalUrlBrowser.WEB_VIEW -> {
                    // Defensive reset: this flow does not depend on Custom Tabs.
                    isChromeTabOpened = false
                    externalUrlDialog = ExternalUrlDialogFragment(
                        context = context,
                        url = url, onDialogDestroyed = {
                            externalUrlDialog = null
                            isChromeTabOpened = false
                            this.onExternalUrlBrowserClosed?.invoke()
                            this.onExternalUrlBrowserClosed = null
                        }
                    )
                    externalUrlDialog?.show()
                    return
                }

                ExternalUrlBrowser.CUSTOM_TABS -> {
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    val intent = customTabsIntent.intent.apply {
                        data = Uri.parse(url)
                    }

                    if (chromeTabLauncher == null) {
                        DeunaLogs.error("chromeTabLauncher is null - did you call registerForActivityResult()?")
                        isChromeTabOpened = false
                        return
                    }
                    chromeTabLauncher?.launch(intent)
                    isChromeTabOpened = true
                }
            }
        }

        fun close() {
            externalUrlDialog?.dismiss()
            // Defensive reset to avoid leaking stale state between flows.
            isChromeTabOpened = false
        }
    }
}
