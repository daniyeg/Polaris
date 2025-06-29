package com.beyond5g.polaris

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

fun sendItem(name: String) {

    val client = OkHttpClient()

    val json = JSONObject()
    json.put("name", name)

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://polaris-server-30ha.onrender.com/api/add/")
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

fun sendTest(
    type_: String,
    phoneNumber: String,
    timestamp: String,
    cellInfo: Int,
    detail: Map<String, String>
) {
    val client = OkHttpClient()

    // Build nested detail object
    val detailJson = JSONObject()
    for ((key, value) in detail) {
        detailJson.put(key, value)
    }

    // Build main JSON body
    val json = JSONObject()
    json.put("type_", type_)
    json.put("phone_number", phoneNumber)
    json.put("timestamp", timestamp)
    json.put("cell_info", cellInfo)
    json.put("detail", detailJson)

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://polaris-server-30ha.onrender.com/api/add_test/")
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
    val client = OkHttpClient()

    val json = JSONObject()
    json.put("phone_number", phoneNumber)
    json.put("lat", lat)
    json.put("lng", lng)
    json.put("timestamp", timestamp)
    json.put("gen", gen)
    json.put("tech", tech)
    json.put("plmn", plmn)
    json.put("cid", cid)

    // Optional fields only added if not null
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

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://polaris-server-30ha.onrender.com/api/add_cell_info/")
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