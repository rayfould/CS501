package com.example.numbergrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.numbergrid.ui.theme.NumberGridTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NumberGridTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Launch the infinite scrolling number grid screen.
                    NumberGridScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NumberGridScreen(modifier: Modifier = Modifier) {
    var numbers = remember { mutableStateListOf<Int>().apply { addAll(1..30) } }
    val gridState = rememberLazyGridState()

    // This block monitors the grid's scroll state.
    // When the user scrolls to the bottom, it adds 30 new numbers.
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { lastIndex ->
                if (lastIndex >= numbers.size - 1) {
                    delay(300L)
                    val start = numbers.last() + 1
                    numbers.addAll((start until start + 30).toList())
                }
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Button(
            onClick = {
                numbers.clear()
                numbers.addAll(1..30)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Reset")
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(numbers) { number ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = number.toString())
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
    NumberGridTheme {
        Greeting("Android")
    }
}
