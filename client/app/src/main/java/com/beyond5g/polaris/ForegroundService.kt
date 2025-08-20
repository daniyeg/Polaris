package com.beyond5g.polaris

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.time.LocalDateTime


class DataUploadService : Service() {

    companion object {
        private const val CHANNEL_ID = "upload_channel"
        private const val CHANNEL_NAME = "Data Upload Service"
        private const val NOTIFICATION_ID = 1
    }

    private var checkbox_sms = false
    private var checkbox_dns = false
    private var checkbox_ping = false
    private var checkbox_download = false
    private var checkbox_upload = false
    private var checkbox_web = false

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Use a background thread looper, not main
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                sendDataToServer()
                handler.postDelayed(this, 15_000) // every 15 sec
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background data upload service"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkbox_sms = intent?.getBooleanExtra("checkbox_sms", false) ?: false
        checkbox_dns = intent?.getBooleanExtra("checkbox_dns", false) ?: false
        checkbox_ping = intent?.getBooleanExtra("checkbox_ping", false) ?: false
        checkbox_download = intent?.getBooleanExtra("checkbox_download", false) ?: false
        checkbox_upload = intent?.getBooleanExtra("checkbox_upload", false) ?: false
        checkbox_web = intent?.getBooleanExtra("checkbox_web", false) ?: false

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // 🔥 FIX #1: remove duplicate call to sendDataToServer()
        // Only run via handler loop
        handler.removeCallbacks(runnable) // ensure only one loop
        handler.post(runnable)

        // 🔥 FIX #2: Keep service alive if app is swiped away
        return START_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Data Upload Service")
            .setContentText("Uploading network measurements in background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null


    private fun sendDataToServer() {
        Thread { // 🔥 FIX #3: run in background thread
            val cellDetector = CellDetector(applicationContext)
            Log.d("DEBUG", "Service loop executed!")

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("DEBUG", "Location permission not granted")
                return@Thread
            }

            val prefs = getSharedPreferences("polaris", MODE_PRIVATE)
            val username = prefs.getString("username", null)

            if (username == null) {
                Log.e("DEBUG", "Username not found in shared preferences")
                return@Thread
            }

            Log.d("DEBUG", "Username: $username")

            Connector.getPhone(username,
                onSuccess = { phoneNumber ->
                    Log.d("DEBUG", "Phone Number: $phoneNumber")
                    processLocationAndCellData(phoneNumber, cellDetector)
                },
                onError = { error ->
                    Log.e("DEBUG", "Failed to get phone number: $error")
                }
            )
        }.start()
    }

    private fun processLocationAndCellData(phoneNumber: String, cellDetector: CellDetector) {
        val locationDetector = LocationDetector(this)

        // Add permission check here instead of on the lambda
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("DEBUG", "Location permission not granted")
            return
        }

