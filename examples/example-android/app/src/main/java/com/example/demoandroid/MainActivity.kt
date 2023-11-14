package com.example.demoandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
//import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.DeUnaSdk
import com.deuna.maven.checkout.Callbacks
import com.deuna.maven.checkout.CheckoutEvents
import com.deuna.maven.checkout.domain.ElementType
import com.deuna.maven.checkout.domain.Environment
import com.deuna.maven.element.domain.ElementCallbacks
import com.bumptech.glide.Glide

// PASO 1: Importar librería de DEUNA

class MainActivity : AppCompatActivity() {
    private var orderToken = ""
    private var userToken = ""
    private var apiKey = ""
    private var environment: Environment = Environment.STAGING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configuración inicial de la interfaz
        setupUI()

        // Configurar listeners para botones
        setupListeners()
    }
    private fun setupUI() {
        val imageView: ImageView = findViewById(R.id.imageView)
        Glide.with(this)
            .load("https://camo.githubusercontent.com/50fa432906fc45b20062b150933db8f0bb86682ec6540624a19a72c59eb20d81/68747470733a2f2f642d756e612d6f6e652e73332e75732d656173742d322e616d617a6f6e6177732e636f6d2f67657374696f6e61646f5f706f725f642d756e612e706e67")
            .into(imageView)
    }

    private fun setupListeners() {
        val payButton: Button = findViewById(R.id.payButton)
        val savePaymentMethodButton: Button = findViewById(R.id.savePaymentMethodButton)
        val applyConfigButton: Button = findViewById(R.id.applyConfigButton)
        val createOrderButton: Button = findViewById(R.id.createOrderButton)

        applyConfigButton.setOnClickListener { applyConfig() }
        payButton.setOnClickListener { initCheckout() }
        savePaymentMethodButton.setOnClickListener { initElements() }
        createOrderButton.setOnClickListener { createOrder() }
    }

    private fun createOrder() {
        Log.d("apiKey", apiKey)
        Log.d("environment", environment.toString())
        val deunaService = DeunaApiService(apiKey, environment)
        val merchant = Merchant()
        val orderTokenTextView: TextView = findViewById(R.id.orderToken)
        val userTokenTextView: TextView = findViewById(R.id.userToken)

        Log.d("debug", "createOrder")
        deunaService.fetchOrderToken(merchant) { newOrderToken, newUserToken ->
            if (newOrderToken != null && newUserToken != null) {
                Log.d("Success", newOrderToken)
                orderToken = newOrderToken
                userToken = newUserToken
                orderTokenTextView.text = orderToken
                userTokenTextView.text = userToken

                Log.d("debug", "Success message")
            } else {
                Log.d("debug", "Error deunaService")
            }
        }
    }

    private fun applyConfig() {
        val inputApiKeyEditText: EditText = findViewById(R.id.inputApiKey)
        val environmentSpinner: Spinner = findViewById(R.id.environmentOption)

        apiKey = inputApiKeyEditText.text.toString()

        environment = when (environmentSpinner.selectedItemPosition) {
            0 -> Environment.STAGING
            1 -> Environment.PRODUCTION
            else -> Environment.DEVELOPMENT
        }

        if (apiKey.isNotEmpty() && orderToken.isNotEmpty()) {
            configureForCheckout()
            configureForElements()

            Toast.makeText(
                this@MainActivity,
                "Configuración actualizada",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(this, "Please enter both API Key and Token", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureForCheckout() {
        DeUnaSdk.config(
            apiKey = apiKey,
            orderToken = orderToken,
            environment = environment,
            context = this@MainActivity,
            callbacks = createCheckoutCallbacks(),
            closeOnEvents = arrayOf(CheckoutEvents.linkFailed)
        )
    }

    private fun configureForElements() {
        DeUnaSdk.config(
            apiKey = apiKey,
            orderToken = orderToken,
            environment = environment,
            userToken = userToken,
            context = this@MainActivity,
            elementType = ElementType.VAULT,
            elementCallbacks = createElementCallbacks(),
            showCloseButton = true
        )
    }

    private fun createCheckoutCallbacks(): Callbacks {
        return Callbacks().apply {
            onSuccess = { orderSuccessResponse ->
                DeUnaSdk.close()
                Intent(this@MainActivity, ThankYouActivity::class.java).apply {
                    startActivity(this)
                }
            }
            onError = { orderErrorResponse, _ ->
                if (orderErrorResponse != null) {
                    Log.d("DeunaSdkOnError", orderErrorResponse.order.items_total_amount.toString())
                }
            }
            onClose = { _ ->
                Log.d("DeunaSdkOnClose", "onClose")
            }
            onChangeAddress = { act ->
                act.finish()
            }
            onCloseEvents = { _ ->
                // Código para manejar los eventos de cierre
                Log.d("DeunaSdkOnCloseEvents", "onCloseEvents")
            }
        }
    }

    private fun createElementCallbacks(): ElementCallbacks {
        return ElementCallbacks().apply {
            onSuccess = { orderSuccessResponse ->
                DeUnaSdk.close()
//                Intent(this@MainActivity, ThankYouActivity::class.java).apply {
//                    startActivity(this)
//                }
            }
            onError = { orderErrorResponse, _ ->
                if (orderErrorResponse != null) {
                    Log.d("DeunaSdkOnError", "DeunaSdkOnError")
                }
            }
            onClose = { _ ->
                Log.d("DeunaSdkOnClose", "onClose")
            }
            onChangeAddress = { act ->
                act.finish()
            }
            onCloseEvents = { _ ->
                // Código para manejar los eventos de cierre
                Log.d("DeunaSdkOnCloseEvents", "onCloseEvents")
            }
        }
    }

    private fun initCheckout() {
        configureForCheckout()
        DeUnaSdk.initCheckout()
    }

    private fun initElements() {
        configureForElements()
        DeUnaSdk.initElements()
    }
}