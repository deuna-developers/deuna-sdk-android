package com.deuna.explore.data

import com.deuna.explore.domain.ExploreEnvironment
import com.deuna.explore.domain.ExploreMerchantProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MerchantService {
    sealed class ServiceError(message: String) : Exception(message) {
        class InvalidURL : ServiceError("Invalid API URL.")
        class InvalidResponse : ServiceError("Unexpected API response.")
        class MerchantProfileNotFound : ServiceError("Unable to fetch merchant information with the provided private key.")
        class Api(message: String) : ServiceError(message)
    }

    suspend fun loadMerchantProfile(
        environment: ExploreEnvironment,
        privateKey: String,
    ): ExploreMerchantProfile = withContext(Dispatchers.IO) {
        val url = URL("${environment.apiBaseURL}/merchants")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("X-Api-Key", privateKey)
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000

            val statusCode = conn.responseCode
            val body = if (statusCode in 200..299) {
                conn.inputStream.bufferedReader().readText()
            } else {
                val errBody = conn.errorStream?.bufferedReader()?.readText() ?: ""
                val apiMsg = extractApiMessage(errBody)
                    ?: "Merchant request failed ($statusCode)."
                throw ServiceError.Api(apiMsg)
            }

            val json = JSONObject(body)
            val root = json.optJSONObject("data") ?: json
            val name = root.optString("merchant_name").ifEmpty { root.optString("name") }
            val countryCode = (root.optString("country_iso").ifEmpty {
                root.optString("country_code").ifEmpty { root.optString("country").ifEmpty { "US" } }
            }).uppercase()
            val currencyCode = (root.optString("currency_iso").ifEmpty {
                root.optString("currency").ifEmpty { "USD" }
            }).uppercase()

            if (countryCode.isEmpty() || currencyCode.isEmpty()) {
                throw ServiceError.MerchantProfileNotFound()
            }

            ExploreMerchantProfile(
                name = name,
                countryCode = countryCode,
                currencyCode = currencyCode,
            )
        } finally {
            conn.disconnect()
        }
    }

    private fun extractApiMessage(body: String): String? {
        return try {
            val j = JSONObject(body)
            j.optString("message").ifEmpty { j.optString("error") }.ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }
}
