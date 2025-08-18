package com.beyond5g.polaris

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        val loginTime = sharedPref.getLong("login_time", 0L)

        val sessionDurationLimit = 30 * 60 * 1000 // 30 minutes in milliseconds

        val currentTime = System.currentTimeMillis()

        var isSessionValid = isLoggedIn && (currentTime - loginTime < sessionDurationLimit)

        isSessionValid = true

        val intent = if (isSessionValid) {
            Intent(this, HomeActivity::class.java)
        } else {
            sharedPref.edit().clear().apply() // session expired, clear
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}

