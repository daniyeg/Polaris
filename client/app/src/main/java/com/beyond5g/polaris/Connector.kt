package com.beyond5g.polaris

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.InetAddress
import kotlin.concurrent.thread
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlin.random.Random



class Connector {
    companion object {

        // Overload: No callbacks (for fire-and-forget requests)
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
                    onError("Request failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            if (jsonResponse.has("id")) {
                                onSuccess(jsonResponse.get("id").toString())
                            } else {
                                onSuccess(responseBody)
                            }
                        } catch (e: Exception) {
                            onSuccess(responseBody) // not JSON, just return raw
                        }
                    } else {
                        onError("Server error (${response.code}): $responseBody")
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
            rxlev: Double? = null,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit

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

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://polaris-server-30ha.onrender.com/api/add_cell_info/")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError("Request failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            if (jsonResponse.has("id")) {
                                onSuccess(jsonResponse.get("id").toString())
                            } else {
//                                onSuccess(responseBody)
                            }
                        } catch (e: Exception) {
//                            onSuccess(responseBody) // not JSON, just return raw
                        }
                    } else {
                        onError("Server error (${response.code}): $responseBody")
                    }
                }
            })
        }

        fun sendTest(
            type_: String,
            phoneNumber: String,
            timestamp: String,
            cellInfo: Int,
            prop: String,
            propVal: String,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit

        ) {
            val detailJson = JSONObject()
            detailJson.put(prop, propVal)

            val json = JSONObject()
            json.put("type_", type_)
            json.put("phone_number", phoneNumber)
            json.put("timestamp", timestamp)
            json.put("cell_info", cellInfo)
            json.put("detail", detailJson)

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://polaris-server-30ha.onrender.com/api/add_test/")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError("Request failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            if (jsonResponse.has("id")) {
                                onSuccess(jsonResponse.get("id").toString())
                            } else {
                                onSuccess(responseBody)
                            }
                        } catch (e: Exception) {
                            onSuccess(responseBody) // not JSON, just return raw
                        }
                    } else {
                        onError("Server error (${response.code}): $responseBody")
                    }
                }
            })
        }

        fun sendSmsWithDeliveryTest(
            phoneNumber: String,
            message: String,
            context: Context
        ) {
            val smsManager = SmsManager.getDefault()

            val sentIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent("SMS_SENT"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val deliveredIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent("SMS_DELIVERED"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val startTime = System.currentTimeMillis()

            // Register delivery receiver
            ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    val endTime = System.currentTimeMillis()
                    val deliveryTime = endTime - startTime
                    println(" SMS Delivery time: ${deliveryTime} ms")
                    context.unregisterReceiver(this)
                }
            }, IntentFilter("SMS_DELIVERED"), ContextCompat.RECEIVER_NOT_EXPORTED)

            smsManager.sendTextMessage(
                phoneNumber,
                null,
                message,
                sentIntent,
                deliveredIntent
            )
        }


        fun pingResponse(
            host: String = "8.8.8.8",
            onSuccess: (Double) -> Unit,
            onError: (String) -> Unit
        ) {
            thread {
                try {
                    // Run one ping and exit (-c 1)
                    val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 $host")
                    val startTime = System.nanoTime()
                    val exitCode = process.waitFor()
                    val endTime = System.nanoTime()

                    if (exitCode == 0) {
                        val elapsedMs = (endTime - startTime) / 1_000_000.0
                        onSuccess(elapsedMs)
                    } else {
                        onError("Ping failed with exit code $exitCode")
                    }
                } catch (e: Exception) {
                    onError("Ping error: ${e.message}")
                }
            }
        }

        fun dnsResolution(
            hostname: String = "google.com",
            onSuccess: (Double) -> Unit,
            onError: (String) -> Unit
        ) {
            thread {
                try {
                    val startTime = System.nanoTime()
                    InetAddress.getByName(hostname) // DNS resolution happens here
                    val endTime = System.nanoTime()

                    val elapsedMs = (endTime - startTime) / 1_000_000.0 // milliseconds
                    onSuccess(elapsedMs)
                } catch (e: Exception) {
                    onError("DNS resolution failed: ${e.message}")
                }
            }
        }

        fun webAnswer(
            apiUrl: String = "https://api.github.com",
            onSuccess: (Double) -> Unit,
            onError: (String) -> Unit
        ) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(apiUrl)
                .get()
                .build()

            val startTime = System.nanoTime()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError("Network error: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val endTime = System.nanoTime()
                    val elapsedMs = (endTime - startTime) / 1_000_000.0 // milliseconds
                    response.close()
                    onSuccess(elapsedMs)
                }
            })
        }

        fun httpUpload(
            apiUrl: String = "https://polaris-server-30ha.onrender.com/api/upload_test/",
            onSuccess: (Double) -> Unit,
            onError: (String) -> Unit
        ) {
            val client = OkHttpClient()

            // Generate a 1MB random byte array
            val sizeBytes = 1 * 1024 * 1024
            val byteArray = ByteArray(sizeBytes)
            Random.Default.nextBytes(byteArray)

            // Create a request body for the file
            val fileBody = byteArray.toRequestBody("application/octet-stream".toMediaType())

            // Multipart form data
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "test.bin", fileBody)
                .build()

            val request = Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build()

            val startTime = System.nanoTime()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError("Network error: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val endTime = System.nanoTime()

                    if (!response.isSuccessful) {
                        onError("Server error (${response.code})")
                        return
                    }

                    val elapsedSeconds = (endTime - startTime) / 1_000_000_000.0
                    val bits = sizeBytes * 8.0
                    val throughputMbps = bits / elapsedSeconds / 1_000_000

                    onSuccess(throughputMbps)
                }
            })
        }


        fun httpDownload(
            apiUrl: String = "https://polaris-server-30ha.onrender.com/api/download_test/",
            onSuccess: (Double) -> Unit,
            onError: (String) -> Unit
        ) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(apiUrl)
                .get()
                .build()

            val startTime = System.nanoTime()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError("Network error: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        onError("Server error (${response.code})")
                        return
                    }

                    val bytes = response.body?.bytes() ?: ByteArray(0)
                    val endTime = System.nanoTime()

                    val elapsedSeconds = (endTime - startTime) / 1_000_000_000.0
                    val bits = bytes.size * 8.0
                    val throughputMbps = bits / elapsedSeconds / 1_000_000  // megabits per second

//                    Log.d("LOGIN_DEBUG", "seconds: $elapsedSeconds")
//                    Log.d("LOGIN_DEBUG", "size: $bits")

                    onSuccess(throughputMbps)
                }
            })
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
