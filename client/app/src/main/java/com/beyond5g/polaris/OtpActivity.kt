package com.beyond5g.polaris

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

class OtpActivity : ComponentActivity() {

    private lateinit var codeEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp)

        val phoneNumber = intent.getStringExtra("phone_number") ?: ""
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)

        codeEditText = findViewById(R.id.code)
        val sendCodeButton = findViewById<Button>(R.id.sendcode)
        val confirmButton = findViewById<Button>(R.id.confirm)

        sendCodeButton.setOnClickListener {
            Connector.sendRequestOtp(phoneNumber) { success, response ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "OTP sent", Toast.LENGTH_SHORT).show()
                        Log.d("OTP_RESPONSE", "Success: $response")
                    } else {
                        Toast.makeText(this, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                        Log.e("OTP_RESPONSE", "Error: $response")
                    }
                }
            }
        }

        confirmButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()

            Connector.sendVerifyOtp(phoneNumber, code) { success ->
                runOnUiThread {
                    if (success) {
                        sharedPref.edit().putBoolean("is_otp_verified", true).apply()
                        Toast.makeText(this, "OTP verified!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
