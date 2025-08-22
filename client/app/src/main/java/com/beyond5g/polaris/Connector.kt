package com.beyond5g.polaris

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

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

fun sendItem(name: String) {
    val json = JSONObject()
    json.put("name", name)

    sendJsonToApi("https://polaris-server-30ha.onrender.com/api/add/", json)
}

fun sendTest(
    type_: String,
    phoneNumber: String,
    timestamp: String,
    cellInfo: Int,
    detail: Map<String, String>
) {
    val detailJson = JSONObject()
    for ((key, value) in detail) {
        detailJson.put(key, value)
    }

    val json = JSONObject()
    json.put("type_", type_)
    json.put("phone_number", phoneNumber)
    json.put("timestamp", timestamp)
    json.put("cell_info", cellInfo)
    json.put("detail", detailJson)

    sendJsonToApi("https://polaris-server-30ha.onrender.com/api/add_test/", json)
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

fun sendLogin(identifier: String, password: String) {
    val json = JSONObject()
    json.put("identifier", identifier)
    json.put("password", password)

    sendJsonToApi("https://polaris-server-30ha.onrender.com/api/login/", json)
}

fun sendLogout() {
    val emptyJson = JSONObject()
    sendJsonToApi("https://polaris-server-30ha.onrender.com/api/logout/", emptyJson)
}

fun sendRequestOtp(phoneNumber: String) {
    val json = JSONObject()
    json.put("phone_number", phoneNumber)

    sendJsonToApi("https://polaris-server-30ha.onrender.com/api/request_otp/", json)
}

fun sendSignUp(username: String, password: String, phoneNumber: String) {
    val json = JSONObject()
    json.put("username", username)
    json.put("password", password)
    json.put("phone_number", phoneNumber)

    sendJsonToApi("https://polaris-server-30ha.onrender.com/api/signup/", json)
}

fun sendVerifyOtp(phoneNumber: String, otpCode: String) {
    val json = JSONObject()
    json.put("phone_number", phoneNumber)
    json.put("otp_code", otpCode)

    sendJsonToApi("https://polaris-server-30ha.onrender.com/api/verify_otp/", json)
}
