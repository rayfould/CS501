package com.example.shoppingcart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shoppingcart.ui.theme.ShoppingCartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingCartTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShoppingCartScreen(
                        modifier = Modifier.padding(innerPadding).padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingCartScreen(modifier: Modifier = Modifier) {
    val showSnackbar = remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = "Shopping Cart", modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Apples")
                Text(text = "$0.99 each")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Qty: 4")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Bread")
                Text(text = "$2.50 each")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Qty: 1")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Steak")
                Text(text = "$15.00 each")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Qty: 2")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Ultra rare, clear, minimal wear, autographed")
                Text(text = "Holographic Charizard Card")
                Text(text = "$39,999.00 each")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Qty: 1")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showSnackbar.value = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Checkout")
        }

        if (showSnackbar.value) {
            Text(text = "Ordered", modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShoppingCartPreview() {
    ShoppingCartTheme {
        ShoppingCartScreen()
    }
}
