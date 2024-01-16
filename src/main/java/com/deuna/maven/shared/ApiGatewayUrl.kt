package com.deuna.maven.shared

/**
 * The ApiGatewayUrl enum class is used to set the API Gateway URL based on the environment.

 */
enum class ApiGatewayUrl(val url: String)  {
    DEVELOPMENT("https://api.dev.deuna.io"),
    STAGING("https://api.stg.deuna.io"),
    PRODUCTION("https://api.deuna.io"),
    SANDBOX("https://api.sbx.deuna.io");
}