        locationDetector.getCurrentLocation(
            onSuccess = { lat, lng ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cellDetector.updateCellDetails()

                    val timestamp = LocalDateTime.now()
                        .toString()
                        .replace("T", " ")

                    if (lat != null && lng != null &&
                        cellDetector.cid != null &&
                        cellDetector.plmn != null &&
                        cellDetector.type != null) {

                        sendCellInfoToServer(phoneNumber, lat, lng, timestamp, cellDetector)
                    } else {
                        Log.e("SendCellInfo", "Missing required fields: " +
                                "lat=$lat, lng=$lng, cid=${cellDetector.cid}, " +
                                "plmn=${cellDetector.plmn}, type=${cellDetector.type}")
                    }
                }
            },
            onFailure = { e ->
                Log.e("Location", "Failed to get location", e)
            }
        )
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private fun sendCellInfoToServer(
        phoneNumber: String,
        lat: Double,
        lng: Double,
        timestamp: String,
        cellDetector: CellDetector
    ) {
        Connector.sendCellInfo(
            phoneNumber = phoneNumber,
            lat = lat,
            lng = lng,
            timestamp = timestamp,
            gen = cellDetector.getNetworkGen() ?: "",
            tech = cellDetector.getNetworkTech() ?: "",
            plmn = cellDetector.plmn!!,
            cid = cellDetector.cid!!.toInt(),
            lac = cellDetector.lac,
            rac = null,
            tac = cellDetector.tac,
            afrn = cellDetector.arfcn?.toDouble(),
            freq = cellDetector.frequencyMHz,
            rsrp = cellDetector.rsrp,
            rsrq = cellDetector.rsrq,
            rscp = cellDetector.rscp,
            ecno = cellDetector.ecn0,
            rxlev = cellDetector.rxlev,
            onSuccess = { idOrResponse ->
                Log.d("Debug", "Cell info sent successfully. Response: $idOrResponse")
                val cellInfoId = try {
                    idOrResponse.toInt()
                } catch (e: NumberFormatException) {
                    Log.e("Debug", "Failed to parse cell info ID: $idOrResponse")
                    null
                }

                if (cellInfoId != null) {
                    performAdditionalTests(phoneNumber, timestamp, cellInfoId)
                }
            },
            onError = { error ->
                Log.e("Debug", "Failed to send cell info: $error")
            }
        )
    }

    private fun performAdditionalTests(phoneNumber: String, timestamp: String, cellInfoId: Int) {
        if (checkbox_sms) {
            performSmsTest(phoneNumber, timestamp, cellInfoId)
        }
        if (checkbox_dns) {
            performDnsTest(phoneNumber, timestamp, cellInfoId)
        }
        if (checkbox_ping) {
            performPingTest(phoneNumber, timestamp, cellInfoId)
        }
        if (checkbox_download) {
            performDownloadTest(phoneNumber, timestamp, cellInfoId)
        }
        if (checkbox_upload) {
            performUploadTest(phoneNumber, timestamp, cellInfoId)
        }
        if (checkbox_web) {
            performWebTest(phoneNumber, timestamp, cellInfoId)
        }
    }

    private fun performSmsTest(phoneNumber: String, timestamp: String, cellInfoId: Int) {

    }

    private fun performDnsTest(phoneNumber: String, timestamp: String, cellInfoId: Int) {
        Connector.dnsResolution(
            onSuccess = { ms ->
                Log.d("DNS", "DNS resolution time: %.2f ms".format(ms))
                Connector.sendTest(
                    type_ = "dns",
                    phoneNumber = phoneNumber,
                    timestamp = timestamp,
                    cellInfo = cellInfoId,
                    prop = "time",
                    propVal = ms,
                    onSuccess = { type_ ->
                        Log.d("Debug", "DNS test sent successfully")
                    },
                    onError = { error ->
                        Log.d("Debug", "DNS test failed to send: $error")
                    }
                )
            },
            onError = { error ->
                Log.e("DNS", "DNS test failed: $error")
            }
        )
    }

    private fun performPingTest(phoneNumber: String, timestamp: String, cellInfoId: Int) {
        Connector.pingResponse(
            onSuccess = { ms ->
                Log.d("PING", "Ping Time: %.2f ms".format(ms))
                Connector.sendTest(
                    type_ = "ping",
                    phoneNumber = phoneNumber,
                    timestamp = timestamp,
                    cellInfo = cellInfoId,
                    prop = "latency",
                    propVal = ms,
                    onSuccess = { type_ ->
                        Log.d("Debug", "Ping test sent successfully")
                    },
                    onError = { error ->
                        Log.d("Debug", "Ping test failed to send: $error")
                    }
                )
            },
            onError = { error ->
                Log.e("PING", "Ping test failed: $error")
            }
        )
    }

    private fun performDownloadTest(phoneNumber: String, timestamp: String, cellInfoId: Int) {
        Connector.httpDownload(
            onSuccess = { mbps ->
                Log.d("API", "Download Throughput: %.2f Mbps".format(mbps))
                Connector.sendTest(
                    type_ = "http_download",
                    phoneNumber = phoneNumber,
                    timestamp = timestamp,
                    cellInfo = cellInfoId,
                    prop = "throughput",
                    propVal = mbps,
                    onSuccess = { type_ ->
                        Log.d("Debug", "Download test sent successfully")
                    },
                    onError = { error ->
                        Log.d("Debug", "Download test failed to send: $error")
                    }
                )
            },
            onError = { error ->
                Log.e("API", "Download test failed: $error")
            }
        )
    }

    private fun performUploadTest(phoneNumber: String, timestamp: String, cellInfoId: Int) {
        Connector.httpUpload(
            onSuccess = { mbps ->
                Log.d("API", "Upload Throughput: %.2f Mbps".format(mbps))
                Connector.sendTest(
                    type_ = "http_upload",
                    phoneNumber = phoneNumber,
                    timestamp = timestamp,
                    cellInfo = cellInfoId,
                    prop = "throughput",
                    propVal = mbps,
                    onSuccess = { type_ ->
                        Log.d("Debug", "Upload test sent successfully")
                    },
                    onError = { error ->
                        Log.d("Debug", "Upload test failed to send: $error")
                    }
                )
            },
            onError = { error ->
                Log.e("API", "Upload test failed: $error")
            }
        )
    }

    private fun performWebTest(phoneNumber: String, timestamp: String, cellInfoId: Int) {
        Connector.webAnswer(
            onSuccess = { ms ->
                Log.d("API", "Web Answer: %.2f ms".format(ms))
                Connector.sendTest(
                    type_ = "web",
                    phoneNumber = phoneNumber,
                    timestamp = timestamp,
                    cellInfo = cellInfoId,
                    prop = "response_time",
                    propVal = ms,
                    onSuccess = { type_ ->
                        Log.d("Debug", "Web test sent successfully")
                    },
                    onError = { error ->
                        Log.d("Debug", "Web test failed to send: $error")
                    }
                )
            },
            onError = { error ->
                Log.e("API", "Web test failed: $error")
            }
        )
    }
}