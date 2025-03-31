package com.example.q1_maps

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.location.Geocoder
import com.example.q1_maps.ui.theme.Q1MapsTheme
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import android.util.Log

// PLEASE READ
// Since the emulator does not fetch the location, I set the location in the device settings
// by going to the toolbar and selecting extended settings, and choosing location.
// I also connected my personal Android phone, and it functioned on my phone. I hope that is
// sufficient to show that the code works.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Q1MapsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    internal fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    internal fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var address by remember { mutableStateOf("Fetching address...") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val geocoder = Geocoder(context)
    val mainActivity = context as MainActivity

    // Request permission if not granted
    if (!mainActivity.hasLocationPermission()) {
        LaunchedEffect(Unit) {
            mainActivity.requestLocationPermission()
        }
    }

    // Actively request location updates
    LaunchedEffect(Unit) {
        if (mainActivity.hasLocationPermission()) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000 // Update interval in milliseconds (10 seconds)
            ).build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                        locationResult.lastLocation?.let { location ->
                            val latLng = LatLng(location.latitude, location.longitude)
                            userLocation = latLng
                            Log.d("MapScreen", "Location fetched: $latLng")
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            address = addresses?.firstOrNull()?.getAddressLine(0) ?: "No address found"
                            Log.d("MapScreen", "Address updated: $address")
                            fusedLocationClient.removeLocationUpdates(this)
                        } ?: Log.d("MapScreen", "Location is null")
                    }
                },
                null
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(text = address, modifier = Modifier.padding(16.dp))

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    onCreate(Bundle())
                    onResume()
                    getMapAsync { googleMap ->
                        if (mainActivity.hasLocationPermission()) {
                            googleMap.isMyLocationEnabled = true
                        }
                        // No default location here; wait for userLocation
                        googleMap.setOnMapClickListener { latLng ->
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("Custom Marker")
                            )
                        }
                    }
                }
            },
            update = { mapView ->
                mapView.getMapAsync { googleMap ->
                    userLocation?.let {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                        Log.d("MapScreen", "Map moved to: $it")
                    } ?: Log.d("MapScreen", "userLocation is null, map not moved")
                }
            }
        )
    }
}