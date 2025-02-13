package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.xmlpull.v1.XmlPullParser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Launch the simplified photo gallery.
                    SimplePhotoGallery(modifier = Modifier.padding(innerPadding))
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

data class Photo(val name: String, val title: String)

fun parsePhotos(context: Context): List<Photo> {
    val photos = mutableListOf<Photo>()
    val parser = context.resources.getXml(R.xml.photos)
    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == "photo") {
            val name = parser.getAttributeValue(null, "name")
            val title = parser.getAttributeValue(null, "title")
            if (name != null && title != null) {
                photos.add(Photo(name, title))
            }
        }
        eventType = parser.next()
    }
    return photos
}

@Composable
fun SimplePhotoGallery(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val photos = remember { parsePhotos(context) }
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    val targetScale = if (selectedPhoto != null) 1f else 0.5f
    val scale by animateFloatAsState(targetValue = targetScale, animationSpec = tween(durationMillis = 500))

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(photos) { photo ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(150.dp)
                        .clickable { selectedPhoto = photo }
                ) {
                    val imageId = context.resources.getIdentifier(photo.name, "drawable", context.packageName)
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = photo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        selectedPhoto?.let { photo ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedPhoto = null },
                contentAlignment = Alignment.Center
            ) {
                val imageId = context.resources.getIdentifier(photo.name, "drawable", context.packageName)
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = photo.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                )
            }
        }
    }
}
