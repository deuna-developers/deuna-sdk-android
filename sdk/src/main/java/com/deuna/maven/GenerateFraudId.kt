package com.deuna.maven

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toBase64
import com.deuna.maven.shared.value
import com.lexisnexisrisk.threatmetrix.rl.TMXConfig
import com.lexisnexisrisk.threatmetrix.rl.TMXEndNotifier
import com.lexisnexisrisk.threatmetrix.rl.TMXProfiling
import com.lexisnexisrisk.threatmetrix.rl.TMXProfilingHandle
import com.lexisnexisrisk.threatmetrix.rl.TMXProfilingOptions
import com.riskified.android_sdk.RiskifiedBeaconMain
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Public API intentionally unchanged to avoid breaking integrators.
 */
fun DeunaSDK.generateFraudId(
    context: Context,
    params: Json? = null,
    callback: (fraudId: String?) -> Unit
) {
    GenerateFraudId(
        context = context.applicationContext,
        environment = environment,
        callback = callback,
        params = params
    ).run()
}

private enum class FraudProviderName {
    RISKIFIED,
    CYBERSOURCE;

    companion object {
        fun from(raw: String): FraudProviderName? =
            entries.firstOrNull { it.name == raw.uppercase(Locale.US) }
    }
}

private data class FraudProviderRequest(
    val name: FraudProviderName,
    val config: Json
)

private class GenerateFraudId(
    private val context: Context,
    private val environment: Environment,
    private val callback: (fraudId: String?) -> Unit,
    private val params: Json?
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val workers = Executors.newCachedThreadPool()

    fun run() {
        val requests = parseRequests(params)
        if (requests.isEmpty()) {
            callbackOnMain(null)
            return
        }

        DeunaLogs.info("[fraud] GENERATING FRAUD ID in env=${environment.value()}")

        val idsByProvider = linkedMapOf<String, Any?>()
        requests.forEach { request ->
            idsByProvider[request.name.name] = UUID.randomUUID().toString().lowercase(Locale.US)
        }

        val latch = CountDownLatch(requests.size)
        requests.forEach { request ->
            workers.execute {
                try {
                    val providerId = idsByProvider[request.name.name] as? String
                    if (providerId != null) {
                        runProvider(
                            provider = request.name,
                            config = request.config,
                            providerId = providerId
                        )
                    }
                } catch (error: Throwable) {
                    DeunaLogs.warning(
                        "[fraud] Provider ${request.name.name} failed: ${error.message}"
                    )
                } finally {
                    latch.countDown()
                }
            }
        }

        workers.execute {
            latch.await(8, TimeUnit.SECONDS)
            callbackOnMain(idsByProvider.toBase64())
        }
    }

    private fun parseRequests(raw: Json?): List<FraudProviderRequest> {
        if (raw == null) return emptyList()

        val requests = mutableListOf<FraudProviderRequest>()
        for ((rawKey, rawValue) in raw) {
            val provider = FraudProviderName.from(rawKey)
            if (provider == null) {
                DeunaLogs.warning("[fraud] Unsupported provider $rawKey. Ignoring.")
                continue
            }

            val config = rawValue.asJsonObjectOrNull()
            if (config == null) {
                DeunaLogs.warning("[fraud] Invalid config for $rawKey. Expected object.")
                continue
            }
            requests.add(FraudProviderRequest(provider, config))
        }
        return requests
    }

    private fun runProvider(provider: FraudProviderName, config: Json, providerId: String) {
        when (provider) {
            FraudProviderName.RISKIFIED -> runRiskified(config, providerId)
            FraudProviderName.CYBERSOURCE -> runCybersource(config, providerId)
        }
    }

    private fun runRiskified(config: Json, providerId: String) {
        val storeDomain = config["storeDomain"] as? String
        if (storeDomain.isNullOrBlank()) {
            DeunaLogs.warning("[fraud] Missing RISKIFIED.storeDomain. Skipping native init.")
            return
        }

        try {
            RiskifiedBeaconMain().startBeacon(
                storeDomain,
                providerId,
                false,
                context
            )
            DeunaLogs.info("[fraud] RISKIFIED beacon started.")
        } catch (error: Throwable) {
            DeunaLogs.warning("[fraud] RISKIFIED native init failed: ${error.message}")
        }
    }

    private fun runCybersource(config: Json, providerId: String) {
        val orgId = config["orgId"] as? String
        val merchantId = config["merchantId"] as? String
        val fpServer = config["fpServer"] as? String ?: "h.online-metrix.net"
        if (orgId.isNullOrBlank() || merchantId.isNullOrBlank()) {
            DeunaLogs.warning("[fraud] Missing CYBERSOURCE.orgId or merchantId. Skipping native init.")
            return
        }

        val sessionId = merchantId + providerId
        try {
            val profiled = CybersourceNativeBridge.profile(
                context = context,
                orgId = orgId,
                fpServer = fpServer,
                sessionId = sessionId
            )
            if (!profiled) {
                DeunaLogs.warning("[fraud] CYBERSOURCE profile did not return TMX_OK.")
            }
        } catch (error: Throwable) {
            DeunaLogs.warning("[fraud] CYBERSOURCE native profiling failed: ${error.message}")
        }
    }

    private fun callbackOnMain(value: String?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.invoke(value)
        } else {
            mainHandler.post { callback.invoke(value) }
        }
    }

    private fun Any?.asJsonObjectOrNull(): Json? {
        val map = this as? Map<*, *> ?: return null
        val result = mutableMapOf<String, Any?>()
        map.forEach { (key, value) ->
            val stringKey = key as? String ?: return null
            result[stringKey] = value
        }
        return result
    }
}

private object CybersourceNativeBridge {
    private val configurationLock = Any()
    private var configuredOrgId: String? = null
    private var configuredFpServer: String? = null

    fun profile(context: Context, orgId: String, fpServer: String, sessionId: String): Boolean {
        val profiling = TMXProfiling.getInstance()
        configureIfNeeded(context, profiling, orgId, fpServer)

        val options = TMXProfilingOptions()
            .setSessionID(sessionId)

        val latch = CountDownLatch(1)
        var profiled = false

        profiling.profile(options, object : TMXEndNotifier {
            override fun complete(result: TMXProfilingHandle.Result) {
                val statusName = result.status.name
                val returnedSessionId = result.sessionID
                DeunaLogs.info(
                    "[fraud] CYBERSOURCE profile status: $statusName, sessionId: $returnedSessionId"
                )
                profiled = statusName == "TMX_OK"
                latch.countDown()
            }
        })

        latch.await(8, TimeUnit.SECONDS)
        return profiled
    }

    private fun configureIfNeeded(
        context: Context,
        profiling: TMXProfiling,
        orgId: String,
        fpServer: String
    ) {
        synchronized(configurationLock) {
            val alreadyConfigured = configuredOrgId
            if (alreadyConfigured != null) {
                if (alreadyConfigured != orgId || configuredFpServer != fpServer) {
                    DeunaLogs.warning(
                        "[fraud] CYBERSOURCE already configured with orgId=$alreadyConfigured and fpServer=$configuredFpServer. Ignoring new orgId=$orgId fpServer=$fpServer."
                    )
                }
                return
            }

            val config = TMXConfig()
                .setContext(context)
                .setOrgId(orgId)
                .setFPServer(fpServer)
            profiling.init(config)
            configuredOrgId = orgId
            configuredFpServer = fpServer
        }
    }
}
