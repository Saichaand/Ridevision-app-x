package com.ridevision.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ridevision.app.domain.detector.RoadHazardDetector
import com.ridevision.app.ui.RideVisionApp
import com.ridevision.app.ui.theme.RideVisionTheme
import com.ridevision.app.ui.viewmodel.RideVisionViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RideVisionViewModel by viewModels {
        RideVisionViewModel.provideFactory(this)
    }
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            initializeLocationClientAndStartUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        RoadHazardDetector.initialize(
            context = this,
            onReady = { /* optional: update a loading state if you track one */ },
            onError = { e -> android.util.Log.e("MainActivity", "Model init failed", e) }
        )

        // Check and request runtime location permissions before initializing LocationServices client
        checkAndRequestLocationPermissions()

        setContent {
            RideVisionTheme {
                RideVisionApp(viewModel = viewModel)
            }
        }
    }

    /**
     * Checks and requests ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions
     * at runtime before attempting to initialize the LocationServices client.
     */
    private fun checkAndRequestLocationPermissions() {
        val hasFine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            initializeLocationClientAndStartUpdates()
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * Safely initializes the FusedLocationProviderClient only after location permissions
     * have been confirmed and starts retrieving location updates.
     */
    private fun initializeLocationClientAndStartUpdates() {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                ?.addOnSuccessListener { loc ->
                    if (loc != null) {
                        val heading = if (loc.hasBearing()) loc.bearing else 45f
                        val speed = if (loc.hasSpeed()) loc.speed * 3.6f else 40f
                        viewModel.updateLocation(loc.latitude, loc.longitude, heading, speed)
                    }
                }
        } catch (e: SecurityException) {
            // Handled gracefully, simulation fallback remains active
        }
    }
}
