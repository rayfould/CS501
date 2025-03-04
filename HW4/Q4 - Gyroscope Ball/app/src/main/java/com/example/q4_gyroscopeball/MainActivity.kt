package com.example.q4_gyroscopeball

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.q4_gyroscopeball.ui.theme.Q4GyroscopeBallTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Q4GyroscopeBallTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BallGameScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BallGameScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Ball position
    var ballX by remember { mutableStateOf(100f) }
    var ballY by remember { mutableStateOf(100f) }
    val ballRadius = 20f

    // Gyroscope tilt
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    // Sensor setup
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                tiltX = event.values[1] // Roll
                tiltY = event.values[0] // Pitch
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    // Register sensor
    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorListener, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    // Update ball position
    LaunchedEffect(Unit) {
        while (true) {
            ballX += tiltX * 10f
            ballY += tiltY * 10f

            // Screen boundaries
            val maxX = 500f - ballRadius
            val maxY = 800f - ballRadius
            ballX = ballX.coerceIn(ballRadius, maxX)
            ballY = ballY.coerceIn(ballRadius, maxY)

            // Obstacles
            val obstacle1 = Rectangle(200f, 300f, 300f, 350f) // Horizontal
            val obstacle2 = Rectangle(100f, 500f, 150f, 600f) // Vertical

            // Collision detection
            if (ballX + ballRadius > obstacle1.left && ballX - ballRadius < obstacle1.right &&
                ballY + ballRadius > obstacle1.top && ballY - ballRadius < obstacle1.bottom) {
                ballX -= tiltX * 10f
                ballY -= tiltY * 10f
            }
            if (ballX + ballRadius > obstacle2.left && ballX - ballRadius < obstacle2.right &&
                ballY + ballRadius > obstacle2.top && ballY - ballRadius < obstacle2.bottom) {
                ballX -= tiltX * 10f
                ballY -= tiltY * 10f
            }

            delay(16L)
        }
    }

    // Draw game
    Canvas(modifier = modifier
        .fillMaxSize()
        .background(Color.LightGray)) {
        // Obstacles
        drawRect(
            color = Color.Black,
            topLeft = Offset(200f, 300f),
            size = androidx.compose.ui.geometry.Size(100f, 50f) // Obstacle 1
        )
        drawRect(
            color = Color.Black,
            topLeft = Offset(100f, 500f),
            size = androidx.compose.ui.geometry.Size(50f, 100f) // Obstacle 2
        )

        // Ball
        drawCircle(
            color = Color.Red,
            radius = ballRadius,
            center = Offset(ballX, ballY)
        )
    }
}

// Rectangle for collision simulation
data class Rectangle(val left: Float, val top: Float, val right: Float, val bottom: Float)

@Preview(showBackground = true)
@Composable
fun BallGamePreview() {
    Q4GyroscopeBallTheme {
        BallGameScreen()
    }
}