package com.beyond5g.polaris

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val confirmButton = findViewById<Button>(R.id.confirm)
        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val signupText = findViewById<TextView>(R.id.signupText)

        signupText.setOnClickListener(){
            startActivity(Intent(this, SignupActivity::class.java))

        }
        confirmButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Connector.sendLogin(username, password) { success, token ->
                runOnUiThread {

                    if (success) {

                        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
                        val currentTime = System.currentTimeMillis()
                        sharedPref.edit()
                            .putBoolean("is_logged_in", true)
                            .putLong("login_time", currentTime)
                            .putString("token", token)
                            .apply()

                        val prefs = getSharedPreferences("polaris", MODE_PRIVATE)
                        prefs.edit().putString("username", username).apply()

                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
