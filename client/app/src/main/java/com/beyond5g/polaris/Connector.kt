package com.beyond5g.polaris

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class Connector {
    companion object {

        // Overload: No callbacks (for fire-and-forget requests)
        fun sendJsonToApi(apiUrl: String, json: JSONObject) {
            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("API", "Request failed: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful) {
                        Log.d("API", "Response: $responseBody")
                    } else {
                        Log.e("API", "Server error (${response.code}): $responseBody")
                    }
                }
            })
        }

        // Overload: With callbacks
        fun sendJsonToApi(
            apiUrl: String,
            json: JSONObject,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit
        ) {
            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError("Network error: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        onSuccess(responseBody)
                    } else {
                        onError("Server error: $responseBody")
                    }
                }
            })
        }

        fun sendCellInfo(
            phoneNumber: String,
            lat: Double,
            lng: Double,
            timestamp: String,
            gen: String,
            tech: String,
            plmn: String,
            cid: Int,
            lac: Int? = null,
            rac: Int? = null,
            tac: Int? = null,
            freqBand: Double? = null,
            afrn: Double? = null,
            freq: Double? = null,
            rsrp: Double? = null,
            rsrq: Double? = null,
            rscp: Double? = null,
            ecno: Double? = null,
            rxlev: Double? = null
        ) {
            val json = JSONObject()
            json.put("phone_number", phoneNumber)
            json.put("lat", lat)
            json.put("lng", lng)
            json.put("timestamp", timestamp)
            json.put("gen", gen)
            json.put("tech", tech)
            json.put("plmn", plmn)
            json.put("cid", cid)

            lac?.let { json.put("lac", it) }
            rac?.let { json.put("rac", it) }
            tac?.let { json.put("tac", it) }
            freqBand?.let { json.put("freq_band", it) }
            afrn?.let { json.put("afrn", it) }
            freq?.let { json.put("freq", it) }
            rsrp?.let { json.put("rsrp", it) }
            rsrq?.let { json.put("rsrq", it) }
            rscp?.let { json.put("rscp", it) }
            ecno?.let { json.put("ecno", it) }
            rxlev?.let { json.put("rxlev", it) }

            sendJsonToApi("https://polaris-server-30ha.onrender.com/api/add_cell_info/", json)
        }

        fun sendTest(
            type_: String,
            phoneNumber: String,
            timestamp: String,
            cellInfo: Int,
            prop: String,
            propVal: String
        ) {
            val detailJson = JSONObject()
            detailJson.put(prop, propVal)

            val json = JSONObject()
            json.put("type_", type_)
            json.put("phone_number", phoneNumber)
            json.put("timestamp", timestamp)
            json.put("cell_info", cellInfo)
            json.put("detail", detailJson)

            sendJsonToApi("https://polaris-server-30ha.onrender.com/api/add_test/", json)
        }

        fun sendLogin(
            identifier: String,
            password: String,
            onResult: (Boolean, String?) -> Unit
        ) {
            val json = JSONObject()
            json.put("identifier", identifier)
            json.put("password", password)

            sendJsonToApi(
                "https://polaris-server-30ha.onrender.com/api/login/",
                json,
                onSuccess = { response ->

                    try {
                        val jsonResponse = JSONObject(response)
                        val token = jsonResponse.getString("token")
                        onResult(true, token)
                        Log.d("LOGIN_DEBUG", "Right,  Sending JSON: $json")


                    } catch (e: Exception) {
                        onResult(false, null)
                    }
                },
                onError = {
                    onResult(false, null)
                    Log.d("LOGIN_DEBUG", "Error,  Sending JSON: $json")

                }
            )
        }

        fun sendLogout(onResult: (Boolean) -> Unit) {
            val emptyJson = JSONObject()
            sendJsonToApi(
                "https://polaris-server-30ha.onrender.com/api/logout/",
                emptyJson,
                onSuccess = { onResult(true) },
                onError = { onResult(false) }
            )
        }

        fun sendRequestOtp(phoneNumber: String, onResult: (Boolean, String?) -> Unit) {
            val json = JSONObject()
            json.put("phone_number", phoneNumber)

            sendJsonToApi(
                "https://polaris-server-30ha.onrender.com/api/request_otp/",
                json,
                onSuccess = { response ->
                    onResult(true, response)
                },
                onError = { error ->
                    onResult(false, error)
                }
            )
        }


        fun sendSignUp(
            username: String,
            password: String,
            phoneNumber: String,
            onResult: (Boolean) -> Unit
        ) {
            val json = JSONObject()
            json.put("username", username)
            json.put("password", password)
            json.put("phone_number", phoneNumber)

            sendJsonToApi(
                "https://polaris-server-30ha.onrender.com/api/signup/",
                json,
                onSuccess = { onResult(true) },
                onError = { onResult(false) }
            )
        }

        fun sendVerifyOtp(
            phoneNumber: String,
            otpCode: String,
            onResult: (Boolean) -> Unit
        ) {
            val json = JSONObject()
            json.put("phone_number", phoneNumber)
            json.put("otp_code", otpCode)

            sendJsonToApi(
                "https://polaris-server-30ha.onrender.com/api/verify_otp/",
                json,
                onSuccess = { onResult(true) },
                onError = { onResult(false) }
            )
        }
    }
}
