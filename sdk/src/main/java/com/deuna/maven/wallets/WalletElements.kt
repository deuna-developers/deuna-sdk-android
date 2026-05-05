package com.deuna.maven.wallets

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.deuna.maven.shared.DeunaHttpClient
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.widgets.elements_widget.ElementsError
import java.net.URLEncoder
import java.util.concurrent.Executors

internal class WalletElements(
    private val context: Context,
    private val environment: Environment,
    private val publicApiKey: String,
    private val requestedProvider: WalletProvider,
    private val orderToken: String?,
    private val userInfo: UserInfo?,
    private val callbacks: ElementsCallbacks,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val workers = Executors.newSingleThreadExecutor()

    fun run() {
        val cached = GetWalletsAvailable.cachedWallets
            ?: return dispatchError("NOT_INITIALIZED", "Call getWalletsAvailable() before initElements().")

        if (requestedProvider !in cached) {
            return dispatchError("WALLET_UNAVAILABLE", "$requestedProvider is not available on this device.")
        }

        val handler = WalletHandlerRegistry.get(requestedProvider)
            ?: return dispatchError("UNSUPPORTED_WALLET", "$requestedProvider has no registered handler.")

        workers.execute {
            try {
                val fetchResult = fetchCredentials()
                if (fetchResult.userToken.isNullOrEmpty() || fetchResult.userId.isNullOrEmpty()) {
                    dispatchError("MISSING_USER_AUTH", "userToken or userId is missing — cannot tokenize wallet payment.")
                    return@execute
                }
                handler.launch(context, environment, publicApiKey, fetchResult, callbacks)
            } catch (e: Exception) {
                DeunaLogs.error("[wallets] WalletElements failed: ${e.message}")
                dispatchError("WALLET_ELEMENTS_FAILED", e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchCredentials(): WalletFetchResult {
        if (orderToken == null) {
            return WalletFetchResult(
                credentials = GetWalletsAvailable.cachedCredentials,
                userToken = null,
                userId = null,
            )
        }

        val url = "${environment.elementsBaseUrl}/api/vault?orderToken=${URLEncoder.encode(orderToken, "UTF-8")}"
        val response = DeunaHttpClient.post(
            url = url,
            headers = mapOf("x-api-key" to publicApiKey),
            body = VaultResponseParser.buildUserInfoBody(userInfo),
        )
        return VaultResponseParser.parseFetchResult(response)
    }

    private fun dispatchError(code: String, message: String) {
        val error = ElementsError(
            type = ElementsError.Type.UNKNOWN_ERROR,
            metadata = ElementsError.Metadata(code = code, message = message),
        )
        mainHandler.post { callbacks.onError?.invoke(error) }
    }
}
