package com.example.demoandroid

import android.content.Intent
import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity


class ThankYouActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thank_you)
    }

    fun backToMainActivity(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current actvity
    }
}