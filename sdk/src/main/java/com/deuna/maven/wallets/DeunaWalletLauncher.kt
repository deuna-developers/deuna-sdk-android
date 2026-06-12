package com.deuna.maven.wallets

import android.content.Context
import com.deuna.maven.shared.Environment
import com.deuna.maven.wallets.google_pay.GooglePayCredentials
import com.deuna.maven.wallets.google_pay.GooglePayTokenizationType

/**
 * Standalone wallet launcher — no DeunaSDK instance required.
 * Used by @deuna/react-native-sdk native module.
 */
object DeunaWalletLauncher {

    /**
     * Returns true if the provider is available on this device.
     * Does NOT make any network calls.
     */
    fun isAvailable(context: Context, provider: String, environment: String): Boolean {
        val walletProvider = WalletProvider.fromProcessorName(provider) ?: return false
        return WalletHandlerRegistry.get(walletProvider)
            ?.isAvailableOnDevice(context, environmentFrom(environment)) ?: false
    }

    /**
     * Launches the wallet payment sheet and returns raw payment data via onResult.
     * No tokenization — caller is responsible for POST to /users/{id}/cards.
     */
    fun launch(
        context: Context,
        provider: String,
        credentials: Map<String, Any>,
        environment: String,
        onResult: (WalletLaunchResult) -> Unit,
    ) {
        val walletProvider = WalletProvider.fromProcessorName(provider)
        if (walletProvider == null) {
            onResult(WalletLaunchResult.Error("UNSUPPORTED_WALLET", "Unknown wallet provider: $provider"))
            return
        }

        val walletCredentials = when (walletProvider) {
            WalletProvider.GOOGLE_PAY -> parseGooglePayCredentials(credentials)
        }

        val handler = WalletHandlerRegistry.get(walletProvider)
        if (handler == null) {
            onResult(WalletLaunchResult.Error("NO_HANDLER", "No handler registered for $provider"))
            return
        }

        handler.launch(context, environmentFrom(environment), walletCredentials, onResult)
    }

    private fun environmentFrom(s: String) = when (s.lowercase()) {
        "production" -> Environment.PRODUCTION
        "staging" -> Environment.STAGING
        "sandbox" -> Environment.SANDBOX
        else -> Environment.DEVELOPMENT
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseGooglePayCredentials(map: Map<String, Any>): GooglePayCredentials {
        val ti = map["transactionInfo"] as? Map<String, Any>
        return GooglePayCredentials(
            merchantId = map["merchantId"] as? String ?: "",
            merchantName = map["merchantName"] as? String ?: "",
            gateway = map["gateway"] as? String ?: "",
            gatewayMerchantId = map["gatewayMerchantId"] as? String ?: "",
            tokenizationType = if ((map["tokenizationType"] as? String).equals("DIRECT", ignoreCase = true))
                GooglePayTokenizationType.DIRECT else GooglePayTokenizationType.PAYMENT_GATEWAY,
            publicKey = map["publicKey"] as? String,
            allowedCardNetworks = (map["allowedCardNetworks"] as? List<*>)
                ?.filterIsInstance<String>() ?: GooglePayCredentials.DEFAULT_CARD_NETWORKS,
            allowedAuthMethods = (map["allowedAuthMethods"] as? List<*>)
                ?.filterIsInstance<String>() ?: GooglePayCredentials.DEFAULT_AUTH_METHODS,
            transactionInfo = ti?.let {
                GooglePayCredentials.TransactionInfo(
                    totalPrice = it["totalPrice"] as? String ?: "0.00",
                    currencyCode = it["currencyCode"] as? String ?: "",
                    countryCode = it["countryCode"] as? String ?: "",
                )
            },
        )
    }
}
