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
                if (false) {
                    // ------------------- TESTS -------------------
                    val checkbox_sms = findViewById<CheckBox>(R.id.checkbox_sms).isChecked
                    val checkbox_dns = findViewById<CheckBox>(R.id.checkbox_dns).isChecked
                    val checkbox_ping = findViewById<CheckBox>(R.id.checkbox_ping).isChecked
                    val checkbox_download = findViewById<CheckBox>(R.id.checkbox_download).isChecked
                    val checkbox_upload = findViewById<CheckBox>(R.id.checkbox_upload).isChecked
                    val checkbox_web = findViewById<CheckBox>(R.id.checkbox_web).isChecked

                    var dnsTimeMs: Double? = null
                    var pingTimeMs: Double? = null
                    var downloadMbps: Double? = null
                    var uploadMbps: Double? = null
                    var webAnswerMs: Double? = null

                    if (checkbox_sms) {
                        // TODO: implement SMS test
                    }

                    if (checkbox_dns) {
                        dnsResolution(
                            onSuccess = { ms ->
                                dnsTimeMs = ms
                                Log.d("DNS", "DNS resolution time: %.2f ms".format(ms))
                            },
                            onError = { error -> Log.e("DNS", error) }
                        )
                    }

                    if (checkbox_ping) {
                        pingResponse(
                            onSuccess = { ms ->
                                pingTimeMs = ms
                                Log.d("PING", "Ping Time: %.2f ms".format(ms))
                            },
                            onError = { error -> Log.e("PING", error) }
                        )
                    }

                    if (checkbox_download) {
                        httpDownload(
                            onSuccess = { mbps ->
                                downloadMbps = mbps
                                Log.d("API", "Download Throughput: %.2f Mbps".format(mbps))
                            },
                            onError = { error -> Log.e("API", error) }
                        )
                    }

                    if (checkbox_upload) {
                        httpUpload(
                            onSuccess = { mbps ->
                                uploadMbps = mbps
                                Log.d("API", "Upload Throughput: %.2f Mbps".format(mbps))
                            },
                            onError = { error -> Log.e("API", error) }
                        )
                    }

                    if (checkbox_web) {
                        webAnswer(
                            onSuccess = { ms ->
                                webAnswerMs = ms
                                Log.d("API", "Web Answer: %.2f ms".format(ms))
                            },
                            onError = { error -> Log.e("API", error) }
                        )
                    }

                } else {
                    val locationDetector = LocationDetector(this)

                    locationDetector.getCurrentLocation(
                        onSuccess = { lat, lng ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                cellDetector.updateCellDetails()

                                val timestamp = java.time.LocalDateTime.now()
                                    .toString()
                                    .replace("T", " ")

                                if (lat != null &&
                                    lng != null &&
                                    cellDetector.cid != null &&
                                    cellDetector.plmn != null &&
                                    cellDetector.type != null
                                ) {

                                    Connector.sendCellInfo(
                                        phoneNumber = "5235242",
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
                                            Log.d("Debug", "✅ API Success, Returned: $idOrResponse")
                                            var cellInfoId: Int? = null

                                            cellInfoId = idOrResponse.toInt()

                                            val timestamp = java.time.LocalDateTime.now()
                                                .toString()
                                                .replace("T", " ")

                                            Connector.sendTest(
                                                type_ = "sms",
                                                phoneNumber = "5235244",
                                                timestamp = timestamp,
                                                cellInfo = cellInfoId,
                                                prop = "send_time",
                                                propVal = ,
                                                onSuccess = { type_ ->
                                                    Log.d("Debug","test sent")},
                                                onError = { type_ ->
                                                    Log.d("Debug","test NOT sent")}
                                            )



                                        },
                                        onError = { error ->
                                            Log.e("Debug", "❌ API Error: $error")
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



                }
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
