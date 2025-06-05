package com.beyond5g.polaris

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val confirmButton = findViewById<Button>(R.id.confirm)
        confirmButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.email).text.toString()
            val username = findViewById<EditText>(R.id.username).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()
            val code = findViewById<EditText>(R.id.code).text.toString()
        }


        val sendcodeButton = findViewById<Button>(R.id.sendcode)
        sendcodeButton.setOnClickListener {

        }
    }
}
