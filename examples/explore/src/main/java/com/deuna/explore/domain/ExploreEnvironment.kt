package com.deuna.explore.domain

import com.deuna.maven.shared.Environment

enum class ExploreEnvironment(val title: String, val apiBaseURL: String) {
    SANDBOX("Sandbox", "https://api.sandbox.deuna.io"),
    DEVELOPMENT("Develop", "https://api.dev.deuna.io"),
    STAGING("Staging", "https://api.stg.deuna.io");

    val sdkEnvironment: Environment
        get() = when (this) {
            SANDBOX -> Environment.SANDBOX
            DEVELOPMENT -> Environment.DEVELOPMENT
            STAGING -> Environment.STAGING
        }
}
