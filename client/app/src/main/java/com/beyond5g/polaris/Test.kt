package com.beyond5g.polaris

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import kotlin.system.measureTimeMillis

class Test {
    private val client = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_1_1))
        .connectTimeout(100, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(200, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(100, java.util.concurrent.TimeUnit.SECONDS)
        .followRedirects(true)  // Enable redirect following
        .followSslRedirects(true)
        .build()


    fun httpDownloadTest(
        url: String,
        onResult: (throughputMbps: Double, totalMB: Double, timeSec: Double) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Connection", "close")
                    .header("Accept-Encoding", "identity")  // Disable compression
                    .header("User-Agent", "Mozilla/5.0 (Android)")
                    .header("Accept", "*/*")  // Some servers expect this
                    .build()

                var totalBytes = 0L
                var chunkCount = 0
                val timeMillis = measureTimeMillis {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            println("HTTP error: ${response.code}")
                            return@use
                        }

                        println("Response code: ${response.code}")
                        println("Content length: ${response.body?.contentLength()}")

                        response.body?.byteStream()?.use { inputStream ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            while (true) {
                                bytesRead = inputStream.read(buffer)
                                if (bytesRead == -1) break
                                totalBytes += bytesRead
                                chunkCount++
                                println("Read chunk #$chunkCount with $bytesRead bytes")
                            }
                        }
                    }
                }

                val timeSec = timeMillis / 1000.0
                val throughputMbps = if (timeSec > 0)
                    (totalBytes * 8) / (timeSec * 1024 * 1024)
                else 0.0
                val totalMB = totalBytes / (1024.0 * 1024.0)

                withContext(Dispatchers.Main) {
                    println("Total bytes read: $totalBytes")
                    onResult(throughputMbps, totalMB, timeSec)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("Download failed: ${e.message}")
                    onResult(-1.0, 0.0, 0.0)
                }
            }
        }
    }
}
