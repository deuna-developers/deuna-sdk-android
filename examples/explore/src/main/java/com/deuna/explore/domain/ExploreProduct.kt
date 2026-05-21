package com.deuna.explore.domain

data class ExploreMerchantProfile(
    val name: String,
    val countryCode: String,
    val currencyCode: String,
)

data class ExploreProduct(
    val id: String,
    val name: String,
    val image: String,
    val priceInCents: Int,
    val fractionDigits: Int,
    val currencyCode: String,
    val currencySymbol: String,
)

data class OrderTokenResult(
    val orderToken: String,
    val merchantProfile: ExploreMerchantProfile,
)
