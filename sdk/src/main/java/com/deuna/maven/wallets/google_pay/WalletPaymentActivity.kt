package com.deuna.maven.wallets.google_pay

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.widgets.elements_widget.ElementsError
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONObject
import java.util.concurrent.Executors

class WalletPaymentActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PAYMENT_REQUEST_JSON = "PAYMENT_REQUEST_JSON"
        const val EXTRA_USER_TOKEN = "USER_TOKEN"
        const val EXTRA_USER_ID = "USER_ID"
        const val EXTRA_ENVIRONMENT = "ENVIRONMENT"
        const val EXTRA_PUBLIC_API_KEY = "PUBLIC_API_KEY"

        @Volatile
        internal var pendingCallbacks: ElementsCallbacks? = null
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val workers = Executors.newSingleThreadExecutor()

    private val googlePayLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val callbacks = pendingCallbacks
        pendingCallbacks = null

        when (result.resultCode) {
            RESULT_OK -> {
                val paymentData = result.data?.let { PaymentData.getFromIntent(it) }
                if (paymentData == null) {
                    DeunaLogs.error("[wallets] PaymentData is null on RESULT_OK")
                    dispatchError(callbacks, "PAYMENT_DATA_NULL", "Payment data not received.")
                    mainHandler.post { finish() }
                    return@registerForActivityResult
                }
                handlePaymentSuccess(callbacks, paymentData)
            }

            RESULT_CANCELED -> {
                mainHandler.post {
                    callbacks?.onClosed?.invoke(CloseAction.userAction)
                    finish()
                }
            }

            else -> {
                dispatchError(
                    callbacks,
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
                val callbacks = pendingCallbacks
                pendingCallbacks = null
                handlePaymentSuccess(callbacks, task.result)
            } else {
                val exception = task.exception
                if (exception is ResolvableApiException) {
                    googlePayLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution).build()
                    )
                } else {
                    val callbacks = pendingCallbacks
                    pendingCallbacks = null
                    DeunaLogs.error("[wallets] loadPaymentData failed: ${exception?.message}")
                    dispatchError(
                        callbacks,
                        "GOOGLE_PAY_ERROR",
                        exception?.message ?: "Unknown error"
                    )
                    mainHandler.post { finish() }
                }
            }
        }
    }

    private fun handlePaymentSuccess(callbacks: ElementsCallbacks?, paymentData: PaymentData) {
        val paymentDataJson = paymentData.toJson()
        val userToken = intent.getStringExtra(EXTRA_USER_TOKEN)
        val userId = intent.getStringExtra(EXTRA_USER_ID)
        val publicApiKey = intent.getStringExtra(EXTRA_PUBLIC_API_KEY) ?: ""
        val envName = intent.getStringExtra(EXTRA_ENVIRONMENT) ?: Environment.PRODUCTION.name
        val environment = Environment.valueOf(envName)

        if (!userToken.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            workers.execute {
                try {
                    val apiResponse = TokenizeWalletCard.tokenize(
                        environment = environment,
                        publicApiKey = publicApiKey,
                        userId = userId,
                        userToken = userToken,
                        paymentDataJson = paymentDataJson,
                    )
                    val hasError = apiResponse.has("error") && !apiResponse.isNull("error")
                    if (hasError) {
                        val err = apiResponse.optJSONObject("error")
                        dispatchError(
                            callbacks,
                            err?.optString("code") ?: "TOKENIZATION_ERROR",
                            err?.optString("message") ?: "Card tokenization returned an error.",
                        )
                    } else {
                        mainHandler.post { callbacks?.onSuccess?.invoke(jsonToMap(apiResponse)) }
                    }
                } catch (e: Exception) {
                    DeunaLogs.error("[wallets] Tokenization failed: ${e.message}")
                    dispatchError(
                        callbacks,
                        "TOKENIZATION_REQUEST_FAILED",
                        e.message ?: "Unknown error"
                    )
                } finally {
                    mainHandler.post { finish() }
                }
            }
        } else {
            dispatchError(callbacks, "MISSING_USER_AUTH", "userToken or userId is missing — cannot tokenize wallet payment.")
            mainHandler.post { finish() }
        }
    }

    private fun dispatchError(callbacks: ElementsCallbacks?, code: String, message: String) {
        val error = ElementsError(
            type = ElementsError.Type.UNKNOWN_ERROR,
            metadata = ElementsError.Metadata(code = code, message = message),
        )
        mainHandler.post { callbacks?.onError?.invoke(error) }
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
