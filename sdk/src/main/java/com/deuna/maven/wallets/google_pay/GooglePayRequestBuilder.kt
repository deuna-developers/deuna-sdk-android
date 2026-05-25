package com.deuna.maven.wallets.google_pay

import com.deuna.maven.shared.DeunaLogs
import com.google.android.gms.wallet.PaymentDataRequest
import org.json.JSONArray
import org.json.JSONObject

/** Builds the [PaymentDataRequest] JSON required by the Google Pay API. */
internal object GooglePayRequestBuilder {

    fun build(credentials: GooglePayCredentials): PaymentDataRequest {
        val json = JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(buildCardPaymentMethod(credentials)))
            credentials.transactionInfo?.let { put("transactionInfo", buildTransactionInfo(it)) }
            put("merchantInfo", buildMerchantInfo(credentials))
        }
        return PaymentDataRequest.fromJson(json.toString())
    }

    private fun buildCardPaymentMethod(credentials: GooglePayCredentials) = JSONObject().apply {
        put("type", "CARD")
        put("parameters", JSONObject().apply {
            put("allowedAuthMethods", JSONArray(credentials.allowedAuthMethods))
            put("allowedCardNetworks", JSONArray(credentials.allowedCardNetworks))
        })
        put("tokenizationSpecification", buildTokenizationSpec(credentials))
    }

    private fun buildTokenizationSpec(credentials: GooglePayCredentials) = JSONObject().apply {
        if (credentials.tokenizationType == GooglePayTokenizationType.DIRECT) {
            put("type", "DIRECT")
            put("parameters", JSONObject().apply {
                put("protocolVersion", credentials.protocolVersion)
                put("publicKey", credentials.publicKey ?: "")
            })
        } else {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject().apply {
                if (credentials.gateway.isNotEmpty()) put("gateway", credentials.gateway)
                if (credentials.gatewayMerchantId.isNotEmpty()) put("gatewayMerchantId", credentials.gatewayMerchantId)
            })
        }
    }

    private fun buildTransactionInfo(info: GooglePayCredentials.TransactionInfo) = JSONObject().apply {
        put("totalPrice", info.totalPrice)
        put("totalPriceStatus", "FINAL")
        put("currencyCode", info.currencyCode)
        put("countryCode", info.countryCode)
    }

    private fun buildMerchantInfo(credentials: GooglePayCredentials) = JSONObject().apply {
        if (credentials.merchantId.isNotEmpty()) put("merchantId", credentials.merchantId)
        if (credentials.merchantName.isNotEmpty()) put("merchantName", credentials.merchantName)
    }
}
