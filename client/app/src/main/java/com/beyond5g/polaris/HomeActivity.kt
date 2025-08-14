package com.beyond5g.polaris

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.beyond5g.polaris.Connector.Companion.httpDownload

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
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                if (true){

                    httpDownload(
                        "https://polaris-server-30ha.onrender.com/api/download_test/",
                        onSuccess = { mbps ->
                            Log.d("API", "Throughput: %.2f Mbps".format(mbps))
                        },
                        onError = { error ->
                            Log.e("API", error)
                        }
                    )


                }else if (true) {
                    val cellInfoId = 4
                    val timestamp = java.time.LocalDateTime.now()
                        .toString()
                        .replace("T", " ")

                    Connector.sendTest(
                        type_ = "sms",
                        phoneNumber = "5235244",
                        timestamp = timestamp,
                        cellInfo = cellInfoId,
                        prop = "send_time",
                        propVal = "204"
                    )
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
                                        rxlev = cellDetector.rxlev
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
