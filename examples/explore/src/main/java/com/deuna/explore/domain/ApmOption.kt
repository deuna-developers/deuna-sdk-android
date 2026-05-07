package com.deuna.explore.domain

data class ApmOption(
    val paymentMethod: String,
    val processor: String,
    val logo: String,
    val iosCompatible: Boolean,
    val androidCompatible: Boolean,
)
