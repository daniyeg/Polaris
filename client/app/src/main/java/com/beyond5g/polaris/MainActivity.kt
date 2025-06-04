package com.beyond5g.polaris

import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beyond5g.polaris.ui.theme.PolarisTheme
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {
    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        val requiredPermissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.ACCESS_NETWORK_STATE
        )

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        locationHelper = LocationHelper(this)

        setContent {
            PolarisTheme {
                val locationText = remember { mutableStateOf("Location not available") }
                val timeText = remember { mutableStateOf("Time not set") }
                val generationText = remember { mutableStateOf("Network generation not detected") }
                val generationTechText = remember { mutableStateOf("Network Technology not detected") }

                val plmnText = remember { mutableStateOf("N/A") }
                val lacText = remember { mutableStateOf("N/A") }
                val racText = remember { mutableStateOf("N/A") }
                val tacText = remember { mutableStateOf("N/A") }
                val cidText = remember { mutableStateOf("N/A") }
                val arfcnText = remember { mutableStateOf("N/A") }
                val bandText = remember { mutableStateOf("N/A") }
                val freqText = remember { mutableStateOf("N/A") }
                val rsrpText = remember { mutableStateOf("N/A") }
                val rsrqText = remember { mutableStateOf("N/A") }
                val rscpText = remember { mutableStateOf("N/A") }
                val ecn0Text = remember { mutableStateOf("N/A") }
                val rxlevText = remember { mutableStateOf("N/A") }

                val cellDetector = remember { CellDetector(this@MainActivity) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Greeting(name = "Android")

                        Text(text = locationText.value)
                        Text(text = timeText.value)
                        Text(text = generationText.value)
                        Text(text = generationTechText.value)
                        Text(text = "PLMN ID: ${plmnText.value}")
                        Text(text = "LAC: ${lacText.value}")
                        Text(text = "RAC: ${racText.value}")
                        Text(text = "TAC: ${tacText.value}")
                        Text(text = "Cell ID: ${cidText.value}")
                        Text(text = "ARFCN: ${arfcnText.value}")
                        Text(text = "Band: ${bandText.value}")
                        Text(text = "Frequency: ${freqText.value}")
                        Text(text = "rsrp: ${rsrpText.value}")
                        Text(text = "rsrq: ${rsrqText.value}")
                        Text(text = "rscp: ${rscpText.value}")
                        Text(text = "ecn0: ${ecn0Text.value}")
                        Text(text = "rxlev: ${rxlevText.value}")


                        Button(onClick = {
                            if (!hasPhoneStatePermission()) {
                                requestPhoneStatePermission()
                                return@Button
                            }

                            LocationHelper.checkOrRequestPermission(
                                this@MainActivity,
                                onGranted = {
                                    val currentDate = DateTimeHelper.getCurrentDate()
                                    val currentTime = DateTimeHelper.getCurrentTime("HH:mm:ss")
                                    val gen = cellDetector.getNetworkGen()
                                    val genTech = cellDetector.getNetworkTech()
                                    generationText.value = gen.toString()
                                    generationTechText.value = if (gen == "WIFI") "-" else genTech.toString()

                                    locationHelper.getCurrentLocation(
                                        onSuccess = { lat, lng ->
                                            locationText.value = "Lat: $lat, Lng: $lng"
                                            timeText.value = "$currentDate $currentTime"
                                        },
                                        onFailure = {
                                            locationText.value = "Error: ${it.message}"
                                        }
                                    )


                                    cellDetector.updateCellDetails()
                                    plmnText.value = cellDetector.plmn ?: "N/A"
                                    lacText.value = (cellDetector.lac?: "N/A").toString()
                                    racText.value = cellDetector.rac?.toString() ?: "N/A"
                                    tacText.value = cellDetector.tac?.toString() ?: "N/A"
                                    cidText.value = cellDetector.cid?.toString() ?: "N/A"
                                    arfcnText.value = cellDetector.arfcn?.toString() ?: "N/A"
                                    bandText.value = cellDetector.band ?: "N/A"
                                    freqText.value = cellDetector.frequencyMHz?.toString() ?: "N/A"
                                    rsrpText.value = cellDetector.rsrp?.toString() ?: "N/A"
                                    rsrqText.value = cellDetector.rsrq?.toString() ?: "N/A"
                                    rscpText.value = cellDetector.rscp?.toString() ?: "N/A"
                                    ecn0Text.value = cellDetector.ecn0?.toString() ?: "N/A"
                                    rxlevText.value = cellDetector.rxlev?.toString() ?: "N/A"

                                },
                                onDenied = {
                                    locationText.value = "Permission denied"
                                }
                            )
                        }) {
                            Text("Get Location & Network")
                        }
                    }
                }
            }
        }

    }

    private fun hasPhoneStatePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPhoneStatePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            101
        )
    }


    override fun onPause() {
        super.onPause()
        locationHelper.removeLocationUpdates()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PolarisTheme {
        Greeting("Android")
    }
}



