package com.example.demoandroid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.shared.toMap
import org.json.JSONObject

fun getFormattedMapString(map: Map<String, Any>): String {
    return map.entries.joinToString(separator = "\n") { (key, value) ->
        "$key: $value"
    }
}

class SaveCardSuccessfulActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CREATED_CARD = "EXTRA_CREATED_CARD"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thank_you)

        val card = JSONObject(intent.getStringExtra(EXTRA_CREATED_CARD)!!).toMap()
        findViewById<TextView>(R.id.save_card_message).text = """
            TARJETA GUARDADA
       
            ID: ${card["id"]}
            Primeros 6 dígitos: ${card["firstSix"]}
            Últimos 4 dígitos: ${card["lastFour"]}
            Nombre en la tarjeta: ${card["cardHolder"]}
            Fecha de expiración: ${card["expirationDate"]}
        """.trimIndent()
    }

    fun backToMainActivity(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current actvity
    }
}