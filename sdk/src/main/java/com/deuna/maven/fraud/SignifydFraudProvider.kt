package com.deuna.maven.fraud

import android.content.Context
import com.deuna.maven.GenerateFraudId
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal fun GenerateFraudId.runSignifyd(config: Json, providerId: String) {
    val orgId = config["orgId"] as? String
    val fpServer = (config["fpServer"] as? String).takeIf { !it.isNullOrBlank() }
        ?: "imgs.signifyd.com"

    if (orgId.isNullOrBlank()) {
        DeunaLogs.warning("[fraud] Missing SIGNIFYD.orgId. Skipping.")
        return
    }

    try {
        val profiled = SignifydNativeBridge.profile(context, orgId, fpServer, providerId)
        if (!profiled) {
            DeunaLogs.error("[fraud] SIGNIFYD ✘ profiling did not return TMX_OK — sessionId=$providerId")
        }
    } catch (e: ClassNotFoundException) {
        DeunaLogs.error("[fraud] SIGNIFYD not linked. Add the Signifyd AAR dependency to your app.")
    } catch (e: Throwable) {
        DeunaLogs.error("[fraud] SIGNIFYD ✘ native profiling threw exception: ${e.message}")
    }
}

private object SignifydNativeBridge {
    // Signifyd uses the non-branded ThreatMetrix SDK (no "rl" sub-package).
    private const val TMX_PROFILING_CLASS   = "com.lexisnexisrisk.threatmetrix.TMXProfiling"
    private const val TMX_CONFIG_CLASS      = "com.lexisnexisrisk.threatmetrix.TMXConfig"
    private const val TMX_OPTIONS_CLASS     = "com.lexisnexisrisk.threatmetrix.TMXProfilingOptions"
    private const val TMX_NOTIFIER_CLASS    = "com.lexisnexisrisk.threatmetrix.TMXEndNotifier"
    private const val TMX_RESULT_CLASS      = "com.lexisnexisrisk.threatmetrix.TMXProfilingHandle\$Result"

    private val configurationLock = Any()
    private var configuredOrgId: String? = null
    private var configuredFpServer: String? = null

    /**
     * Configure (once) and profile the device.
     * Returns true only when TMXStatusCode == TMX_OK.
     */
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
        val resultClass = Class.forName(TMX_RESULT_CLASS)

        val notifier = Proxy.newProxyInstance(classLoader, arrayOf(notifierInterface)) { _, method, args ->
            if (method.name == "complete") {
                val result = args?.getOrNull(0)
                val statusName = resolveStatus(result, resultClass)
                val returnedSessionId = resolveSessionId(result, resultClass)
                DeunaLogs.info("[fraud] SIGNIFYD profile status=$statusName sessionId=$returnedSessionId")
                if (statusName == "TMX_OK") {
                    profiled = true
                    DeunaLogs.info("[fraud] SIGNIFYD ✔ fraud ID generated — sessionId=$returnedSessionId")
                } else {
                    DeunaLogs.error("[fraud] SIGNIFYD ✘ profiling failed — status=$statusName sessionId=$returnedSessionId")
                }
                latch.countDown()
            }
            null
        }

        profilingClass
            .getMethod("profile", optionsClass, notifierInterface)
            .invoke(profiling, options, notifier)

        val timedOut = !latch.await(8, TimeUnit.SECONDS)
        if (timedOut) {
            DeunaLogs.error("[fraud] SIGNIFYD ✘ profiling timed out after 8s — sessionId=$sessionId")
        }
        return profiled
    }

    private fun configureIfNeeded(
        context: Context,
        profilingClass: Class<*>,
        profiling: Any?,
        orgId: String,
        fpServer: String,
    ) {
        synchronized(configurationLock) {
            val alreadyConfigured = configuredOrgId
            if (alreadyConfigured != null) {
                if (alreadyConfigured != orgId || configuredFpServer != fpServer) {
                    DeunaLogs.warning(
                        "[fraud] SIGNIFYD already configured with orgId=$alreadyConfigured fpServer=$configuredFpServer. Ignoring new orgId=$orgId fpServer=$fpServer."
                    )
                }
                return
            }

            val configClass = Class.forName(TMX_CONFIG_CLASS)
            val config = configClass.getDeclaredConstructor().newInstance()
            configClass.getMethod("setContext", Context::class.java).invoke(config, context)
            configClass.getMethod("setOrgId", String::class.java).invoke(config, orgId)
            configClass.getMethod("setFPServer", String::class.java).invoke(config, fpServer)

            try {
                profilingClass.getMethod("init", configClass).invoke(profiling, config)
                configuredOrgId = orgId
                configuredFpServer = fpServer
            } catch (e: java.lang.reflect.InvocationTargetException) {
                val cause = e.cause ?: e
                DeunaLogs.error("[fraud] SIGNIFYD ✘ init failed — ${cause.message}")
                throw cause
            }
        }
    }

    private fun resolveStatus(result: Any?, resultClass: Class<*>): String? {
        if (result == null) return null
        return try {
            val status = resultClass.getMethod("getStatus").invoke(result)
            (status as? Enum<*>)?.name ?: status?.toString()
        } catch (_: Throwable) { null }
    }

    private fun resolveSessionId(result: Any?, resultClass: Class<*>): String? {
        if (result == null) return null
        return try {
            resultClass.getMethod("getSessionID").invoke(result) as? String
        } catch (_: Throwable) { null }
    }
}
