package com.deuna.explore.domain

import org.json.JSONObject

data class IntegrationConfig(
    val environment: ExploreEnvironment,
    val privateKey: String,
    val publicKey: String,
    val orderToken: String,
    val userToken: String,
    val fraudId: String,
    val fraudProvidersJson: String,
    val merchantName: String,
    val merchantCountryCode: String,
    val merchantCurrencyCode: String,
    val hidePayButton: Boolean,
    val enableSplitPayment: Boolean,
    val presentationMode: ExplorePresentationMode,
    val selectedWidget: ExploreWidget,
    val userInfoFirstName: String,
    val userInfoLastName: String,
    val userInfoEmail: String,
) {
    companion object {
        private val defaultFraudProvidersJson = """
            {
              "CYBERSOURCE": {
                "orgId": "your_org_id",
                "merchantId": "your_merchant_id"
              },
              "RISKIFIED": {
                "storeDomain": "your_domain.com"
              }
            }
        """.trimIndent()

        val default = IntegrationConfig(
            environment = ExploreEnvironment.SANDBOX,
            privateKey = "",
            publicKey = "YOUR_PUBLIC_API_KEY",
            orderToken = "",
            userToken = "",
            fraudId = "",
            fraudProvidersJson = defaultFraudProvidersJson,
            merchantName = "",
            merchantCountryCode = "US",
            merchantCurrencyCode = "USD",
            hidePayButton = true,
            enableSplitPayment = false,
            presentationMode = ExplorePresentationMode.MODAL,
            selectedWidget = ExploreWidget.PAYMENT_WIDGET,
            userInfoFirstName = "",
            userInfoLastName = "",
            userInfoEmail = "",
        )

        fun fromJson(jsonStr: String): IntegrationConfig {
            return try {
                val j = JSONObject(jsonStr)
                val d = default
                IntegrationConfig(
                    environment = ExploreEnvironment.entries.firstOrNull { it.name == j.optString("environment") } ?: d.environment,
                    privateKey = j.optString("privateKey", d.privateKey),
                    publicKey = j.optString("publicKey", d.publicKey),
                    orderToken = j.optString("orderToken", d.orderToken),
                    userToken = j.optString("userToken", d.userToken),
                    fraudId = j.optString("fraudId", d.fraudId),
                    fraudProvidersJson = j.optString("fraudProvidersJson", d.fraudProvidersJson),
                    merchantName = j.optString("merchantName", d.merchantName),
                    merchantCountryCode = j.optString("merchantCountryCode", d.merchantCountryCode),
                    merchantCurrencyCode = j.optString("merchantCurrencyCode", d.merchantCurrencyCode),
                    hidePayButton = j.optBoolean("hidePayButton", d.hidePayButton),
                    enableSplitPayment = j.optBoolean("enableSplitPayment", d.enableSplitPayment),
                    presentationMode = ExplorePresentationMode.entries.firstOrNull { it.name == j.optString("presentationMode") } ?: d.presentationMode,
                    selectedWidget = ExploreWidget.entries.firstOrNull { it.name == j.optString("selectedWidget") } ?: d.selectedWidget,
                    userInfoFirstName = j.optString("userInfoFirstName", d.userInfoFirstName),
                    userInfoLastName = j.optString("userInfoLastName", d.userInfoLastName),
                    userInfoEmail = j.optString("userInfoEmail", d.userInfoEmail),
                )
            } catch (e: Exception) {
                default
            }
        }
    }

    fun toJson(): String = JSONObject().apply {
        put("environment", environment.name)
        put("privateKey", privateKey)
        put("publicKey", publicKey)
        put("orderToken", orderToken)
        put("userToken", userToken)
        put("fraudId", fraudId)
        put("fraudProvidersJson", fraudProvidersJson)
        put("merchantName", merchantName)
        put("merchantCountryCode", merchantCountryCode)
        put("merchantCurrencyCode", merchantCurrencyCode)
        put("hidePayButton", hidePayButton)
        put("enableSplitPayment", enableSplitPayment)
        put("presentationMode", presentationMode.name)
        put("selectedWidget", selectedWidget.name)
        put("userInfoFirstName", userInfoFirstName)
        put("userInfoLastName", userInfoLastName)
        put("userInfoEmail", userInfoEmail)
    }.toString()
}

data class DraftConfig(
    val environment: ExploreEnvironment,
    val privateKey: String,
    val publicKey: String,
    val orderToken: String,
    val userToken: String,
    val fraudId: String,
    val fraudProvidersJson: String,
    val merchantName: String,
    val merchantCountryCode: String,
    val merchantCurrencyCode: String,
    val hidePayButton: Boolean,
    val enableSplitPayment: Boolean,
    val presentationMode: ExplorePresentationMode,
    val selectedWidget: ExploreWidget,
    val userInfoFirstName: String,
    val userInfoLastName: String,
    val userInfoEmail: String,
) {
    companion object {
        fun from(config: IntegrationConfig) = DraftConfig(
            environment = config.environment,
            privateKey = config.privateKey,
            publicKey = config.publicKey,
            orderToken = config.orderToken,
            userToken = config.userToken,
            fraudId = config.fraudId,
            fraudProvidersJson = config.fraudProvidersJson,
            merchantName = config.merchantName,
            merchantCountryCode = config.merchantCountryCode,
            merchantCurrencyCode = config.merchantCurrencyCode,
            hidePayButton = config.hidePayButton,
            enableSplitPayment = config.enableSplitPayment,
            presentationMode = config.presentationMode,
            selectedWidget = config.selectedWidget,
            userInfoFirstName = config.userInfoFirstName,
            userInfoLastName = config.userInfoLastName,
            userInfoEmail = config.userInfoEmail,
        )
    }

    fun toIntegrationConfig() = IntegrationConfig(
        environment = environment,
        privateKey = privateKey,
        publicKey = publicKey,
        orderToken = orderToken,
        userToken = userToken,
        fraudId = fraudId,
        fraudProvidersJson = fraudProvidersJson,
        merchantName = merchantName,
        merchantCountryCode = merchantCountryCode,
        merchantCurrencyCode = merchantCurrencyCode,
        hidePayButton = hidePayButton,
        enableSplitPayment = enableSplitPayment,
        presentationMode = presentationMode,
        selectedWidget = selectedWidget,
        userInfoFirstName = userInfoFirstName,
        userInfoLastName = userInfoLastName,
        userInfoEmail = userInfoEmail,
    )
}
