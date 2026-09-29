package com.deuna.maven

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.deuna.maven.fraud.initMercadoPago
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal fun parseAndInitFraudProviders(
    context: Context,
    params: Json,
    onSuccess: (() -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null
) {
    val mainHandler = Handler(Looper.getMainLooper())
    val dispatchMain = { action: () -> Unit ->
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    val requests = mutableListOf<FraudProviderRequest>()
    for ((rawKey, rawValue) in params) {
        val provider = FraudProviderName.from(rawKey)
        if (provider == null) {
            DeunaLogs.warning("[fraud] Unsupported provider $rawKey in initialize. Ignoring.")
            continue
        }

        val config = (rawValue as? Map<*, *>)?.let { map ->
            val result = mutableMapOf<String, Any?>()
            map.forEach { (k, v) -> (k as? String)?.let { result[it] = v } }
            result
        } ?: emptyMap()
        requests.add(FraudProviderRequest(provider, config))
    }

    if (requests.isEmpty()) {
        dispatchMain { onSuccess?.invoke() }
        return
    }

    val errors = Collections.synchronizedList(mutableListOf<String>())
    val latch = CountDownLatch(requests.size)

    requests.forEach { request ->
        when (request.name) {
            FraudProviderName.MERCADOPAGO -> {
                initMercadoPago(
                    context = context,
                    config = request.config,
                    onSuccess = {
                        latch.countDown()
                    },
                    onError = { error ->
                        errors.add(error)
                        latch.countDown()
                    }
                )
            }
            else -> {
                // Providers that do not require pre-initialization complete immediately
                latch.countDown()
            }
        }
    }

    Executors.newSingleThreadExecutor().execute {
        try {
            latch.await(8, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        dispatchMain {
            if (errors.isEmpty()) {
                onSuccess?.invoke()
            } else {
                onError?.invoke(errors.joinToString("; "))
            }
        }
    }
}
