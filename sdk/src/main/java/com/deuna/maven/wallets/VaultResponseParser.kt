package com.deuna.maven.wallets

import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.wallets.google_pay.GooglePayCredentials
import com.deuna.maven.wallets.google_pay.GooglePayTokenizationType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Parses responses from POST /api/vault into domain objects.
 *
 * Used by both [GetWalletsAvailable] (provider list only) and
 * [WalletElements] (full fetch result with credentials + user auth).
 */
internal object VaultResponseParser {

    data class ProvidersResult(
        val providers: List<WalletProvider>,
        val credentials: Map<WalletProvider, WalletCredentials>,
    )

    fun parseProviders(root: JSONObject): ProvidersResult {
        val paymentMethods = root.optJSONArray("paymentMethods")
            ?: return ProvidersResult(emptyList(), emptyMap())
        val merchant = root.optJSONObject("checkout")?.optJSONObject("merchant")
        val providers = mutableListOf<WalletProvider>()
        val credentialsMap = mutableMapOf<WalletProvider, WalletCredentials>()

        for (i in 0 until paymentMethods.length()) {
            val method = paymentMethods.optJSONObject(i) ?: continue
            val provider = WalletProvider.fromProcessorName(method.optString("processor_name"))
                ?: continue
            if (provider !in providers) {
                providers.add(provider)
                when (provider) {
                    WalletProvider.GOOGLE_PAY ->
                        credentialsMap[provider] = parseGooglePayCredentials(method, merchant)
                }
            }
        }
        return ProvidersResult(providers, credentialsMap)
    }

    fun parseFetchResult(root: JSONObject): WalletFetchResult {
        val paymentMethods = root.optJSONArray("paymentMethods")
        val merchant = root.optJSONObject("checkout")?.optJSONObject("merchant")
        val order = root.optJSONObject("checkout")?.optJSONObject("order")?.optJSONObject("order")
        val userAuthData = root.optJSONObject("userAuthResponse")?.optJSONObject("data")

        val credentialsMap = mutableMapOf<WalletProvider, WalletCredentials>()
        if (paymentMethods != null) {
            for (i in 0 until paymentMethods.length()) {
                val method = paymentMethods.optJSONObject(i) ?: continue
                val provider = WalletProvider.fromProcessorName(method.optString("processor_name"))
                    ?: continue
                when (provider) {
                    WalletProvider.GOOGLE_PAY ->
                        credentialsMap[provider] =
                            parseGooglePayCredentials(method, merchant, order)
                }
            }
        }

        return WalletFetchResult(
            credentials = credentialsMap,
            userToken = userAuthData?.optString("user_token")?.takeIf { it.isNotEmpty() },
            userId = userAuthData?.optString("user_id")?.takeIf { it.isNotEmpty() },
        )
    }

    fun buildUserInfoBody(userInfo: UserInfo?): JSONObject? {
        if (userInfo == null || userInfo.email.isEmpty()) {
            return null
        }

        val email = userInfo.email
        val firstName = userInfo.firstName
        val lastName = userInfo.lastName

        return JSONObject().apply {
            put("email", email)
            if (!firstName.isNullOrEmpty()) put("firstName", firstName)
            if (!lastName.isNullOrEmpty()) put("lastName", lastName)
        }
    }

    private fun parseGooglePayCredentials(
        method: JSONObject,
        merchant: JSONObject?,
        order: JSONObject? = null,
    ): GooglePayCredentials {
        val creds = method.optJSONObject("credentials") ?: JSONObject()
        val extraParams = method.optJSONObject("extra_params") ?: JSONObject()
        val merchantId = creds.optString("external_merchant_id", "")
        val gatewayRaw = extraParams.optString("gateway", "")

        return GooglePayCredentials(
            merchantId = merchantId,
            merchantName = merchant?.optString("name", "") ?: "",
            gateway = gatewayRaw,
            gatewayMerchantId = merchantId,
            tokenizationType = if (gatewayRaw.equals("DIRECT", ignoreCase = true))
                GooglePayTokenizationType.DIRECT else GooglePayTokenizationType.PAYMENT_GATEWAY,
            publicKey = creds.optString("public_api_key", "").takeIf { it.isNotEmpty() },
            allowedCardNetworks = extraParams.optJSONArray("allowed_card_networks")?.toStringList()
                ?: GooglePayCredentials.DEFAULT_CARD_NETWORKS,
            allowedAuthMethods = extraParams.optJSONArray("allowed_auth_methods")?.toStringList()
                ?: GooglePayCredentials.DEFAULT_AUTH_METHODS,
            transactionInfo = parseTransactionInfo(order, merchant),
        )
    }

    private fun parseTransactionInfo(
        order: JSONObject?,
        merchant: JSONObject?,
    ): GooglePayCredentials.TransactionInfo? {
        val currency = order?.optString("currency", "") ?: return null
        val country = merchant?.optString("country", "") ?: return null
        if (currency.isEmpty() || country.isEmpty()) return null
        return GooglePayCredentials.TransactionInfo(
            totalPrice = "%.2f".format((order.optInt("total_amount", 0)) / 100.0),
            currencyCode = currency.uppercase(),
            countryCode = country.uppercase(),
        )
    }

    private fun JSONArray.toStringList(): List<String> = (0 until length()).map { getString(it) }
}
