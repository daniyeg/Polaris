package com.beyond5g.polaris

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class SignupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val confirmButton = findViewById<Button>(R.id.confirm)
        confirmButton.setOnClickListener {
            val phoneNumber = findViewById<EditText>(R.id.phoneNumber).text.toString().trim()
            val username = findViewById<EditText>(R.id.username).text.toString().trim()
            val password = findViewById<EditText>(R.id.password).text.toString().trim()

            if (phoneNumber.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Connector.sendSignUp(username, password, phoneNumber) { success ->
                runOnUiThread {
                    if (success) {
                        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
                        sharedPref.edit()
                            .putString("pending_phone_number", phoneNumber)
                            .putBoolean("is_logged_in", true) // optional, only if you want to allow navigation
                            .putBoolean("is_otp_verified", false)
                            .putLong("login_time", System.currentTimeMillis())
                            .apply()

                        Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, OtpActivity::class.java)
                        intent.putExtra("phone_number", phoneNumber)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Signup failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
