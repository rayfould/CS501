package com.example.q1_recipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.q1_recipe.ui.theme.Q1RecipeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi


interface RecipeApiService {
    @GET("search.php")
    suspend fun searchRecipes(@Query("s") query: String): RecipeResponse
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Q1RecipeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RecipeSearchScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}



object RetrofitInstance {
    // Base URL for The Meal DB API
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    private val moshi = Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    val api: RecipeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RecipeApiService::class.java)
    }
}

// Data class for JSON response
data class RecipeResponse(
    @Json(name = "meals") val meals: List<Meal>?
)

// Meal class setup
data class Meal(
    @Json(name = "idMeal") val idMeal: String,
    @Json(name = "strMeal") val name: String,
    @Json(name = "strMealThumb") val thumbnail: String,
    @Json(name = "strInstructions") val instructions: String?
)


sealed class RecipeUiState {
    object Loading : RecipeUiState()
    data class Success(val recipes: List<Meal>) : RecipeUiState()
    data class Error(val message: String) : RecipeUiState()
}

class RecipeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Success(emptyList()))
    val uiState: StateFlow<RecipeUiState> = _uiState

    fun searchRecipes(query: String) {
        // Loading indicator
        _uiState.value = RecipeUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchRecipes(query)
                val meals = response.meals ?: emptyList()
                _uiState.value = RecipeUiState.Success(meals)
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error("Error fetching recipes: ${e.message}")
            }
        }
    }
}

@Composable
fun RecipeSearchScreen(
    modifier: Modifier = Modifier,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    val uiState by recipeViewModel.uiState.collectAsState()

    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {
        // Search bar
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Enter recipe search (e.g., chicken)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { recipeViewModel.searchRecipes(query) }) {
                Text("Search")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // UI State handling
        when (uiState) {
            is RecipeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RecipeUiState.Error -> {
                val message = (uiState as RecipeUiState.Error).message
                Text(text = message, fontSize = 18.sp)
            }
            is RecipeUiState.Success -> {
                val recipes = (uiState as RecipeUiState.Success).recipes
                if (recipes.isEmpty()) {
                    Text(text = "No recipes found.", fontSize = 18.sp)
                } else {
                    LazyColumn {
                        items(recipes) { meal ->
                            RecipeItem(meal = meal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(meal: Meal) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        // Image using Coil, if available
        Image(
            painter = rememberAsyncImagePainter(model = meal.thumbnail),
            contentDescription = meal.name,
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = meal.name, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            // Display the description as the first 100 letters
            Text(text = meal.instructions?.take(100)?.plus("...") ?: "No description")
        }
    }
}


