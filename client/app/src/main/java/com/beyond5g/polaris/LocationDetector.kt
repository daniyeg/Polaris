package com.beyond5g.polaris

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationDetector(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (lat: Double, lng: Double) -> Unit,
        onFailure: (exception: Exception) -> Unit = { Log.e("Location", "Error", it) }
    ) {
        if (!hasLocationPermission()) {
            onFailure(SecurityException("Location permission not granted"))
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    requestFreshLocation(onSuccess, onFailure)
                }
            }
            .addOnFailureListener(onFailure)
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(
        onSuccess: (lat: Double, lng: Double) -> Unit,
        onFailure: (exception: Exception) -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    onSuccess(location.latitude, location.longitude)
                }
                removeLocationUpdates()
            }
        }

        locationCallback?.let { callback ->
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        }
    }

    fun removeLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        fun checkOrRequestPermission(
            activity: Activity,
            onGranted: () -> Unit,
            onDenied: () -> Unit = {}
        ) {
            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> onGranted()

                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                    // Show explanation dialog if needed
                    onDenied()
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }

                else -> {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }

        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}