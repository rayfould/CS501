package com.example.myapplication

/**
 * MainActivity launches the Typing Speed Test app.
 */
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import org.xmlpull.v1.XmlPullParser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TypingSpeedTestScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


data class TypingWord(val value: String)


fun parseTypingWords(context: Context): List<TypingWord> {
    val words = mutableListOf<TypingWord>()
    val parser = context.resources.getXml(R.xml.typingwords)
    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == "word") {
            val value = parser.getAttributeValue(null, "value")
            if (value != null) {
                words.add(TypingWord(value))
            }
        }
        eventType = parser.next()
    }
    return words
}


@Composable
fun TypingSpeedTestScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val wordPool = remember { parseTypingWords(context) }
    val displayedWords = remember { mutableStateListOf<TypingWord>().apply { repeat(5) { add(wordPool.random()) } } }
    var typedText by remember { mutableStateOf("") }
    var correctCount by remember { mutableStateOf(0) }
    var resetTimerTrigger by remember { mutableStateOf(0) }
    val startTime = remember { System.currentTimeMillis() }
    val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000f
    val wpm = if (elapsedSeconds > 0) (correctCount / elapsedSeconds * 60).toInt() else 0

    LaunchedEffect(resetTimerTrigger) {
        delay(5000L)
        for (i in displayedWords.indices) {
            displayedWords[i] = wordPool.random()
        }
    }

    // If the typed word matches one of the displayed words, it is removed,
    // a new word is added, the correct count is incremented,
    // and the timer is reset by updating resetTimerTrigger.
    LaunchedEffect(typedText) {
        val trimmedInput = typedText.trim()
        val match = displayedWords.find { it.value.equals(trimmedInput, ignoreCase = true) }
        if (match != null) {
            displayedWords.remove(match)
            displayedWords.add(wordPool.random())
            correctCount++
            typedText = ""
            resetTimerTrigger++
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(displayedWords) { word ->
                Text(text = word.value, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            }
        }
        OutlinedTextField(
            value = typedText,
            onValueChange = { typedText = it },
            label = { Text("Type a word") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            modifier = Modifier.padding(16.dp)
        )
        Text(text = "WPM: $wpm", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun TypingSpeedTestPreview() {
    MyApplicationTheme {
        TypingSpeedTestScreen()
    }
}
