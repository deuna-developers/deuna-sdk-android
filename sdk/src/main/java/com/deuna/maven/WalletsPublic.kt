package com.deuna.maven

import android.content.Context
import com.deuna.maven.wallets.WalletHandlerRegistry
import com.deuna.maven.wallets.WalletLaunchResult
import com.deuna.maven.wallets.WalletProvider
import com.deuna.maven.wallets.google_pay.GooglePayCredentials
import com.deuna.maven.wallets.google_pay.GooglePayTokenizationType

/**
 * Launches the wallet payment sheet using pre-fetched credentials from the caller.
 * Returns raw payment data — no tokenization is performed.
 * No vault API call is made — the caller is responsible for fetching credentials.
 */
fun DeunaSDK.launchWallet(
    context: Context,
    provider: String,
    credentials: Map<String, Any>,
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

    handler.launch(context, environment, walletCredentials, onResult)
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
