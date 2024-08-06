package com.example.demoandroid

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.DeunaSDK
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.closeCheckout
import com.deuna.maven.closeElements
import com.deuna.maven.closePaymentWidget
import com.deuna.maven.initCheckout
import com.deuna.maven.initElements
import com.deuna.maven.initPaymentWidget
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.setCustomStyle
import com.deuna.maven.shared.*
import com.deuna.maven.shared.domain.UserInfo
import org.json.JSONObject


const val ERROR_TAG = "❌ DeunaSDK"
const val DEBUG_TAG = "👀 DeunaSDK"

@Suppress("UNCHECKED_CAST")
class MainActivity : AppCompatActivity() {
    private val deunaSdk = DeunaSDK(
        environment = Environment.SANDBOX,
        publicApiKey = "YOUR_PUBLIC_API_KEY",
    )

    private val orderToken: String
        get() = findViewById<EditText>(R.id.inputOrderToken).text.toString().trim()

    private val userToken: String?
        get() {
            val text = findViewById<EditText>(R.id.inputUserToken).text.toString().trim()
            return text.ifEmpty { null }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val payButton: Button = findViewById(R.id.payButton)
        val paymentWidgetButton: Button = findViewById(R.id.paymentWidgetButton)
        val savePaymentMethodButton: Button = findViewById(R.id.savePaymentMethodButton)

        payButton.setOnClickListener { startPaymentProcess() }
        paymentWidgetButton.setOnClickListener { showPaymentWidget() }
        savePaymentMethodButton.setOnClickListener { saveCard() }
    }

    private fun handlePaymentSuccess(data: Json) {
        Intent(this@MainActivity, PaymentSuccessfulActivity::class.java).apply {
            putExtra(
                PaymentSuccessfulActivity.EXTRA_JSON_ORDER,
                JSONObject(data["order"] as Json).toString()
            )
            startActivity(this)
        }
    }

    private fun showPaymentErrorAlertDialog(metadata: PaymentsError.Metadata) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(metadata.code)
        builder.setMessage(metadata.message)

        builder.setPositiveButton(
            "Aceptar",
            DialogInterface.OnClickListener { dialog, which -> // Code to execute when OK button is clicked
                dialog.dismiss()
            },
        )

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun showPaymentWidget() {
        deunaSdk.initPaymentWidget(
            context = this,
            orderToken = orderToken,
            cssFile = "YOUR_THEME_UUID", // optional
            callbacks = PaymentWidgetCallbacks().apply {
                onSuccess = { data ->
                    deunaSdk.closePaymentWidget()
                    handlePaymentSuccess(data)
                }
                onCanceled = {
                    Log.d(DEBUG_TAG, "Payment was canceled by user")
                }
                onCardBinDetected = { cardBinMetadata, refetchOrder ->
                    Log.d(DEBUG_TAG, "cardBinMetadata: $cardBinMetadata")
                    if (cardBinMetadata != null) {

                        deunaSdk.setCustomStyle(
                            data = JSONObject(
                                """
                        {
                          "theme": {
                            "colors": {
                              "primaryTextColor": "#023047",
                              "backgroundSecondary": "#8ECAE6",
                              "backgroundPrimary": "#F2F2F2",
                              "buttonPrimaryFill": "#FFB703",
                              "buttonPrimaryHover": "#FFB703",
                              "buttonPrimaryText": "#000000",
                              "buttonPrimaryActive": "#FFB703"
                            }
                          },
                          "HeaderPattern": {
                            "overrides": {
                              "Logo": {
                                "props": {
                                  "url": "https://images-staging.getduna.com/ema/fc78ef09-ffc7-4d04-aec3-4c2a2023b336/test2.png"
                                }
                              }
                            }
                          }
                        }
                        """
                            ).toMap()
                        )

                        refetchOrder { order ->
                            Log.d(DEBUG_TAG, "onCardBinDetected > refetchOrder: $order")
                        }

                    }
                }
                onInstallmentSelected = { metadata, refetchOrder ->
                    Log.d(DEBUG_TAG, "installmentMetadata: $metadata")
                    refetchOrder { order ->
                        Log.d(DEBUG_TAG, "onInstallmentSelected > refetchOrder: $order")
                    }
                }
                onClosed = {
                    Log.d(DEBUG_TAG, "Widget was closed")
                }
                onError = { error ->
                    Log.e(DEBUG_TAG, "Error type: ${error.type}, metadata: ${error.metadata}")
                    when (error.type) {
                        PaymentsError.Type.INITIALIZATION_FAILED,
                        PaymentsError.Type.NO_INTERNET_CONNECTION -> {
                            deunaSdk.closeCheckout()
                            if (error.metadata != null) {
                                showPaymentErrorAlertDialog(error.metadata!!)
                            }
                        }

                        else -> {}
                    }
                }
            },
            userToken = userToken,
        )
    }


    private fun startPaymentProcess() {
        deunaSdk.initCheckout(
            context = this,
            orderToken = orderToken,
            callbacks = CheckoutCallbacks().apply {
                onSuccess = { data ->
                    Log.d(DEBUG_TAG, "Payment success $data")
                    deunaSdk.closeCheckout()
                    handlePaymentSuccess(data)
                }
                onError = { error ->
                    Log.e(DEBUG_TAG, "Error type: ${error.type}, metadata: ${error.metadata}")
                    when (error.type) {
                        PaymentsError.Type.PAYMENT_ERROR,
                        PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED,
                        PaymentsError.Type.INITIALIZATION_FAILED -> {
                            deunaSdk.closeCheckout()
                            if (error.metadata != null) {
                                showPaymentErrorAlertDialog(error.metadata!!)
                            }
                        }

                        else -> {}
                    }
                }
                onCanceled = {
                    Log.d(DEBUG_TAG, "Payment was canceled by user")
                }
                eventListener = { type, _ ->
                    Log.d("✅ ON EVENT", type.name)
                    when (type) {
                        CheckoutEvent.changeAddress, CheckoutEvent.changeCart -> {
                            deunaSdk.closeCheckout()
                        }

                        else -> {}
                    }
                }
                onClosed = {
                    Log.d(DEBUG_TAG, "Widget was closed")
                }
            },
            userToken = userToken,
        )
    }


    private fun saveCard() {
        deunaSdk.initElements(
            context = this,
            userToken = userToken,
            userInfo = if (userToken == null) UserInfo(
                firstName = "Darwin",
                lastName = "Morocho",
                email = "domorocho+1@deuna.com",
            ) else null,
            callbacks = ElementsCallbacks().apply {
                onSuccess = { data ->
                    val metadata = (data["metadata"] as Json)["createdCard"] as Json
                    deunaSdk.closeElements()
                    Intent(this@MainActivity, SaveCardSuccessfulActivity::class.java).apply {
                        putExtra(
                            SaveCardSuccessfulActivity.EXTRA_CREATED_CARD,
                            JSONObject(metadata).toString()
                        )
                        startActivity(this)
                    }
                }
                eventListener = { type, _ ->
                    Log.d(DEBUG_TAG, "eventListener ${type.name}")
                }
                onError = {
                    Log.e(ERROR_TAG, it.type.message)
                    deunaSdk.closeElements()
                }
                onCanceled = {
                    Log.d(DEBUG_TAG, "Saving card was canceled by user")
                }
                onClosed = {
                    Log.d(DEBUG_TAG, "Widget was closed")
                }
            },
        )
    }
}