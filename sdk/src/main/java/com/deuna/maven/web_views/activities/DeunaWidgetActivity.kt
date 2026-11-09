package com.deuna.maven.web_views.activities

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.deuna.maven.R
import com.deuna.maven.internal.modal.DeunaModalHost
import com.deuna.maven.internal.modal.DeunaModalRecovery
import com.deuna.maven.internal.modal.DeunaWidgetModalRegistry
import com.deuna.maven.internal.modal.dispatchOnClosed
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.ExternalUrlHelper
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.build

class DeunaWidgetActivity : ComponentActivity() {

    companion object {
        const val EXTRA_WIDGET_CONFIG_ID = "deuna_widget_config_id"
    }

    private var widgetConfigId: String? = null
    private lateinit var deunaWidget: DeunaWidget
    private var onClosedDispatched = false
    private var foregroundWatcher: Application.ActivityLifecycleCallbacks? = null

    private val modalHost = object : DeunaModalHost {
        override val deunaWidget: DeunaWidget
            get() = this@DeunaWidgetActivity.deunaWidget

        override fun dismiss() {
            this@DeunaWidgetActivity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        if (shouldForceSoftwareRendering()) {
            window.setFlags(0, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }
        // Preserve status bar visibility — Translucent theme makes it transparent by default.
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContentView(R.layout.deuna_webview_container)

        widgetConfigId = intent.getStringExtra(EXTRA_WIDGET_CONFIG_ID)
        val configId = widgetConfigId
        val widgetConfiguration = if (configId.isNullOrBlank()) {
            null
        } else {
            DeunaWidgetModalRegistry.get(configId)
        }

        if (widgetConfiguration == null) {
            DeunaLogs.error("Missing widget configuration to launch modal activity")
            finish()
            return
        }

        ExternalUrlHelper.registerForActivityResult(this)

        deunaWidget = findViewById(R.id.deuna_webview)
        deunaWidget.widgetConfiguration = widgetConfiguration
        deunaWidget.widgetConfiguration?.onCloseByUser = {
            finish()
        }
        deunaWidget.build()

        widgetConfiguration.sdkInstance.bindModalHost(modalHost)

        onBackPressedDispatcher.addCallback(this) {
            if (!deunaWidget.closeEnabled) {
                return@addCallback
            }

            deunaWidget.closeAction = CloseAction.userAction
            deunaWidget.bridge?.onCloseByUser?.invoke()
        }

        registerForegroundWatcher()
    }

    /**
     * This Activity runs in its own task (see its manifest taskAffinity) so a host Activity
     * declared android:launchMode="singleTask" with an intent-filter for the merchant's own
     * callback_urls scheme — the setup our docs recommend — doesn't collapse it when that deep
     * link resolves (ENTP1-18281 follow-up). But bringing the host task back to front doesn't
     * bring this task along with it — it stays alive, just left behind, invisible to the user.
     * Watch for any other Activity resuming while this one is still open and reclaim focus.
     */
    private fun registerForegroundWatcher() {
        val watcher = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                if (activity === this@DeunaWidgetActivity || isFinishing || isDestroyed) {
                    return
                }
                runCatching {
                    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                    activityManager?.moveTaskToFront(taskId, 0)
                }
            }

            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        }
        application.registerActivityLifecycleCallbacks(watcher)
        foregroundWatcher = watcher
    }

    override fun onStart() {
        super.onStart()
        if (::deunaWidget.isInitialized) {
            deunaWidget.resume()
        }
    }

    override fun onStop() {
        if (::deunaWidget.isInitialized) {
            deunaWidget.pause()
        }
        super.onStop()
    }

    override fun onDestroy() {
        foregroundWatcher?.let { application.unregisterActivityLifecycleCallbacks(it) }
        foregroundWatcher = null
        if (::deunaWidget.isInitialized) {
            deunaWidget.widgetConfiguration?.sdkInstance?.unbindModalHost(modalHost)
            deunaWidget.destroy()
            if (isFinishing && !onClosedDispatched) {
                deunaWidget.widgetConfiguration?.let {
                    DeunaModalRecovery.tryRecoverSuccessOnSystemClose(
                        widgetConfiguration = it,
                        closeAction = deunaWidget.closeAction,
                    )
                    dispatchOnClosed(
                        widgetConfiguration = it,
                        closeAction = deunaWidget.closeAction,
                    )
                    onClosedDispatched = true
                }
            }
        }

        if (isFinishing) {
            widgetConfigId?.let { DeunaWidgetModalRegistry.remove(it) }
        }
        super.onDestroy()
    }

    private fun shouldForceSoftwareRendering(): Boolean {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.getBoolean("com.deuna.explore.widget.force_software_rendering", false) == true
        } catch (_: Exception) {
            false
        }
    }
}
