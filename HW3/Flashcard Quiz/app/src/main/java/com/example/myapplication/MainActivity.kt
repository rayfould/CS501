package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                    FlashcardQuiz(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}

data class Flashcard(val question: String, val answer: String)

fun parseFlashcards(context: Context): List<Flashcard> {
    val flashcards = mutableListOf<Flashcard>()
    val parser = context.resources.getXml(R.xml.flashcards)
    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == "flashcard") {
            val question = parser.getAttributeValue(null, "question")
            val answer = parser.getAttributeValue(null, "answer")
            if (question != null && answer != null) {
                flashcards.add(Flashcard(question, answer))
            }
        }
        eventType = parser.next()
    }
    return flashcards
}

@Composable
fun FlashcardQuiz(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var flashcards by remember { mutableStateOf(parseFlashcards(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(15000L)
            flashcards = flashcards.shuffled()
        }
    }

    LazyRow(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(flashcards) { flashcard ->
            FlashcardItem(flashcard = flashcard)
        }
    }
}

@Composable
fun FlashcardItem(flashcard: Flashcard) {

    var flipped by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(width = 300.dp, height = 200.dp)
            .clickable { flipped = !flipped },
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = flipped) { isFlipped ->
            if (isFlipped) {
                Text(text = flashcard.answer)
            } else {
                Text(text = flashcard.question)
            }
        }
    }
}