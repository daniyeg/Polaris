package com.beyond5g.polaris

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

fun sendUserData() {

    val client = OkHttpClient()

    val json = JSONObject()
    json.put("name", "android")

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://polaris-server-30ha.onrender.com/api/add") // Replace with your actual Django API URL
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("API", "Request failed: $e")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                Log.d("API", "Response: ${response.body?.string()}")
            } else {
                Log.e("API", "Server error: ${response.code}")
            }
        }
    })
}
