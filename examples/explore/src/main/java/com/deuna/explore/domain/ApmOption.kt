package com.deuna.explore.domain

data class ApmOption(
    val label: String,
    val paymentMethods: List<Map<String, Any>>,
    val logo: String,
    val iosCompatible: Boolean,
    val androidCompatible: Boolean,
)
