package com.beyond5g.polaris

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.beyond5g.polaris.Connector.Companion.httpDownload
import com.beyond5g.polaris.Connector.Companion.httpUpload
import com.beyond5g.polaris.Connector.Companion.dnsResolution
import com.beyond5g.polaris.Connector.Companion.getPhone
import com.beyond5g.polaris.Connector.Companion.pingResponse
import com.beyond5g.polaris.Connector.Companion.webAnswer
import org.json.JSONException
import org.json.JSONObject
import okhttp3.ResponseBody
class HomeActivity : ComponentActivity() {

    private lateinit var cellDetector: CellDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        cellDetector = CellDetector(this)
        requestPhoneStatePermission(this)

        val startButton = findViewById<Button>(R.id.start)
        startButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS
                    ),
                    1
                )
            } else {
                    val checkbox_sms = findViewById<CheckBox>(R.id.checkbox_sms).isChecked
                    val checkbox_dns = findViewById<CheckBox>(R.id.checkbox_dns).isChecked
                    val checkbox_ping = findViewById<CheckBox>(R.id.checkbox_ping).isChecked
                    val checkbox_download = findViewById<CheckBox>(R.id.checkbox_download).isChecked
                    val checkbox_upload = findViewById<CheckBox>(R.id.checkbox_upload).isChecked
                    val checkbox_web = findViewById<CheckBox>(R.id.checkbox_web).isChecked

                    var smsTimeMs: Double? = null
                    var dnsTimeMs: Double? = null
                    var pingTimeMs: Double? = null
                    var downloadMbps: Double? = null
                    var uploadMbps: Double? = null
                    var webAnswerMs: Double? = null

                    val prefs = getSharedPreferences("polaris", MODE_PRIVATE)

                    val username = prefs.getString("username", null)
                    Log.d("DEBUG", "username: $username")

                    getPhone(username,

                    onSuccess = { phoneNumber ->

                        Log.d("DEBUG", "Phone Number: $phoneNumber")

                        val locationDetector = LocationDetector(this)
                        locationDetector.getCurrentLocation(
                            onSuccess = { lat, lng ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    cellDetector.updateCellDetails()

                                    val timestamp = java.time.LocalDateTime.now()
                                        .toString()
                                        .replace("T", " ")

                                    if (lat != null && cellDetector.cid != null && cellDetector.plmn != null && cellDetector.type != null) {

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
                                                Log.d("Debug", "ID Returned: $idOrResponse")
                                                var cellInfoId: Int? = null

                                                cellInfoId = idOrResponse.toInt()

                                                if (checkbox_sms) {
                                                    if(checkbox_sms && smsTimeMs != null){
                                                        Connector.sendTest(
                                                            type_ = "sms",
                                                            phoneNumber = phoneNumber,
                                                            timestamp = timestamp,
                                                            cellInfo = cellInfoId,
                                                            prop = "send_time",
                                                            propVal = smsTimeMs,
                                                            onSuccess = { type_ ->
                                                                Log.d("Debug","SMS test sent")},
                                                            onError = { type_ ->
                                                                Log.d("Debug","SMS test NOT sent")}
                                                        )
                                                    }
                                                }

                                                if (checkbox_dns) {
                                                    dnsResolution(
                                                        onSuccess = { ms ->
                                                            dnsTimeMs = ms
                                                            Log.d("DNS", "DNS resolution time: %.2f ms".format(ms))
                                                            if(checkbox_dns && dnsTimeMs != null){
                                                                Connector.sendTest(
                                                                    type_ = "dns",
                                                                    phoneNumber = phoneNumber,
                                                                    timestamp = timestamp,
                                                                    cellInfo = cellInfoId,
                                                                    prop = "time",
                                                                    propVal = dnsTimeMs,
                                                                    onSuccess = { type_ ->
                                                                        Log.d("Debug","DNS test sent")},
                                                                    onError = { type_ ->
                                                                        Log.d("Debug","DNS test NOT sent")}
                                                                )
                                                            }
                                                        },
                                                        onError = { error -> Log.e("DNS", error) }
                                                    )
                                                }

                                                if (checkbox_ping) {
                                                    pingResponse(
                                                        onSuccess = { ms ->
                                                            pingTimeMs = ms
                                                            Log.d("PING", "Ping Time: %.2f ms".format(ms))
                                                            if(checkbox_ping && pingTimeMs != null){
                                                                Connector.sendTest(
                                                                    type_ = "ping",
                                                                    phoneNumber = phoneNumber,
                                                                    timestamp = timestamp,
                                                                    cellInfo = cellInfoId,
                                                                    prop = "latency",
                                                                    propVal = pingTimeMs,
                                                                    onSuccess = { type_ ->
                                                                        Log.d("Debug","Ping test sent")},
                                                                    onError = { type_ ->
                                                                        Log.d("Debug","Ping test NOT sent")}
                                                                )
                                                            }
                                                        },
                                                        onError = { error -> Log.e("PING", error) }
                                                    )
                                                }

                                                if (checkbox_download) {
                                                    httpDownload(
                                                        onSuccess = { mbps ->
                                                            downloadMbps = mbps
                                                            Log.d("API", "Download Throughput: %.2f Mbps".format(mbps))
                                                            if(checkbox_download && downloadMbps != null){
                                                                Connector.sendTest(
                                                                    type_ = "http_download",
                                                                    phoneNumber = phoneNumber,
                                                                    timestamp = timestamp,
                                                                    cellInfo = cellInfoId,
                                                                    prop = "throughput",
                                                                    propVal = downloadMbps,
                                                                    onSuccess = { type_ ->
                                                                        Log.d("Debug","Download test sent")},
                                                                    onError = { type_ ->
                                                                        Log.d("Debug","Download test NOT sent")}
                                                                )
                                                            }
                                                        },
                                                        onError = { error -> Log.e("API", error) }
                                                    )
                                                }

                                                if (checkbox_upload) {
                                                    httpUpload(
                                                        onSuccess = { mbps ->
                                                            uploadMbps = mbps
                                                            Log.d("API", "Upload Throughput: %.2f Mbps".format(uploadMbps))
                                                            if(checkbox_upload && uploadMbps != null){
                                                                Connector.sendTest(
                                                                    type_ = "http_upload",
                                                                    phoneNumber = phoneNumber,
                                                                    timestamp = timestamp,
                                                                    cellInfo = cellInfoId,
                                                                    prop = "throughput",
                                                                    propVal = uploadMbps,
                                                                    onSuccess = { type_ ->
                                                                        Log.d("Debug","Upload test sent")},
                                                                    onError = { type_ ->
                                                                        Log.d("Debug",type_)}
                                                                )
                                                            }
                                                        },
                                                        onError = { error -> Log.e("API", error) }
                                                    )
                                                }

                                                if (checkbox_web) {
                                                    webAnswer(
                                                        onSuccess = { ms ->
                                                            webAnswerMs = ms
                                                            Log.d("API", "Web Answer: %.2f ms".format(ms))
                                                            if(checkbox_web && webAnswerMs != null){
                                                                Connector.sendTest(
                                                                    type_ = "web",
                                                                    phoneNumber = phoneNumber,
                                                                    timestamp = timestamp,
                                                                    cellInfo = cellInfoId,
                                                                    prop = "response_time",
                                                                    propVal = webAnswerMs,
                                                                    onSuccess = { type_ ->
                                                                        Log.d("Debug","Web test sent")},
                                                                    onError = { type_ ->
                                                                        Log.d("Debug","Web test NOT sent")}
                                                                )
                                                            }
                                                        },
                                                        onError = { error -> Log.e("API", error) }
                                                    )
                                                }


                                            },
                                            onError = { error ->
                                                Log.e("Debug", "API Error: $error")
                                            }
                                        )

                                    } else {
                                        Log.e("SendCellInfo", "Missing required fields")
                                    }
                                }
                            },
                            onFailure = { e ->
                                Log.e("Location", "Failed to get location", e)
                            }
                        )
                    },
                        onError = { error -> Log.e("DNS", error) }
                    )


            }
        }
    }
}

// Permission request helper
fun requestPhoneStatePermission(activity: HomeActivity) {
    if (ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            1001
        )
    }
}
