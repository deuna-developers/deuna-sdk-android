package com.deuna.maven.web_views

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent;
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.extensions.findFragmentActivity
import com.deuna.maven.web_views.dialog_fragments.ExternalUrlDialogFragment


enum class ExternalUrlBrowser {
    WEB_VIEW,
    CUSTOM_TABS
}


class ExternalUrlHelper {

    private var externalUrlDialog: ExternalUrlDialogFragment? = null
    private var browser: ExternalUrlBrowser = ExternalUrlBrowser.WEB_VIEW



    companion object {
        private var listeners = mutableSetOf<() -> Unit>()
        private var isChromeTabOpened = false
        private var chromeTabLauncher: ActivityResultLauncher<Intent>? = null

        /**
         * Registers an activity result launcher for custom tabs.
         * This is needed to wait until the custom chrome tab is closed.
         */
        fun registerForActivityResult(
            context: Context
        ) {
            val activity = context.findFragmentActivity() ?: return
            chromeTabLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { _ ->
                if (!activity.isDestroyed) {
                    listeners.forEach {
                        it.invoke()
                        listeners.remove(it)
                    }
                }
                isChromeTabOpened = false
            }
        }
    }


    fun waitUntilChromeTabIsClosed(
        cb: () -> Unit
    ) {
        if (isChromeTabOpened) {
            listeners.add(cb)
        } else {
            cb.invoke()
        }
    }


    fun openUrl(
        context: Context,
        url: String,
        browser: ExternalUrlBrowser
    ) {
        if (url.isEmpty()) {
            return
        }

        val fragmentActivity = context.findFragmentActivity() ?: return

        this.browser = browser

        when (browser) {
            ExternalUrlBrowser.WEB_VIEW -> {
                externalUrlDialog = ExternalUrlDialogFragment(
                    url = url, onDialogDestroyed = {
                        externalUrlDialog = null
                    }
                )
                externalUrlDialog?.show(
                    fragmentActivity.supportFragmentManager,
                    "ExternalUrlDialogFragment+${System.currentTimeMillis()}"
                )
                return
            }

            ExternalUrlBrowser.CUSTOM_TABS -> {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                val intent = customTabsIntent.intent.apply {
                    data = Uri.parse(url)
                }

                chromeTabLauncher?.launch(intent) ?: run {
                    DeunaLogs.error("chromeTabLauncher is null - did you call registerForActivityResult()?")
                }
                isChromeTabOpened = true
            }
        }
    }

    fun close() {
        externalUrlDialog?.dismiss()
    }
}