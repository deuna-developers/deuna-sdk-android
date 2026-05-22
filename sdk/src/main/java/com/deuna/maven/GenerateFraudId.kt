package com.deuna.maven

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.deuna.maven.fraud.runCybersource
import com.deuna.maven.fraud.runKount
import com.deuna.maven.fraud.runRiskified
import com.deuna.maven.fraud.runSignifyd
import com.deuna.maven.fraud.runSift
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toBase64
import com.deuna.maven.shared.value
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
        context = context,
        environment = environment,
        callback = callback,
        params = params
    ).run()
}

private enum class FraudProviderName {
    RISKIFIED,
    CYBERSOURCE,
    SIFT,
    KOUNT,
    SIGNIFYD;

    companion object {
        fun from(raw: String): FraudProviderName? =
            entries.firstOrNull { it.name == raw.uppercase(Locale.US) }
    }
}

private data class FraudProviderRequest(
    val name: FraudProviderName,
    val config: Json
)

internal class GenerateFraudId(
    internal val context: Context,
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
            FraudProviderName.SIFT -> runSift(config, providerId)
            FraudProviderName.KOUNT -> runKount(config, providerId)
            FraudProviderName.SIGNIFYD -> runSignifyd(config, providerId)
        }
    }

    internal fun callbackOnMain(value: String?) {
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
