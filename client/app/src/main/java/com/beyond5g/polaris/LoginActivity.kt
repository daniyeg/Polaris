package com.beyond5g.polaris

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val confirmButton = findViewById<Button>(R.id.confirm)
        confirmButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.username).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()

            responseBody = Connector.sendLogin(username, password)

            val validated = false

            if (validated) {
                val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
                sharedPref.edit().putBoolean("is_logged_in", true).apply()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}