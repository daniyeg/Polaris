package com.beyond5g.polaris

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.util.Log

class HomeActivity : ComponentActivity() {

    private lateinit var cellDetector: CellDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Initialize detector with context
        cellDetector = CellDetector(this)

        val startButton = findViewById<Button>(R.id.start)
        startButton.setOnClickListener @androidx.annotation.RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) {
            // Request permission if needed
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
                val locationDetector = LocationDetector(this)
                locationDetector.getCurrentLocation(
                    onSuccess = { lat, lng ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            cellDetector.updateCellDetails()

                            val timestamp =
                                java.time.LocalDateTime.now().toString().replace("T", " ")

                            if (lat != null && lng != null && cellDetector.cid != null && cellDetector.plmn != null && cellDetector.type != null) {
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
                                    //freqBand = cellDetector.band,
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
                    onFailure = { Log.e("Location", "Failed to get location", it) }
                )
            }
        }
    }
}
