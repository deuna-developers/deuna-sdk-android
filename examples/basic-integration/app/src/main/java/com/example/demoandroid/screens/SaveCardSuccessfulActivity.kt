package com.example.demoandroid.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toMap
import com.example.demoandroid.MainActivity
import com.example.demoandroid.R
import org.json.JSONObject


class SaveCardSuccessfulActivity : AppCompatActivity() {

    companion object {
        const val ARGUMENTS_DATA = "ARGUMENTS_DATA"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thank_you)

        val arguments = JSONObject(intent.getStringExtra(ARGUMENTS_DATA)!!).toMap()
        val savedCardData = arguments["savedCardData"] as Json

        findViewById<TextView>(R.id.save_card_message).text = """
           ${arguments["title"]}
       
            ID: ${savedCardData["id"]}
            Primeros 6 dígitos: ${savedCardData["firstSix"] ?: savedCardData["first_six"]}
            Últimos 4 dígitos: ${savedCardData["lastFour"] ?: savedCardData["last_four"]}
            Nombre en la tarjeta: ${savedCardData["cardHolder"] ?: savedCardData["card_holder"]}
            Fecha de expiración: ${savedCardData["expirationDate"] ?: savedCardData["expiration_date"]}
        """.trimIndent()
    }

    fun backToMainActivity(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current actvity
    }
}