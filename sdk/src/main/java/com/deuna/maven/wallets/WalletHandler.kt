package com.deuna.maven.wallets

import android.content.Context
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Environment
import com.deuna.maven.wallets.google_pay.GooglePayWalletHandler

/**
 * Contract for a wallet provider's launch logic.
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
     * Starts the wallet payment flow. Called on a background thread.
     * Responsible for launching any UI (Activity, sheet, etc.) and wiring [callbacks].
     */
    fun launch(
        context: Context,
        environment: Environment,
        publicApiKey: String,
        fetchResult: WalletFetchResult,
        callbacks: ElementsCallbacks,
    )
}

/** Maps each [WalletProvider] to its [WalletHandler]. Add new wallets here. */
internal object WalletHandlerRegistry {
    private val handlers: Map<WalletProvider, WalletHandler> = mapOf(
        WalletProvider.GOOGLE_PAY to GooglePayWalletHandler,
    )

    fun get(provider: WalletProvider): WalletHandler? = handlers[provider]
}
