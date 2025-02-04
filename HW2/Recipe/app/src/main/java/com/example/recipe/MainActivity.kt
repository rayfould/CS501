package com.example.recipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.recipe.ui.theme.RecipeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Use the innerPadding provided by the Scaffold.
                    Column(modifier = Modifier.padding(innerPadding)) {
                        RecipeCard()
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Recipe Title
            Text(
                text = "Adjarian Khachapuri",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recipe Image inside a Box for potential layering or alignment adjustments
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.adjarian_khachapuri),
                    contentDescription = "Adjarian Khachapuri",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Ingredients section using a Row and Column
            Text(
                text = "Ingredients:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "• 500g bread dough")
                    Text(text = "• 300g Georgian cheese blend (Imeruli and Sulguni)")
                    Text(text = "• 1 egg (plus extra for topping)")
                    Text(text = "• 2 tbsp butter")
                    Text(text = "• Fresh herbs (optional)")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Cooking Instructions
            Text(
                text = "Instructions:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "1. Preheat your oven to 230°C (450°F).\n" +
                        "2. Divide the dough into two pieces and shape them into ovals.\n" +
                        "3. Roll each oval slightly to form a boat-like shape with raised edges.\n" +
                        "4. Fill the center with the cheese blend and crack an egg into each.\n" +
                        "5. Bake for 12-15 minutes until the dough is golden and the egg white is set.\n" +
                        "6. Drizzle with melted butter and garnish with fresh herbs if desired.\n" +
                        "7. Serve hot and enjoy!",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

