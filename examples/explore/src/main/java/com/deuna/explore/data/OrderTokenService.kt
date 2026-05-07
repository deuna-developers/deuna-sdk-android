package com.deuna.explore.data

import com.deuna.explore.domain.ExploreEnvironment
import com.deuna.explore.domain.ExploreProduct
import com.deuna.explore.domain.OrderTokenResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class OrderTokenService(
    private val merchantService: MerchantService = MerchantService(),
) {
    sealed class ServiceError(message: String) : Exception(message) {
        class InvalidURL : ServiceError("Invalid API URL.")
        class InvalidResponse : ServiceError("Unexpected API response.")
        class TokenNotFound : ServiceError("Order token was not returned by the API.")
        class Api(message: String) : ServiceError(message)
    }

    suspend fun createOrderToken(
        environment: ExploreEnvironment,
        privateKey: String,
        products: List<ExploreProduct>,
    ): OrderTokenResult {
        val merchantProfile = merchantService.loadMerchantProfile(environment, privateKey)
        val payload = buildOrderPayload(merchantProfile.currencyCode, merchantProfile.countryCode, products)
        val token = tokenizeOrder(environment, privateKey, payload)
        return OrderTokenResult(orderToken = token, merchantProfile = merchantProfile)
    }

    private suspend fun tokenizeOrder(
        environment: ExploreEnvironment,
        privateKey: String,
        payload: JSONObject,
    ): String = withContext(Dispatchers.IO) {
        val url = URL("${environment.apiBaseURL}/merchants/orders")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("X-Api-Key", privateKey)
            conn.doOutput = true
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000

            conn.outputStream.bufferedWriter().use { it.write(payload.toString()) }

            val statusCode = conn.responseCode
            val body = if (statusCode in 200..299) {
                conn.inputStream.bufferedReader().readText()
            } else {
                val errBody = conn.errorStream?.bufferedReader()?.readText() ?: ""
                val apiMsg = extractApiMessage(errBody)
                    ?: "Order tokenization failed ($statusCode)."
                throw ServiceError.Api(apiMsg)
            }

            val json = JSONObject(body)
            val token = json.optString("token")
            if (token.isEmpty()) throw ServiceError.TokenNotFound()
            token
        } finally {
            conn.disconnect()
        }
    }

    private fun buildOrderPayload(
        currencyCode: String,
        countryCode: String,
        products: List<ExploreProduct>,
    ): JSONObject {
        val normalized = currencyCode.uppercase()
        val zeroDecimals = normalized == "COP" || normalized == "CLP"
        val decimals = if (zeroDecimals) 0 else 2
        val totalAmount = products.sumOf { it.priceInCents }
        val displayAmount = "$normalized ${formatAmount(totalAmount, decimals)}"
        val seed = addressSeed(countryCode)
        val email = "explore-android+${UUID.randomUUID().toString().take(8)}@deuna.test"

        val address = JSONObject().apply {
            put("first_name", "Explore")
            put("last_name", "Tester")
            put("phone", "+593999999999")
            put("identity_document", "1234567890")
            put("lat", seed.lat)
            put("lng", seed.lng)
            put("address1", "Main Street 123")
            put("address2", "")
            put("city", seed.city)
            put("zipcode", seed.zipcode)
            put("state_name", seed.stateName)
            put("country", seed.countryName)
            put("country_code", seed.countryCode)
            put("email", email)
        }

        val items = JSONArray()
        products.forEach { product ->
            val itemDisplay = "$normalized ${formatAmount(product.priceInCents, product.fractionDigits)}"
            items.put(JSONObject().apply {
                put("id", product.id)
                put("name", product.name)
                put("description", "Product from Explore Android sample")
                put("quantity", 1)
                put("sku", product.id.uppercase())
                put("category", "sample")
                put("total_amount", JSONObject().apply {
                    put("amount", product.priceInCents)
                    put("display_amount", itemDisplay)
                })
                put("unit_price", JSONObject().apply {
                    put("amount", product.priceInCents)
                    put("display_amount", itemDisplay)
                })
            })
        }

        val order = JSONObject().apply {
            put("order_id", UUID.randomUUID().toString())
            put("store_code", "all")
            put("currency", normalized)
            put("tax_amount", 0)
            put("shipping_amount", 0)
            put("items_total_amount", totalAmount)
            put("sub_total", totalAmount)
            put("total_amount", totalAmount)
            put("display_total_amount", displayAmount)
            put("items", items)
            put("discounts", JSONArray())
            put("shipping_address", address)
            put("billing_address", address)
            put("status", "pending")
            put("timezone", "America/Guayaquil")
        }

        return JSONObject().apply {
            put("order_type", "DEUNA_NOW")
            put("order", order)
        }
    }

    private data class AddressSeed(
        val city: String,
        val stateName: String,
        val countryName: String,
        val countryCode: String,
        val zipcode: String,
        val lat: Double,
        val lng: Double,
    )

    private fun addressSeed(countryCode: String): AddressSeed = when (countryCode.uppercase()) {
        "MX" -> AddressSeed("Ciudad de Mexico", "CDMX", "Mexico", "MX", "06600", 19.4326, -99.1332)
        "CO" -> AddressSeed("Bogota", "Cundinamarca", "Colombia", "CO", "110111", 4.711, -74.0721)
        "CL" -> AddressSeed("Santiago", "Region Metropolitana", "Chile", "CL", "8320000", -33.4489, -70.6693)
        "PE" -> AddressSeed("Lima", "Lima", "Peru", "PE", "15001", -12.0464, -77.0428)
        "EC" -> AddressSeed("Quito", "Pichincha", "Ecuador", "EC", "170150", -0.1807, -78.4678)
        else -> AddressSeed("Miami", "Florida", "United States", "US", "33101", 25.7617, -80.1918)
    }

    private fun formatAmount(cents: Int, decimals: Int): String {
        val divisor = if (decimals == 0) 1.0 else 100.0
        val value = cents / divisor
        return if (decimals == 0) value.toLong().toString()
        else String.format("%.${decimals}f", value)
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
