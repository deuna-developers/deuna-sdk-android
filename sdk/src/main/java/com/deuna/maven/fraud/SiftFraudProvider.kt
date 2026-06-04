package com.deuna.maven.fraud

import android.app.Activity
import android.content.Context
import com.deuna.maven.GenerateFraudId
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json

internal fun GenerateFraudId.runSift(config: Json, providerId: String) {
    val accountId = config["accountId"] as? String
    val beaconKey = config["beaconKey"] as? String
    if (accountId.isNullOrBlank() || beaconKey.isNullOrBlank()) {
        DeunaLogs.warning("[fraud] Missing SIFT.accountId or beaconKey. Native Sift requires both (note: CDL only needs beaconKey). Skipping native init.")
        return
    }
    try {
        SiftNativeBridge.profile(context, accountId, beaconKey, providerId)
        DeunaLogs.info("[fraud] SIFT profile initiated.")
    } catch (error: Throwable) {
        DeunaLogs.warning("[fraud] SIFT native init failed: ${error.message}")
    }
}

private object SiftNativeBridge {
    private const val SIFT_CLASS = "siftscience.android.Sift"
    private const val SIFT_CONFIG_BUILDER_CLASS = "siftscience.android.Sift\$Config\$Builder"

    fun profile(context: Context, accountId: String, beaconKey: String, sessionId: String) {
        if (context !is Activity) {
            DeunaLogs.warning("[fraud] SIFT requires Activity context. Pass Activity to generateFraudId for SIFT to work. Skipping.")
            return
        }
        val siftClass = Class.forName(SIFT_CLASS)
        val configBuilderClass = Class.forName(SIFT_CONFIG_BUILDER_CLASS)

        val builder = configBuilderClass.getDeclaredConstructor().newInstance()
        configBuilderClass.getMethod("withAccountId", String::class.java).invoke(builder, accountId)
        configBuilderClass.getMethod("withBeaconKey", String::class.java).invoke(builder, beaconKey)

        siftClass.getMethod("open", Activity::class.java, configBuilderClass)
            .invoke(null, context, builder)
        siftClass.getMethod("setUserId", String::class.java).invoke(null, sessionId)
        siftClass.getMethod("collect").invoke(null)
    }
}
