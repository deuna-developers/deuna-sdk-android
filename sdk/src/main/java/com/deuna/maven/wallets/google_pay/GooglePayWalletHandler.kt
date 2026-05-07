package com.deuna.maven.wallets.google_pay

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Environment
import com.deuna.maven.wallets.WalletCredentials
import com.deuna.maven.wallets.WalletHandler
import com.deuna.maven.wallets.WalletLaunchResult
import com.deuna.maven.wallets.WalletProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal object GooglePayWalletHandler : WalletHandler {

    override val provider = WalletProvider.GOOGLE_PAY

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun isAvailableOnDevice(context: Context, environment: Environment): Boolean {
        val gmsStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (gmsStatus != ConnectionResult.SUCCESS) {
            DeunaLogs.info("[wallets] Google Play Services unavailable (code=$gmsStatus). Google Pay excluded.")
            return false
        }

        val googlePayEnv = if (environment == Environment.PRODUCTION)
            WalletConstants.ENVIRONMENT_PRODUCTION else WalletConstants.ENVIRONMENT_TEST

        val paymentsClient = Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder().setEnvironment(googlePayEnv).build(),
        )

        val request = IsReadyToPayRequest.fromJson(
            JSONObject().apply {
                put("apiVersion", 2)
                put("apiVersionMinor", 0)
                put("allowedPaymentMethods", JSONArray().put(JSONObject().apply {
                    put("type", "CARD")
                    put("parameters", JSONObject().apply {
                        put("allowedAuthMethods", JSONArray(GooglePayCredentials.DEFAULT_AUTH_METHODS))
                        put("allowedCardNetworks", JSONArray(GooglePayCredentials.DEFAULT_CARD_NETWORKS))
                    })
                }))
            }.toString()
        )

        val latch = CountDownLatch(1)
        var isReady = false
        paymentsClient.isReadyToPay(request).addOnCompleteListener { task ->
            isReady = task.isSuccessful && task.result == true
            latch.countDown()
        }
        latch.await(5, TimeUnit.SECONDS)
        return isReady
    }

    override fun launch(
        context: Context,
        environment: Environment,
        credentials: WalletCredentials,
        onResult: (WalletLaunchResult) -> Unit,
    ) {
        val googlePayCredentials = credentials as? GooglePayCredentials
            ?: run {
                onResult(WalletLaunchResult.Error("NO_GOOGLE_PAY_CREDENTIALS", "No Google Pay configuration found."))
                return
            }

        WalletPaymentActivity.pendingOnResult = onResult
        context.startActivity(buildIntent(context, environment, googlePayCredentials))
    }

    private fun buildIntent(
        context: Context,
        environment: Environment,
        credentials: GooglePayCredentials,
    ) = Intent(context, WalletPaymentActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        putExtra(WalletPaymentActivity.EXTRA_PAYMENT_REQUEST_JSON, GooglePayRequestBuilder.build(credentials).toJson())
        putExtra(WalletPaymentActivity.EXTRA_ENVIRONMENT, environment.name)
    }
}
