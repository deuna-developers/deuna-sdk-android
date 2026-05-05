package com.deuna.maven

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.wallets.WalletFetchResult
import com.deuna.maven.wallets.WalletHandlerRegistry
import com.deuna.maven.wallets.WalletProvider
import com.deuna.maven.wallets.google_pay.GooglePayCredentials
import com.deuna.maven.wallets.google_pay.GooglePayTokenizationType
import com.deuna.maven.widgets.elements_widget.ElementsError

private val mainHandler = Handler(Looper.getMainLooper())

/**
 * Returns true if the given wallet provider is available on this device.
 * Does NOT make any network calls.
 */
fun DeunaSDK.isWalletAvailableOnDevice(context: Context, provider: String): Boolean {
    val walletProvider = WalletProvider.fromProcessorName(provider) ?: return false
    return WalletHandlerRegistry.get(walletProvider)
        ?.isAvailableOnDevice(context, environment) ?: false
}

/**
 * Launches the wallet payment sheet using pre-fetched credentials from the caller.
 * No vault API call is made — the caller is responsible for fetching credentials.
 */
fun DeunaSDK.launchWallet(
    context: Context,
    provider: String,
    credentials: Map<String, Any>,
    userToken: String?,
    userId: String?,
    callbacks: ElementsCallbacks,
) {
    val walletProvider = WalletProvider.fromProcessorName(provider)
    if (walletProvider == null) {
        dispatchLaunchError(callbacks, "UNSUPPORTED_WALLET", "Unknown wallet provider: $provider")
        return
    }

    val walletCredentials = when (walletProvider) {
        WalletProvider.GOOGLE_PAY -> parseGooglePayCredentials(credentials)
    }

    val fetchResult = WalletFetchResult(
        credentials = mapOf(walletProvider to walletCredentials),
        userToken = userToken,
        userId = userId,
    )

    val handler = WalletHandlerRegistry.get(walletProvider)
    if (handler == null) {
        dispatchLaunchError(callbacks, "NO_HANDLER", "No handler registered for $provider")
        return
    }

    handler.launch(context, environment, publicApiKey, fetchResult, callbacks)
}

private fun dispatchLaunchError(callbacks: ElementsCallbacks, code: String, message: String) {
    val err = ElementsError(
        type = ElementsError.Type.UNKNOWN_ERROR,
        metadata = ElementsError.Metadata(code = code, message = message),
    )
    mainHandler.post { callbacks.onError?.invoke(err) }
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
