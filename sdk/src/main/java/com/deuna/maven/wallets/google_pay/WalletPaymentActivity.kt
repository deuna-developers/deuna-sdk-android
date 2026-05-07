package com.deuna.maven.wallets.google_pay

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Environment
import com.deuna.maven.wallets.WalletLaunchResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONObject

class WalletPaymentActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PAYMENT_REQUEST_JSON = "PAYMENT_REQUEST_JSON"
        const val EXTRA_ENVIRONMENT = "ENVIRONMENT"

        @Volatile internal var pendingOnResult: ((WalletLaunchResult) -> Unit)? = null
        @Volatile internal var lastPaymentDataJson: String? = null
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private val googlePayLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val onResult = pendingOnResult
        pendingOnResult = null

        when (result.resultCode) {
            RESULT_OK -> {
                val paymentData = result.data?.let { PaymentData.getFromIntent(it) }
                if (paymentData == null) {
                    DeunaLogs.error("[wallets] PaymentData is null on RESULT_OK")
                    dispatchError(onResult, "PAYMENT_DATA_NULL", "Payment data not received.")
                    mainHandler.post { finish() }
                    return@registerForActivityResult
                }
                handlePaymentSuccess(onResult, paymentData)
            }

            RESULT_CANCELED -> {
                mainHandler.post {
                    onResult?.invoke(WalletLaunchResult.Closed)
                    finish()
                }
            }

            else -> {
                dispatchError(
                    onResult,
                    "GOOGLE_PAY_ERROR",
                    "Unexpected result code: ${result.resultCode}"
                )
                mainHandler.post { finish() }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestJson = intent.getStringExtra(EXTRA_PAYMENT_REQUEST_JSON)
        if (requestJson == null) {
            DeunaLogs.error("[wallets] WalletPaymentActivity: missing payment request")
            finish()
            return
        }

        val envName = intent.getStringExtra(EXTRA_ENVIRONMENT) ?: Environment.PRODUCTION.name
        val environment = Environment.valueOf(envName)

        val googlePayEnv = if (environment == Environment.PRODUCTION)
            WalletConstants.ENVIRONMENT_PRODUCTION
        else
            WalletConstants.ENVIRONMENT_TEST

        val paymentsClient = Wallet.getPaymentsClient(
            this,
            Wallet.WalletOptions.Builder().setEnvironment(googlePayEnv).build()
        )

        val request = PaymentDataRequest.fromJson(requestJson)
        paymentsClient.loadPaymentData(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val onResult = pendingOnResult
                pendingOnResult = null
                handlePaymentSuccess(onResult, task.result)
            } else {
                val exception = task.exception
                if (exception is ResolvableApiException) {
                    googlePayLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution).build()
                    )
                } else {
                    val onResult = pendingOnResult
                    pendingOnResult = null
                    DeunaLogs.error("[wallets] loadPaymentData failed: ${exception?.message}")
                    dispatchError(
                        onResult,
                        "GOOGLE_PAY_ERROR",
                        exception?.message ?: "Unknown error"
                    )
                    mainHandler.post { finish() }
                }
            }
        }
    }

    private fun handlePaymentSuccess(onResult: ((WalletLaunchResult) -> Unit)?, paymentData: PaymentData) {
        val paymentDataJson = paymentData.toJson()
        lastPaymentDataJson = paymentDataJson
        val paymentMap = try { jsonToMap(JSONObject(paymentDataJson)) } catch (_: Exception) { emptyMap() }
        mainHandler.post {
            onResult?.invoke(WalletLaunchResult.Success(mapOf("paymentData" to paymentMap)))
            finish()
        }
    }

    private fun dispatchError(onResult: ((WalletLaunchResult) -> Unit)?, code: String, message: String) {
        mainHandler.post { onResult?.invoke(WalletLaunchResult.Error(code, message)) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun jsonToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.get(key)
        }
        return map
    }
}
