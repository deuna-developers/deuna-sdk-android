package com.deuna.maven.wallets

import android.content.Context
import com.deuna.maven.shared.Environment
import com.deuna.maven.wallets.google_pay.GooglePayWalletHandler

sealed class WalletLaunchResult {
    data class Success(val rawData: Map<String, Any?>) : WalletLaunchResult()
    data class Error(val code: String, val message: String) : WalletLaunchResult()
    object Closed : WalletLaunchResult()
}

/**
 * Contract for a wallet provider's launch logic.
 * Handlers only manage native payment UI and return raw payment data.
 * Tokenization is the caller's responsibility.
 * Add a new wallet by implementing this interface and registering it in [WalletHandlerRegistry].
 */
internal interface WalletHandler {
    val provider: WalletProvider

    /**
     * Returns true if this wallet is available on the current device.
     * Called on a background thread.
     */
    fun isAvailableOnDevice(context: Context, environment: Environment): Boolean

    /**
     * Starts the wallet payment flow and returns raw payment data via [onResult].
     * Never tokenizes — callers are responsible for tokenization.
     */
    fun launch(
        context: Context,
        environment: Environment,
        credentials: WalletCredentials,
        onResult: (WalletLaunchResult) -> Unit,
    )
}

/** Maps each [WalletProvider] to its [WalletHandler]. Add new wallets here. */
internal object WalletHandlerRegistry {
    private val handlers: Map<WalletProvider, WalletHandler> = mapOf(
        WalletProvider.GOOGLE_PAY to GooglePayWalletHandler,
    )

    fun get(provider: WalletProvider): WalletHandler? = handlers[provider]
}
