package com.deuna.maven.shared

enum class Environment(
    val checkoutBaseUrl: String,
    val elementsBaseUrl: String,
    val paymentWidgetBaseUrl: String,
) {
    DEVELOPMENT(
        "https://api.dev.deuna.io",
        "https://elements.dev.deuna.io",
        "https://pay.dev.deuna.com"
    ),
    PRODUCTION(
        "https://api.deuna.io",
        "https://elements.deuna.com",
        "https://pay.deuna.io"
    ),
    STAGING(
        "https://api.stg.deuna.io",
        "https://elements.stg.deuna.io",
        "https://pay.stg.deuna.com"
    ),
    SANDBOX(
        "https://api.sandbox.deuna.io",
        "https://elements.sandbox.deuna.io",
        "https://pay.sandbox.deuna.io"
    )
}