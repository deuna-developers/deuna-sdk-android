package com.deuna.maven.wallets

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.DeunaHttpClient
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.domain.UserInfo
import java.net.URLEncoder
import java.util.concurrent.Executors

data class GetWalletsAvailableParams(
    val orderToken: String? = null,
    val userInfo: UserInfo? = null,
)

fun DeunaSDK.getWalletsAvailable(
    context: Context,
    params: GetWalletsAvailableParams? = null,
    callback: (wallets: List<WalletProvider>, error: WalletsError?) -> Unit,
) {
    GetWalletsAvailable(
        context = context.applicationContext,
        environment = environment,
        publicApiKey = publicApiKey,
        params = params,
        callback = callback,
    ).run()
}

class GetWalletsAvailable(
    private val context: Context,
    private val environment: Environment,
    private val publicApiKey: String,
    private val params: GetWalletsAvailableParams?,
    private val callback: (wallets: List<WalletProvider>, error: WalletsError?) -> Unit,
) {
    companion object {
        @Volatile internal var cachedWallets: List<WalletProvider>? = null
        @Volatile internal var cachedCredentials: Map<WalletProvider, WalletCredentials> = emptyMap()
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val workers = Executors.newSingleThreadExecutor()

    fun run() {
        cachedWallets?.let {
            DeunaLogs.info("[wallets] Returning cached wallets")
            callbackOnMain(it, null)
            return
        }
        workers.execute {
            try {
                val parsed = fetchAndParse()
                val available = parsed.providers.filter { isAvailableOnDevice(it) }
                cachedWallets = available
                cachedCredentials = parsed.credentials
                callbackOnMain(available, null)
            } catch (e: Exception) {
                DeunaLogs.error("[wallets] getWalletsAvailable failed: ${e.message}")
                callbackOnMain(emptyList(), WalletsError.fetchFailed(e.message ?: "Unknown error"))
            }
        }
    }

    private fun fetchAndParse(): VaultResponseParser.ProvidersResult {
        val urlBuilder = StringBuilder("${environment.elementsBaseUrl}/api/vault")
        params?.orderToken?.let { urlBuilder.append("?orderToken=${URLEncoder.encode(it, "UTF-8")}") }

        val response = DeunaHttpClient.post(
            url = urlBuilder.toString(),
            headers = mapOf("x-api-key" to publicApiKey),
            body = VaultResponseParser.buildUserInfoBody(params?.userInfo),
        )
        return VaultResponseParser.parseProviders(response)
    }

    private fun isAvailableOnDevice(provider: WalletProvider): Boolean =
        WalletHandlerRegistry.get(provider)?.isAvailableOnDevice(context, environment) ?: false

    private fun callbackOnMain(wallets: List<WalletProvider>, error: WalletsError?) {
        DeunaLogs.info("[wallets] Available wallets: ${wallets.size}")
        if (Looper.myLooper() == Looper.getMainLooper()) callback(wallets, error)
        else mainHandler.post { callback(wallets, error) }
    }
}
