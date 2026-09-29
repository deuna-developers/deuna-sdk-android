package com.deuna.maven.fraud

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.deuna.maven.GenerateFraudId
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal object MercadoPagoState {
    private val started = AtomicBoolean(false)
    private val readyLatch = CountDownLatch(1)
    private val initExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun startInit(
        context: Context,
        onSuccess: (() -> Unit)? = null,
        onError: ((error: String) -> Unit)? = null
    ) {
        if (started.compareAndSet(false, true)) {
            initExecutor.execute {
                try {
                    val deviceSdkClass = Class.forName("com.mercadolibre.android.device.sdk.DeviceSDK")
                    val getInstanceMethod = deviceSdkClass.getMethod("getInstance")
                    val instance = getInstanceMethod.invoke(null)

                    val executeMethod = deviceSdkClass.getMethod("execute", Context::class.java)
                    DeunaLogs.info("[fraud] MERCADOPAGO initializing sequence in background")
                    executeMethod.invoke(instance, context)

                    // DeviceSDK uses AsyncTask with CountDownLatch(1).await(3, TimeUnit.SECONDS)
                    // Wait 3500ms to allow async collectors to finish
                    try {
                        Thread.sleep(3500)
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                    DeunaLogs.info("[fraud] MERCADOPAGO ✔ background initialization completed")
                    dispatchOnMain { onSuccess?.invoke() }
                } catch (e: ClassNotFoundException) {
                    val message = "MERCADOPAGO not linked. Add MercadoPago device SDK dependency to your app."
                    DeunaLogs.error("[fraud] $message")
                    dispatchOnMain { onError?.invoke(message) }
                } catch (e: Throwable) {
                    val message = e.message ?: "Unknown error"
                    DeunaLogs.error("[fraud] MERCADOPAGO ✘ background init failed: $message", e)
                    dispatchOnMain { onError?.invoke(message) }
                } finally {
                    readyLatch.countDown()
                }
            }
        } else {
            // Already started; wait for latch in worker thread to deliver callback
            initExecutor.execute {
                try {
                    readyLatch.await(5, TimeUnit.SECONDS)
                    dispatchOnMain { onSuccess?.invoke() }
                } catch (e: Throwable) {
                    dispatchOnMain { onError?.invoke(e.message ?: "Timeout waiting for initialization") }
                }
            }
        }
    }

    fun ensureReady(context: Context, timeoutMs: Long = 4000L) {
        if (!started.get()) {
            startInit(context)
        }
        try {
            readyLatch.await(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun dispatchOnMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun initMercadoPago(
    context: Context,
    config: Json,
    onSuccess: (() -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null
) {
    MercadoPagoState.startInit(context, onSuccess, onError)
}

@Suppress("UNUSED_PARAMETER")
internal fun GenerateFraudId.runMercadoPago(config: Json): Any? {
    return try {
        val deviceSdkClass = Class.forName("com.mercadolibre.android.device.sdk.DeviceSDK")
        val getInstanceMethod = deviceSdkClass.getMethod("getInstance")
        val instance = getInstanceMethod.invoke(null)

        // Ensure initialization has finished (0ms if already initialized, or waits remainder/fallback)
        MercadoPagoState.ensureReady(context)

        val getInfoJsonMethod = deviceSdkClass.getMethod("getInfoAsJsonString")
        val jsonString = getInfoJsonMethod.invoke(instance) as? String

        if (jsonString.isNullOrBlank()) {
            DeunaLogs.error("[fraud] MERCADOPAGO ✘ empty fingerprint")
            return null
        }

        DeunaLogs.info("[fraud] MERCADOPAGO ✔ fingerprint generated")
        try {
            org.json.JSONObject(jsonString)
        } catch (_: Throwable) {
            jsonString
        }
    } catch (e: ClassNotFoundException) {
        DeunaLogs.error("[fraud] MERCADOPAGO not linked. Add MercadoPago device SDK dependency to your app.")
        null
    } catch (e: Throwable) {
        DeunaLogs.error("[fraud] MERCADOPAGO ✘ failed: ${e.message}", e)
        null
    }
}
