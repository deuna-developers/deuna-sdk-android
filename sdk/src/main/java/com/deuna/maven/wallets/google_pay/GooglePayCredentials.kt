package com.deuna.maven.wallets.google_pay

import com.deuna.maven.wallets.WalletCredentials

internal enum class GooglePayTokenizationType { PAYMENT_GATEWAY, DIRECT }

internal data class GooglePayCredentials(
    val merchantId: String = "",
    val merchantName: String = "",
    val gateway: String = "",
    val gatewayMerchantId: String = "",
    val tokenizationType: GooglePayTokenizationType = GooglePayTokenizationType.PAYMENT_GATEWAY,
    val publicKey: String? = null,
    val protocolVersion: String = "ECv2",
    val allowedCardNetworks: List<String> = DEFAULT_CARD_NETWORKS,
    val allowedAuthMethods: List<String> = DEFAULT_AUTH_METHODS,
    val transactionInfo: TransactionInfo? = null,
) : WalletCredentials {

    data class TransactionInfo(
        val totalPrice: String,
        val currencyCode: String,
        val countryCode: String,
    )

    companion object {
        val DEFAULT_CARD_NETWORKS = listOf("AMEX", "DISCOVER", "MASTERCARD", "VISA")
        val DEFAULT_AUTH_METHODS = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    }
}
