package com.deuna.maven.shared

enum class Environment(
    val checkoutBaseUrl: String,
    val elementsBaseUrl: String
) {
    DEVELOPMENT(
        "https://api.dev.deuna.io",
        "https://elements.dev.deuna.io"
    ),
    PRODUCTION(
        "https://api.deuna.io",
        "https://elements.deuna.com"
    ),
    STAGING(
        "https://api.stg.deuna.io",
        "https://elements.stg.deuna.io"
    ),
    SANDBOX(
        "https://api.sandbox.deuna.io",
        "https://elements.sandbox.deuna.io"
    )
}