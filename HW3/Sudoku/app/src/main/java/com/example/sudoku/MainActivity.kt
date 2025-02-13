package com.example.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SudokuTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Sudoku(modifier = Modifier.padding(innerPadding))
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
    SudokuTheme {
        Greeting("Android")
    }
}

@Composable
fun Sudoku(modifier: Modifier) {
    var grid by remember { mutableStateOf(Array(9) { IntArray(9) }) }
    var selectedRow by remember { mutableStateOf(-1) }
    var selectedCol by remember { mutableStateOf(-1) }
    var inputNumber by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Initialize first row with random numbers
    LaunchedEffect(Unit) {
        val numbers = (1..9).shuffled().toList()
        for (i in 0 until 9) {
            grid = grid.map { it.copyOf() }.toTypedArray()
            grid[0][i] = numbers[i]
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(9),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(81) { index ->
                            val row = index / 9
                            val col = index % 9

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (row == 0) Color.LightGray else Color.White)
                                    .clickable(
                                        enabled = row != 0 && grid[row][col] == 0
                                    ) {
                                        selectedRow = row
                                        selectedCol = col
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (grid[row][col] != 0) {
                                    Text(grid[row][col].toString())
                                }
                            }
                        }
                    }

                    // Button to reset
                    Button(onClick = {
                        grid = Array(9) { IntArray(9) }
                        val numbers = (1..9).shuffled().toList()
                        for (i in 0 until 9) {
                            grid = grid.map { it.copyOf() }.toTypedArray()
                            grid[0][i] = numbers[i]
                        }
                    }) {
                        Text("Reset")
                    }

                    // Dialog for input
                    if (selectedRow != -1 && selectedCol != -1) {
                        AlertDialog(
                            onDismissRequest = {
                                selectedRow = -1
                                selectedCol = -1
                            },
                            title = { Text("Enter number (1-9)") },
                            text = {
                                TextField(
                                    value = inputNumber,
                                    onValueChange = { inputNumber = it },
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                            },
                            confirmButton = {
                                Button(onClick = {
                                    val number = inputNumber.toIntOrNull()
                                    if (number != null && number in 1..9) {
                                        grid = grid.map { it.copyOf() }.toTypedArray()
                                        grid[selectedRow][selectedCol] = number
                                        if (isGameWon(grid)) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("You won!")
                                            }
                                        }
                                    }
                                    selectedRow = -1
                                    selectedCol = -1
                                    inputNumber = ""
                                }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}

//Win checking condition functioon
private fun isGameWon(grid: Array<IntArray>): Boolean {
    if (grid.any { it.contains(0) }) return false

    //Convert to hash set, check for all numbers
    for (row in grid) {
        val numbers = row.toSet()
        if (numbers.size != 9 || numbers.contains(0)) return false
    }

    for (col in 0 until 9) {
        val column = (0 until 9).map { grid[it][col] }.toSet()
        if (column.size != 9 || column.contains(0)) return false
    }
    // Check 3x3 blocks
    for (blockRow in 0 until 3) {
        for (blockCol in 0 until 3) {
            val block = mutableSetOf<Int>()
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    block.add(grid[blockRow * 3 + i][blockCol * 3 + j])
                }
            }
            if (block.size != 9 || block.contains(0)) return false
        }
    }

    return true
}