package com.beyond5g.polaris

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log

class HomeActivity : ComponentActivity() {

    private val PERMISSION_REQUEST_CODE = 200
    private var isStarted = false
    private val checkBoxStates = mutableMapOf<Int, Boolean>()

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.SEND_SMS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)


        val checkBoxes = listOf(
            R.id.checkbox_dns,
            R.id.checkbox_ping,
            R.id.checkbox_download,
            R.id.checkbox_upload,
            R.id.checkbox_web,
            R.id.checkbox_sms
        )

        checkBoxes.forEach { checkBoxId ->
            val checkBox: CheckBox = findViewById(checkBoxId)
            checkBoxStates[checkBoxId] = checkBox.isChecked

            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                val previousState = checkBoxStates[checkBoxId]
                if (previousState != isChecked) {
                    Log.d("CheckBox", "${buttonView.text} changed to: $isChecked")
                    checkBoxStates[checkBoxId] = isChecked

                    if (isStarted){
                        stopServiceWithCheckboxes()

                        startServiceWithCheckboxes()
                    }

                }
            }
        }

        val startButton = findViewById<Button>(R.id.start)
        startButton.setOnClickListener {
            isStarted = !isStarted

            updateButtonState(startButton)
            if (checkAndRequestPermissions()) {

                if(isStarted){
                    startServiceWithCheckboxes()
                }else{
                    stopServiceWithCheckboxes()
                }
            }
        }


    }

    private fun checkAndRequestPermissions(): Boolean {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            startServiceWithCheckboxes()
        } else {
            // some permissions denied
        }
    }

//    private fun requestIgnoreBatteryOptimizations() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent()
//            val packageName = packageName
//            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
//
//            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
//                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//                intent.data = Uri.parse("package:$packageName")
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//            }
//        }
//    }

    private fun startServiceWithCheckboxes() {
        val checkbox_sms = findViewById<CheckBox>(R.id.checkbox_sms).isChecked
        val checkbox_dns = findViewById<CheckBox>(R.id.checkbox_dns).isChecked
        val checkbox_ping = findViewById<CheckBox>(R.id.checkbox_ping).isChecked
        val checkbox_download = findViewById<CheckBox>(R.id.checkbox_download).isChecked
        val checkbox_upload = findViewById<CheckBox>(R.id.checkbox_upload).isChecked
        val checkbox_web = findViewById<CheckBox>(R.id.checkbox_web).isChecked

        val serviceIntent = Intent(this, DataUploadService::class.java).apply {
            putExtra("checkbox_sms", checkbox_sms)
            putExtra("checkbox_dns", checkbox_dns)
            putExtra("checkbox_ping", checkbox_ping)
            putExtra("checkbox_download", checkbox_download)
            putExtra("checkbox_upload", checkbox_upload)
            putExtra("checkbox_web", checkbox_web)
        }
        startService(serviceIntent)
    }

    private fun stopServiceWithCheckboxes() {
        val serviceIntent = Intent(this, DataUploadService::class.java)

        stopService(serviceIntent)
    }

    private fun updateButtonState(button: Button) {
        if (isStarted) {
            button.text = "Stop"
            button.background = ContextCompat.getDrawable(this, R.drawable.circle_button_red)
        } else {
            button.text = "Start"
            button.background = ContextCompat.getDrawable(this, R.drawable.circle_button_green)
        }
    }
}

