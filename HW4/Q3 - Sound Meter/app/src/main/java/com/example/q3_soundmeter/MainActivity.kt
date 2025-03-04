package com.example.q3_soundmeter

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.q3_soundmeter.ui.theme.Q3SoundMeterTheme
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request microphone permission
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            setContent {
                Q3SoundMeterTheme {
                    if (isGranted) {
                        SoundMeterScreen()
                    } else {
                        Text(
                            text = "Microphone permission needed!",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        )
                    }
                }
            }
        }
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}

@SuppressLint("MissingPermission")
@Composable
fun SoundMeterScreen() {
    // State for decibel level
    var decibelLevel by remember { mutableStateOf(0f) }

    // AudioRecord setup
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    val audioRecord = remember {
        AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )
    }

    val audioBuffer = ShortArray(bufferSize / 2)

    // Start recording audio with a non-blocking loop
    LaunchedEffect(Unit) {
        if (bufferSize > 0) { // Check if buffer size is valid
            audioRecord.startRecording()
            while (true) {
                val readSize = audioRecord.read(audioBuffer, 0, audioBuffer.size)
                if (readSize > 0) {
                    // Calculate amplitude (RMS)
                    var sum = 0.0
                    for (i in 0 until readSize) {
                        sum += audioBuffer[i] * audioBuffer[i]
                    }
                    val amp = Math.sqrt(sum / readSize).toFloat()
                    val max = 32767f // Max value for 16-bit PCM
                    decibelLevel = (20 * log10(amp / max) + 60f).coerceAtLeast(0f)
                }
                delay(100L) // Short delay to avoid blocking UI
            }
        }
    }

    // Stop mic after closing
    DisposableEffect(Unit) {
        onDispose {
            audioRecord.stop()
            audioRecord.release()
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Sound Meter",
            fontSize = 28.sp,
            color = Color.Blue,
            modifier = Modifier.padding(16.dp)
        )

        // Decibels
        Text(
            text = "Noise Level: ${decibelLevel.roundToInt()} dB",
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )

        // Progress bar
        val progress = (decibelLevel / 120f).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .width(200.dp)
                .height(20.dp)
                .padding(16.dp),
            color = when {
                decibelLevel > 85f -> Color.Red
                decibelLevel > 60f -> Color.Yellow
                else -> Color.Green
            }
        )

        // Alert if above 85db
        if (decibelLevel > 85f) {
            Text(
                text = "Too Loud!",
                fontSize = 20.sp,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SoundMeterPreview() {
    Q3SoundMeterTheme {
        SoundMeterScreen()
    }
}