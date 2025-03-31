package com.example.q2_polygon

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                MapScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }

    internal fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

// I couldn't be asked to change the location, again, so just don't press the "Find me"
// button, or you're gonna lose the trail.
// (its in Odesa, Ukraine, if you wanna look for it, since that's my hometown)
//
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mainActivity = context as MainActivity
    val mapView = rememberMapViewWithLifecycle()

    // Default values for the polygons and polylines
    var polylineColor by remember { mutableStateOf(android.graphics.Color.BLUE) }
    var polylineWidth by remember { mutableStateOf(5f) }
    var polygonColor by remember { mutableStateOf(android.graphics.Color.GREEN) }
    var polygonWidth by remember { mutableStateOf(3f) }
    // References to the shapes so that we can call em later
    var polyline by remember { mutableStateOf<com.google.android.gms.maps.model.Polyline?>(null) }
    var polygon by remember { mutableStateOf<com.google.android.gms.maps.model.Polygon?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Polyline controls in two rows
        Text(text = "Polyline Controls", modifier = Modifier.padding(start = 16.dp, top = 16.dp))
        Row(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {
                polylineColor = android.graphics.Color.RED
                polyline?.color = polylineColor
            }) {
                Text("Red Polyline")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                polylineColor = android.graphics.Color.CYAN  // using MAGENTA as bright purple
                polyline?.color = polylineColor
            }) {
                Text("Cyan Polyline")
            }
        }
        Row(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {
                polylineWidth = 10f  // thick
                polyline?.width = polylineWidth
            }) {
                Text("Thick Polyline")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                polylineWidth = 5f  // thin
                polyline?.width = polylineWidth
            }) {
                Text("Thin Polyline")
            }
        }

        // Polygon controls in two rows
        Text(text = "Polygon Controls", modifier = Modifier.padding(start = 16.dp, top = 16.dp))
        Row(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {
                polygonColor = android.graphics.Color.GREEN
                polygon?.strokeColor = polygonColor
            }) {
                Text("Green Polygon")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                polygonColor = android.graphics.Color.BLUE
                polygon?.strokeColor = polygonColor
            }) {
                Text("Blue Polygon")
            }
        }
        Row(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {
                polygonWidth = 6f  // thick
                polygon?.strokeWidth = polygonWidth
            }) {
                Text("Thick Polygon")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                polygonWidth = 3f  // thin
                polygon?.strokeWidth = polygonWidth
            }) {
                Text("Thin Polygon")
            }
        }

        // Display the Google Maps aapp
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { mapView ->
                mapView.getMapAsync { googleMap ->
                    if (mainActivity.hasLocationPermission()) {
                        googleMap.isMyLocationEnabled = true
                    }
                    // Trail
                    val trailPoints = listOf(
                        LatLng(46.4825, 30.7233),
                        LatLng(46.4870, 30.7300),
                        LatLng(46.4920, 30.7250)
                    )
                    if (polyline == null) {
                        polyline = googleMap.addPolyline(
                            PolylineOptions()
                                .addAll(trailPoints)
                                .color(polylineColor)
                                .width(polylineWidth)
                                .clickable(true)
                        )
                    } else {
                        polyline?.color = polylineColor
                        polyline?.width = polylineWidth
                    }
                    // Park
                    val parkPoints = listOf(
                        LatLng(46.4800, 30.7200),
                        LatLng(46.4800, 30.7300),
                        LatLng(46.4850, 30.7300),
                        LatLng(46.4850, 30.7200)
                    )
                    if (polygon == null) {
                        polygon = googleMap.addPolygon(
                            PolygonOptions()
                                .addAll(parkPoints)
                                .strokeColor(polygonColor)
                                .strokeWidth(polygonWidth)
                                .fillColor(android.graphics.Color.argb(50, 0, 255, 0))
                                .clickable(true)
                        )
                    } else {
                        polygon?.strokeColor = polygonColor
                        polygon?.strokeWidth = polygonWidth
                    }
                    // Polyline click listener
                    googleMap.setOnPolylineClickListener {
                        android.widget.Toast.makeText(
                            context,
                            "Hiking Trail\nLength: give or take like 2 miles, Difficulty: Mid",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Polygon click listener
                    googleMap.setOnPolygonClickListener {
                        android.widget.Toast.makeText(
                            context,
                            "Park Area\nArea: at least 15 feet",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Center the camera on the trail's first point
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(trailPoints.first(), 13f))
                }
            }
        )
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _: androidx.lifecycle.LifecycleOwner, event: Lifecycle.Event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    return mapView
}