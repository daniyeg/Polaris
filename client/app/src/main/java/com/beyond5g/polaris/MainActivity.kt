package com.beyond5g.polaris

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.beyond5g.polaris.ui.theme.PolarisTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Use AppDatabase to insert Test row in IO thread
//        val db = AppDatabase.getDatabase(applicationContext)
//        val dao = db.testDao()

        val test = Test(
            phone_number = "09123456789",
            timestamp = System.currentTimeMillis(),
            cell_info_id = 1
        )

//        CoroutineScope(Dispatchers.IO).launch {
//            dao.insert(test)
//        }

        // ✅ Show UI
        setContent {
            PolarisTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Hello $name!")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
//            sendItem("hmmmmmm")
//            sendTest(
//                type_ = "http_download",
//                phoneNumber = "5235244",
//                timestamp = "2025-06-29T05:03:31.821Z",
//                cellInfo = 1,
//                detail = mapOf("throughput" to "50.0")
//            )

            sendCellInfo(
                phoneNumber = "5235242",
                lat = 35.6892,
                lng = 51.3890,
                timestamp = "2025-06-29T10:00:00.000Z",
                gen = "4G",
                tech = "NR",
                plmn = "43211",
                cid = 123456,
                rsrp = -95.0,
                rsrq = -10.0
            )


        }) {
            Text("Send Data")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PolarisTheme {
        Greeting("Android")
    }
}