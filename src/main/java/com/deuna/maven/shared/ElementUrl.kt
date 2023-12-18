package com.deuna.maven.shared

enum class ElementUrl (var url: String) {
    DEVELOPMENT("https://elements.dev.deuna.io"),
    STAGING("https://elements.stg.deuna.io"),
    PRODUCTION("https://elements.deuna.io"),
    SANDBOX("https://elements.sbx.deuna.io");
}