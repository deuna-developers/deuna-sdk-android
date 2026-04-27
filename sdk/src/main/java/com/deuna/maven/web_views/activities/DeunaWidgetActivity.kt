package com.deuna.maven.web_views.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
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

    private val modalHost = object : DeunaModalHost {
        override val deunaWidget: DeunaWidget
            get() = this@DeunaWidgetActivity.deunaWidget

        override fun dismiss() {
            this@DeunaWidgetActivity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep widget rendering off GPU to avoid WebView GL crashes on some providers/devices.
        window.setFlags(
            0,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
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
}
