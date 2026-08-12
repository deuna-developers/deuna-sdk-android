package com.deuna.maven.fraud

import android.content.Context
import com.deuna.maven.GenerateFraudId
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal fun GenerateFraudId.runKount(config: Json, providerId: String) {
    val merchantId = config["merchantId"] as? String
    if (merchantId.isNullOrBlank()) {
        DeunaLogs.warning("[fraud] Missing KOUNT.merchantId. Skipping native init.")
        return
    }
    val environment = (config["environment"] as? String ?: "PRODUCTION").uppercase()
    try {
        KountNativeBridge.profile(context, merchantId, environment, providerId)
        DeunaLogs.info("[fraud] KOUNT profile initiated.")
    } catch (error: Throwable) {
        DeunaLogs.warning("[fraud] KOUNT native init failed: ${error.message}")
    }
}

private object KountNativeBridge {
    private const val KOUNT_CLASS = "com.kount.api.KountSDK"

    fun profile(context: Context, merchantId: String, environment: String, sessionId: String) {
        val kountClass = Class.forName(KOUNT_CLASS)
        val instance = kountClass.getDeclaredField("INSTANCE").get(null)

        kountClass.getMethod("setMerchantId", String::class.java).invoke(instance, merchantId)
        kountClass.getMethod("setEnvironment", Int::class.javaPrimitiveType)
            .invoke(instance, resolveEnvironmentConstant(kountClass, instance, environment))
        kountClass.getMethod("setSessionId", String::class.java).invoke(instance, sessionId)

        val latch = CountDownLatch(1)
        val classLoader = Thread.currentThread().contextClassLoader ?: kountClass.classLoader!!
        val fn1 = Class.forName("kotlin.jvm.functions.Function1")
        val fn2 = Class.forName("kotlin.jvm.functions.Function2")

        val onComplete = Proxy.newProxyInstance(classLoader, arrayOf(fn1)) { _, _, args ->
            DeunaLogs.info("[fraud] KOUNT collection complete: ${args?.getOrNull(0)}")
            latch.countDown()
            null
        }
        val onFailure = Proxy.newProxyInstance(classLoader, arrayOf(fn2)) { _, _, args ->
            DeunaLogs.warning("[fraud] KOUNT error: ${args?.getOrNull(1)}, sessionId: ${args?.getOrNull(0)}")
            latch.countDown()
            null
        }

        kountClass.getMethod("collectForSession", Context::class.java, fn1, fn2)
            .invoke(instance, context, onComplete, onFailure)

        latch.await(8, TimeUnit.SECONDS)
    }

    private fun resolveEnvironmentConstant(kountClass: Class<*>, instance: Any?, environment: String): Int {
        val fieldName = if (environment == "TEST") "ENVIRONMENT_TEST" else "ENVIRONMENT_PRODUCTION"
        return try {
            kountClass.getDeclaredField(fieldName).getInt(instance)
        } catch (_: NoSuchFieldException) {
            val companionClass = Class.forName("$KOUNT_CLASS\$Companion")
            val companion = kountClass.getDeclaredField("Companion").get(null)
            companionClass.getDeclaredField(fieldName).getInt(companion)
        }
    }
}
