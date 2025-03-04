package com.example.q1_calculatealtitudechanges

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.q1_calculatealtitudechanges.ui.theme.Q1CalculateAltitudeChangesTheme
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Q1CalculateAltitudeChangesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AltimeterUI(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Simulates altitude changes based on pressure readings.
 * The user can move a slider to simulate the pressure actually changing
 * The altitude is displayed , and the background color changes at higher altitudes, as requested.
 */
@Composable
fun AltimeterUI(modifier: Modifier = Modifier) {
    // Sea level
    val P0 = 1013.25f

    // Def pressure for simulated chanes
    var pressure by remember { mutableStateOf(1013.25f) }

    // Compute altitude
    val altitude = calculateAltitude(pressure, P0)

    // Determine background color based on altitude. Higher altitudes = darker background.
    val backgroundColor = altitudeToColor(altitude)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Display the current  pressure
        Text(text = "Pressure: ${"%.2f".format(pressure)} hPa")

        // Display the altitude
        Text(text = "Altitude: ${"%.1f".format(altitude)} m")

        // Slider to simulate pressure
        Column {
            Text("Adjust Pressure")
            Slider(
                value = pressure,
                onValueChange = { newValue -> pressure = newValue },
                valueRange = 800f..1050f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.Black,
                    activeTrackColor = Color.Gray
                )
            )
        }

        // Reset button
        Button(onClick = { pressure = P0 }) {
            Text("Reset")
        }
    }
}

/**
 * Calculates the altitude using the formula
 */
fun calculateAltitude(P: Float, P0: Float): Float {
    return 44330f * (1f - (P / P0).pow(1f / 5.255f))
}

/**
 * Maps altitude to a background color. Higher altitudes return darker colors.
 * This is just a simple linear mapping for demonstration.
 */
fun altitudeToColor(altitude: Float): Color {
    // Altitude will be 10k at max, for demo
    val top = altitude.coerceIn(0f, 10000f)
    val colorScale = (1f - top / 10000f) * 1f
    return Color(colorScale, colorScale, colorScale)
}


