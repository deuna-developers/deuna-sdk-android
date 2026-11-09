package com.deuna.maven.wallets

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.deuna.maven.shared.DeunaHttpClient
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.DeunaProgressDialog
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.wallets.google_pay.TokenizeWalletCard
import com.deuna.maven.wallets.google_pay.WalletPaymentActivity
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
    private val progress = DeunaProgressDialog(context)

    fun run() {
        val cached = GetWalletsAvailable.cachedWallets
            ?: return dispatchError("NOT_INITIALIZED", "Call getWalletsAvailable() before initElements().")

        if (requestedProvider !in cached) {
            return dispatchError("WALLET_UNAVAILABLE", "$requestedProvider is not available on this device.")
        }

        val handler = WalletHandlerRegistry.get(requestedProvider)
            ?: return dispatchError("UNSUPPORTED_WALLET", "$requestedProvider has no registered handler.")

        workers.execute {
            progress.show()
            try {
                val fetchResult = fetchCredentials()

                val userToken = fetchResult.userToken
                val userId = fetchResult.userId
                if (userToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
                    progress.dismiss()
                    dispatchError("MISSING_USER_AUTH", "userToken or userId is missing — cannot tokenize wallet payment.")
                    return@execute
                }

                val credentials = fetchResult.credentials[requestedProvider]
                    ?: run {
                        progress.dismiss()
                        return@execute dispatchError("NO_CREDENTIALS", "No credentials found for $requestedProvider.")
                    }

                progress.dismiss()
                handler.launch(context, environment, credentials) { result ->
                    when (result) {
                        is WalletLaunchResult.Success -> {
                            progress.show()
                            tokenize(userToken, userId)
                        }
                        is WalletLaunchResult.Error -> dispatchError(result.code, result.message)
                        is WalletLaunchResult.Closed -> mainHandler.post {
                            callbacks.onClosed?.invoke(CloseAction.userAction)
                        }
                    }
                }
            } catch (e: Exception) {
                progress.dismiss()
                DeunaLogs.error("[wallets] WalletElements failed: ${e.message}")
                dispatchError("WALLET_ELEMENTS_FAILED", e.message ?: "Unknown error")
            }
        }
    }

    private fun tokenize(userToken: String, userId: String) {
        val paymentDataJson = WalletPaymentActivity.lastPaymentDataJson
        if (paymentDataJson.isNullOrEmpty()) {
            progress.dismiss()
            dispatchError("MISSING_PAYMENT_DATA", "Raw wallet data missing paymentData field.")
            return
        }

        workers.execute {
            try {
                val apiResponse = TokenizeWalletCard.tokenize(
                    environment = environment,
                    publicApiKey = publicApiKey,
                    userId = userId,
                    userToken = userToken,
                    paymentDataJson = paymentDataJson,
                )
                progress.dismiss()
                val hasError = apiResponse.has("error") && !apiResponse.isNull("error")
                if (hasError) {
                    val err = apiResponse.optJSONObject("error")
                    dispatchError(
                        err?.optString("code") ?: "TOKENIZATION_ERROR",
                        err?.optString("message") ?: "Card tokenization returned an error.",
                    )
                } else {
                    mainHandler.post { callbacks.onSuccess?.invoke(jsonToMap(apiResponse)) }
                }
            } catch (e: Exception) {
                progress.dismiss()
                DeunaLogs.error("[wallets] Tokenization failed: ${e.message}")
                dispatchError("TOKENIZATION_REQUEST_FAILED", e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchCredentials(): VaultResponseParser.FetchResult {
        if (orderToken == null) {
            return VaultResponseParser.FetchResult(
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

    @Suppress("UNCHECKED_CAST")
    private fun jsonToMap(json: org.json.JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.get(key)
        }
        return map
    }
}
