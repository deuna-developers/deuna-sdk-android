package com.deuna.maven.wallets

internal data class WalletFetchResult(
    val credentials: Map<WalletProvider, WalletCredentials>,
    val userToken: String?,
    val userId: String?,
)
