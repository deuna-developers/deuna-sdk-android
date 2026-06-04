package com.deuna.maven.fraud

import android.content.Context
import com.deuna.maven.GenerateFraudId
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal fun GenerateFraudId.runAccertify(config: Json, providerId: String) {
    try {
        // 1. Resolve MMEWrapper and callback class
        val mmeWrapperClass = Class.forName("com.inmobile.MMEWrapper")
        val callbackInterface = Class.forName("com.inmobile.MMEWrapperCallback")

        // 2. Resolve singleton INSTANCE
        val wrapperInstance = mmeWrapperClass.getDeclaredField("INSTANCE").get(null)

        // 3. Resolve disclosure constants dynamically from com.inmobile.MMEConstants$DISCLOSURES
        val disclosureClass = try {
            Class.forName("com.inmobile.MMEConstants\$DISCLOSURES")
        } catch (e: Throwable) {
            DeunaLogs.error("[fraud] ACCERTIFY: Failed to load MMEConstants\$DISCLOSURES", e)
            null
        }

        // 4. Read dynamic consent values from config JSON or default to true
        val locationConsent = config["locationConsent"] as? Boolean ?: true
        val phoneConsent = config["phoneConsent"] as? Boolean ?: true

        // 5. Construct disclosure map with resolved enum/field keys
        val disclosureMap = mutableMapOf<Any, Boolean>()
        if (disclosureClass != null) {
            val locationEnum = if (disclosureClass.isEnum) {
                disclosureClass.enumConstants?.firstOrNull { (it as Enum<*>).name == "LOCATION" }
            } else {
                disclosureClass.getField("LOCATION").get(null)
            }
            val phoneEnum = if (disclosureClass.isEnum) {
                disclosureClass.enumConstants?.firstOrNull { (it as Enum<*>).name == "PHONE_STATE" }
            } else {
                disclosureClass.getField("PHONE_STATE").get(null)
            }

            if (locationEnum != null) {
                disclosureMap[locationEnum] = locationConsent
            }
            if (phoneEnum != null) {
                disclosureMap[phoneEnum] = phoneConsent
            }
        }

        DeunaLogs.info("[fraud] ACCERTIFY starting sequence — sessionId=$providerId")

        // 6. Create callback proxy for start method
        val startLatch = CountDownLatch(1)
        var startSucceeded = false
        var startException: Throwable? = null

        val startCallback = Proxy.newProxyInstance(
            callbackInterface.classLoader,
            arrayOf(callbackInterface)
        ) { _, method, args ->
            if (method.name == "onComplete") {
                val success = args[0] as Boolean
                val exception = args[2] as? Exception
                startSucceeded = success
                startException = exception
                startLatch.countDown()
            }
            null
        }

        // Find the method: start(Context, MMEWrapperCallback)
        val startMethod = mmeWrapperClass.getMethod("start", Context::class.java, callbackInterface)
        startMethod.invoke(wrapperInstance, context, startCallback)

        val completedStart = startLatch.await(8, TimeUnit.SECONDS)
        if (!completedStart) {
            DeunaLogs.error("[fraud] ACCERTIFY ✘ start timed out after 8s")
            return
        }

        if (!startSucceeded) {
            DeunaLogs.error("[fraud] ACCERTIFY ✘ start failed", startException)
            return
        }

        DeunaLogs.info("[fraud] ACCERTIFY ✔ started. Sending device data...")

        // 7. Create callback proxy for sendDeviceData method
        val sendLatch = CountDownLatch(1)
        var sendSucceeded = false
        var sendException: Throwable? = null

        val sendCallback = Proxy.newProxyInstance(
            callbackInterface.classLoader,
            arrayOf(callbackInterface)
        ) { _, method, args ->
            if (method.name == "onComplete") {
                val success = args[0] as Boolean
                val exception = args[2] as? Exception
                sendSucceeded = success
                sendException = exception
                sendLatch.countDown()
            }
            null
        }

        // Find method: sendDeviceData(Context, String, Map, MMEWrapperCallback)
        val sendMethod = mmeWrapperClass.getMethod(
            "sendDeviceData",
            Context::class.java,
            String::class.java,
            Map::class.java,
            callbackInterface
        )
        sendMethod.invoke(wrapperInstance, context, providerId, disclosureMap, sendCallback)

        val completedSend = sendLatch.await(8, TimeUnit.SECONDS)
        if (!completedSend) {
            DeunaLogs.error("[fraud] ACCERTIFY ✘ sendDeviceData timed out after 8s")
            return
        }

        if (sendSucceeded) {
            DeunaLogs.info("[fraud] ACCERTIFY ✔ fraud ID generated — sessionId=$providerId")
        } else {
            DeunaLogs.error("[fraud] ACCERTIFY ✘ sendDeviceData failed", sendException)
        }

    } catch (e: ClassNotFoundException) {
        DeunaLogs.error("[fraud] ACCERTIFY not linked. Add Accertify AAR dependency to your app.")
    } catch (e: Throwable) {
        DeunaLogs.error("[fraud] ACCERTIFY ✘ native integration failed: ${e.message}", e)
    }
}
