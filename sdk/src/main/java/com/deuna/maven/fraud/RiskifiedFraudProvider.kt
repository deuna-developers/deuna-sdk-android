package com.deuna.maven.fraud

import android.content.Context
import com.deuna.maven.GenerateFraudId
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json

internal fun GenerateFraudId.runRiskified(config: Json, providerId: String) {
    val storeDomain = config["storeDomain"] as? String
    if (storeDomain.isNullOrBlank()) {
        DeunaLogs.warning("[fraud] Missing RISKIFIED.storeDomain. Skipping native init.")
        return
    }
    try {
        RiskifiedNativeBridge.startBeacon(storeDomain, providerId, context)
        DeunaLogs.info("[fraud] RISKIFIED beacon started.")
    } catch (error: Throwable) {
        DeunaLogs.warning("[fraud] RISKIFIED native init failed: ${error.message}")
    }
}

private object RiskifiedNativeBridge {
    private const val BEACON_MAIN_CLASS = "com.riskified.android_sdk.RiskifiedBeaconMain"

    fun startBeacon(storeDomain: String, providerId: String, context: Context) {
        val clazz = Class.forName(BEACON_MAIN_CLASS)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod(
            "startBeacon",
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType,
            Context::class.java
        )
        method.invoke(instance, storeDomain, providerId, false, context)
    }
}
