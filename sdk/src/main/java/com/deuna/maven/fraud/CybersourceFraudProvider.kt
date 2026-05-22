package com.deuna.maven.fraud

import android.content.Context
import com.deuna.maven.GenerateFraudId
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal fun GenerateFraudId.runCybersource(config: Json, providerId: String) {
    val orgId = config["orgId"] as? String
    val merchantId = config["merchantId"] as? String
    val fpServer = config["fpServer"] as? String ?: "h.online-metrix.net"
    if (orgId.isNullOrBlank() || merchantId.isNullOrBlank()) {
        DeunaLogs.warning("[fraud] Missing CYBERSOURCE.orgId or merchantId. Skipping native init.")
        return
    }
    val sessionId = merchantId + providerId
    try {
        val profiled = CybersourceNativeBridge.profile(context, orgId, fpServer, sessionId)
        if (!profiled) {
            DeunaLogs.warning("[fraud] CYBERSOURCE profile did not return TMX_OK.")
        }
    } catch (error: Throwable) {
        DeunaLogs.warning("[fraud] CYBERSOURCE native profiling failed: ${error.message}")
    }
}

private object CybersourceNativeBridge {
    private const val TMX_PROFILING_CLASS = "com.lexisnexisrisk.threatmetrix.rl.TMXProfiling"
    private const val TMX_CONFIG_CLASS = "com.lexisnexisrisk.threatmetrix.rl.TMXConfig"
    private const val TMX_OPTIONS_CLASS = "com.lexisnexisrisk.threatmetrix.rl.TMXProfilingOptions"
    private const val TMX_NOTIFIER_CLASS = "com.lexisnexisrisk.threatmetrix.rl.TMXEndNotifier"

    private val configurationLock = Any()
    private var configuredOrgId: String? = null
    private var configuredFpServer: String? = null

    fun profile(context: Context, orgId: String, fpServer: String, sessionId: String): Boolean {
        val profilingClass = Class.forName(TMX_PROFILING_CLASS)
        val profiling = profilingClass.getMethod("getInstance").invoke(null)

        configureIfNeeded(context, profilingClass, profiling, orgId, fpServer)

        val optionsClass = Class.forName(TMX_OPTIONS_CLASS)
        val options = optionsClass.getDeclaredConstructor().newInstance()
        optionsClass.getMethod("setSessionID", String::class.java).invoke(options, sessionId)

        val latch = CountDownLatch(1)
        var profiled = false

        val classLoader = Thread.currentThread().contextClassLoader ?: profilingClass.classLoader!!
        val notifierInterface = Class.forName(TMX_NOTIFIER_CLASS)

        val notifier = Proxy.newProxyInstance(classLoader, arrayOf(notifierInterface)) { _, method, args ->
            if (method.name == "complete") {
                val result = args?.getOrNull(0)
                val statusName = resolveStatusName(result)
                val returnedSessionId = resolveSessionId(result)
                DeunaLogs.info("[fraud] CYBERSOURCE profile status: $statusName, sessionId: $returnedSessionId")
                profiled = statusName == "TMX_OK"
                latch.countDown()
            }
            null
        }

        profilingClass.getMethod("profile", optionsClass, notifierInterface)
            .invoke(profiling, options, notifier)

        latch.await(8, TimeUnit.SECONDS)
        return profiled
    }

    private fun configureIfNeeded(
        context: Context,
        profilingClass: Class<*>,
        profiling: Any?,
        orgId: String,
        fpServer: String
    ) {
        synchronized(configurationLock) {
            val alreadyConfigured = configuredOrgId
            if (alreadyConfigured != null) {
                if (alreadyConfigured != orgId || configuredFpServer != fpServer) {
                    DeunaLogs.warning(
                        "[fraud] CYBERSOURCE already configured with orgId=$alreadyConfigured fpServer=$configuredFpServer. Ignoring new orgId=$orgId fpServer=$fpServer."
                    )
                }
                return
            }
            val configClass = Class.forName(TMX_CONFIG_CLASS)
            val config = configClass.getDeclaredConstructor().newInstance()
            configClass.getMethod("setContext", Context::class.java).invoke(config, context)
            configClass.getMethod("setOrgId", String::class.java).invoke(config, orgId)
            configClass.getMethod("setFPServer", String::class.java).invoke(config, fpServer)
            profilingClass.getMethod("init", configClass).invoke(profiling, config)
            configuredOrgId = orgId
            configuredFpServer = fpServer
        }
    }

    private fun resolveStatusName(result: Any?): String? {
        if (result == null) return null
        val resultClass = result.javaClass
        val status = try {
            resultClass.getMethod("getStatus").invoke(result)
        } catch (_: Throwable) {
            try {
                resultClass.getDeclaredField("status").apply { isAccessible = true }.get(result)
            } catch (_: Throwable) { null }
        }
        return status?.toString()
    }

    private fun resolveSessionId(result: Any?): String? {
        if (result == null) return null
        val resultClass = result.javaClass
        return try {
            resultClass.getMethod("getSessionID").invoke(result) as? String
        } catch (_: Throwable) {
            try {
                resultClass.getDeclaredField("sessionID").apply { isAccessible = true }.get(result) as? String
            } catch (_: Throwable) { null }
        }
    }
}
