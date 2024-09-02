package com.example.demoandroid.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.demoandroid.MainActivity
import com.example.demoandroid.R

class ClickToPaySuccessfulActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thank_you)
        findViewById<TextView>(R.id.save_card_message).text = """
            Pago con Click To Pay
            Exitoso
        """.trimIndent()
    }

    fun backToMainActivity(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current actvity
    }
}