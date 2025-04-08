package com.example.q2_diary

import android.content.Context
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
import com.example.q2_diary.ui.theme.Q2DiaryTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// DataStore extension property
val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Q2DiaryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DiaryApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DiaryApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // DataStore key for font size.
    val font_size_key = floatPreferencesKey("font_size")

    // State for font size
    var fontSize by remember { mutableStateOf(16f) }

    // Load font size
    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        fontSize = prefs[font_size_key] ?: 16f
    }

    // Update font size
    fun updateFontSize(newSize: Float) {
        fontSize = newSize
        coroutineScope.launch {
            context.dataStore.edit { settings ->
                settings[font_size_key] = newSize
            }
        }
    }

    // State for the selected date
    var selectedDate by remember { mutableStateOf(Date()) }

    // Date format
    val displayFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val fileFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    // State for the entry text
    var diaryText by remember { mutableStateOf("") }

    // Load the diary entry for the selected date
    LaunchedEffect(selectedDate) {
        diaryText = loadDiaryEntry(context, "diary_${fileFormat.format(selectedDate)}.txt")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display the selected date and  navigation buttons
        Text(text = "Selected Date: ${displayFormat.format(selectedDate)}", fontSize = fontSize.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                // Navigate to the previous day
                val cal = Calendar.getInstance()
                cal.time = selectedDate
                cal.add(Calendar.DAY_OF_YEAR, -1)
                selectedDate = cal.time
            }) {
                Text("Previous Day")
            }
            Button(onClick = {
                // Navigate to the next day
                val cal = Calendar.getInstance()
                cal.time = selectedDate
                cal.add(Calendar.DAY_OF_YEAR, 1)
                selectedDate = cal.time
            }) {
                Text("Next Day")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Text field for writing the diary entry
        TextField(
            value = diaryText,
            onValueChange = { diaryText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Write your entry here...") },
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = fontSize.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Adjust font size, if necessary
        Text(text = "Font Size: ${fontSize.toInt()} sp", fontSize = fontSize.sp)
        Slider(
            value = fontSize,
            onValueChange = { updateFontSize(it) },
            valueRange = 12f..30f,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Button to save the entry to storage
        Button(onClick = {
            val fileName = "diary_${fileFormat.format(selectedDate)}.txt"
            saveDiaryEntry(context, fileName, diaryText)
        }) {
            Text("Save Entry")
        }
    }
}

// Helper function to load a diary entry from storage
fun loadDiaryEntry(context: Context, fileName: String): String {
    return try {
        context.openFileInput(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        ""  // Return an empty string if the file doesn't exist
    }
}

// Helper function to save a diary entry to storage
fun saveDiaryEntry(context: Context, fileName: String, text: String) {
    context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
        it.write(text.toByteArray())
    }
}
