package com.example.mrolympias

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
import androidx.compose.ui.tooling.preview.Preview
import com.example.mrolympias.ui.theme.MrOlympiasTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MrOlympiasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        MrOlympiasGallery()
                    }
                }
            }
        }
    }
}

@Composable
fun MrOlympiasGallery() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Mr. Olympias",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.photo1),
                    contentDescription = "Ronnie Coleman",
                    modifier = Modifier.size(100.dp)
                )
                Text(text = "Ronnie Coleman")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.photo2),
                    contentDescription = "Jay Cutler",
                    modifier = Modifier.size(100.dp)
                )
                Text(text = "Jay Cutler")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.photo3),
                    contentDescription = "Arnold",
                    modifier = Modifier.size(100.dp)
                )
                Text(text = "Arnold")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.photo4),
                    contentDescription = "Dorian Yates",
                    modifier = Modifier.size(100.dp)
                )
                Text(text = "Dorian Yates")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.photo5),
                    contentDescription = "Frank Zane",
                    modifier = Modifier.size(100.dp)
                )
                Text(text = "Frank Zane")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.photo6),
                    contentDescription = "Larry Scott",
                    modifier = Modifier.size(100.dp)
                )
                Text(text = "Larry Scott")
            }
        }
    }
}