package com.deuna.maven.wallets

enum class WalletProvider(val processorName: String) {
    GOOGLE_PAY("google_pay");

    companion object {
        fun fromProcessorName(name: String): WalletProvider? =
            entries.firstOrNull { it.processorName == name }
    }
}
