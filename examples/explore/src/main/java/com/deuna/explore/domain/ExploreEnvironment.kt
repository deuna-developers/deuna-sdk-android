package com.deuna.explore.domain

import com.deuna.maven.shared.Environment

enum class ExploreEnvironment(val title: String, private val _apiBaseURL: String) {
    SANDBOX("Sandbox", "https://api.sandbox.deuna.io"),
    DEVELOPMENT("Develop", "https://api.dev.deuna.io"),
    STAGING("Staging", "https://api.stg.deuna.io"),
    PREPROD("Preprod", "http://apigw:8080");

    val apiBaseURL: String
        get() = when (this) {
            PREPROD -> dynamicPreprodEndpoint ?: System.getenv("DEUNA_API_ENDPOINT") ?: _apiBaseURL
            else -> _apiBaseURL
        }

    val sdkEnvironment: Environment
        get() = when (this) {
            SANDBOX -> Environment.SANDBOX
            DEVELOPMENT -> Environment.DEVELOPMENT
            STAGING -> Environment.STAGING
            PREPROD -> Environment.STAGING
        }

    companion object {
        var dynamicPreprodEndpoint: String? = null
    }
}
