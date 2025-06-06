package com.beyond5g.polaris

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val confirmButton = findViewById<Button>(R.id.confirm)
        confirmButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.username).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()

            val validated = true
            if (validated){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

        }


    }
}
