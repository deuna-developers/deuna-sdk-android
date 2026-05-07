package com.deuna.maven.wallets.google_pay

import com.deuna.maven.shared.DeunaHttpClient
import com.deuna.maven.shared.Environment
import org.json.JSONObject
import java.net.URLEncoder

internal object TokenizeWalletCard {

    /**
     * POST {checkoutBaseUrl}/users/{userId}/cards
     * Body: { "google_pay": { "paymentData": paymentData }, "credential_source": "google_pay" }
     */
    fun tokenize(
        environment: Environment,
        publicApiKey: String,
        userId: String,
        userToken: String,
        paymentDataJson: String,
    ): JSONObject {
        val url = "${environment.checkoutBaseUrl}/users/${URLEncoder.encode(userId, "UTF-8")}/cards"

        val body = JSONObject().apply {
            put("google_pay", JSONObject().apply { put("paymentData", JSONObject(paymentDataJson)) })
            put("credential_source", "google_pay")
        }

        return DeunaHttpClient.post(
            url = url,
            headers = mapOf(
                "Authorization" to "Bearer $userToken",
                "x-api-key" to publicApiKey,
            ),
            body = body,
        )
    }
}
