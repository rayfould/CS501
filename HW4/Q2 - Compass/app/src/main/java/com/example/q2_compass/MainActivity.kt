package com.example.q2_compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.q2_compass.ui.theme.Q2CompassTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Q2CompassTheme {
                CompassLevelScreen()
            }
        }
    }
}

@Composable
fun CompassLevelScreen() {
    val context = LocalContext.current

    // State variables to store sensor values
    var compassAngle by remember { mutableStateOf(0f) }
    var rollAngle by remember { mutableStateOf(0f) }
    var pitchAngle by remember { mutableStateOf(0f) }

    // Setup sensor manager
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Sensor data
    val accelerometer = FloatArray(3)
    val magnetometer = FloatArray(3)

    // Rotation matrix for some fancy stuff later on
    val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)

    // Sensor listener for changes of the sensor data
    val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    // Accelerometer
                    System.arraycopy(event.values, 0, accelerometer, 0, accelerometer.size)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    // Magnometer
                    System.arraycopy(event.values, 0, magnetometer, 0, magnetometer.size)
                }
                Sensor.TYPE_GYROSCOPE -> {
                    // Hyroscope for roll \ pitch
                    rollAngle = event.values[1] * 180 / Math.PI.toFloat()
                    pitchAngle = event.values[0] * 180 / Math.PI.toFloat()
                }
            }

            // Calculate compass angle
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, magnetometer)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            compassAngle = (orientationAngles[0] * 180 / Math.PI.toFloat() + 360) % 360
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    DisposableEffect(Unit) {
        // Set up sensors
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorListener, gyroscope, SensorManager.SENSOR_DELAY_UI)

        // Dispose sensors
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Compass
        Text(
            text = "Compass Adventure",
            fontSize = 24.sp,
            color = Color.Black,
            modifier = Modifier.padding(16.dp)
        )

        Canvas(modifier = Modifier.size(200.dp)) {
            drawCircle(
                color = Color.Gray,
                radius = size.width / 2
            )

            // Compass pointy thingy
            rotate(-compassAngle, pivot = center) {
                drawLine(
                    color = Color.Red,
                    start = center,
                    end = Offset(center.x, 0f),
                    strokeWidth = 8f
                )
            }
        }

        Text(
            text = "Heading: ${compassAngle.roundToInt()}°",
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )

        // Level
        Text(
            text = "Bubble Level",
            fontSize = 24.sp,
            color = Color.Black,
            modifier = Modifier.padding(16.dp)
        )

        Box(
            modifier = Modifier
                .size(200.dp, 100.dp)
                .background(Color.LightGray)
        ) {
            // Draw good ol lil bubble level
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bubbleX = size.width / 2 + (rollAngle * 2).coerceIn(-size.width / 2 + 20, size.width / 2 - 20)
                val bubbleY = size.height / 2 + (pitchAngle * 2).coerceIn(-size.height / 2 + 20, size.height / 2 - 20)

                drawCircle(
                    color = Color.Red,
                    radius = 20f,
                    center = Offset(bubbleX, bubbleY)
                )
            }
        }

        Text(
            text = "Roll: ${rollAngle.roundToInt()}°\nPitch: ${pitchAngle.roundToInt()}°",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CompassLevelPreview() {
    Q2CompassTheme {
        CompassLevelScreen()
    }
}