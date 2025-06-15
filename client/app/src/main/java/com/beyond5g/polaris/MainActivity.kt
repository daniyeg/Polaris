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
        enableEdgeToEdge()

        // ✅ Initialize and insert a test row into the database
        val db = DatabaseProvider.getDatabase(applicationContext)
        val dao = db.testDao()
        val test = Test(
            phonenumber = "09123456789",
            timestamp = System.currentTimeMillis(),
            cell_info_id = 1
        )

        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(test)
        }

        setContent {
            PolarisTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
            sendUserData()
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