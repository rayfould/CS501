package com.example.q2_repoviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.q2_repoviewer.ui.theme.Q2RepoViewerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Q2RepoViewerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RepoViewerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

}


data class Repo(
    val name: String,
    val description: String?
)

interface GitHubApiService {
    @GET("users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 30
    ): Response<List<Repo>>
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.github.com/"

    private val moshi = Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    val api: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GitHubApiService::class.java)
    }
}

// UI state
sealed class RepoUiState {
    object Idle : RepoUiState()
    object Loading : RepoUiState()
    data class Success(val repos: List<Repo>, val hasMore: Boolean) : RepoUiState()
    data class Error(val message: String) : RepoUiState()
}


class RepositoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<RepoUiState>(RepoUiState.Idle)
    val uiState: StateFlow<RepoUiState> = _uiState

    // Current query and page
    private var currentQuery: String = ""
    private var currentPage: Int = 1
    private var currentRepos: MutableList<Repo> = mutableListOf()

    fun searchRepos(query: String) {
        // Reset state
        currentQuery = query.trim()
        if (currentQuery.isEmpty()) return
        currentPage = 1
        currentRepos.clear()
        fetchRepos()
    }

    fun loadMore() {
        if (currentQuery.isNotEmpty()) {
            currentPage++
            fetchRepos()
        }
    }

    private fun fetchRepos() {
        _uiState.value = RepoUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getUserRepos(currentQuery, currentPage)
                if (response.isSuccessful) {
                    val repos = response.body() ?: emptyList()
                    currentRepos.addAll(repos)
                    // Check for next page
                    val linkHeader = response.headers()["Link"] ?: ""
                    val hasMore = linkHeader.contains("rel=\"next\"")
                    _uiState.value = RepoUiState.Success(currentRepos.toList(), hasMore)
                } else {
                    _uiState.value = RepoUiState.Error("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = RepoUiState.Error("Exception: ${e.localizedMessage}")
            }
        }
    }
}

@Composable
fun RepoViewerScreen(
    modifier: Modifier = Modifier,
    repositoryViewModel: RepositoryViewModel = viewModel()
) {
    var usernameInput by remember { mutableStateOf("") }
    val uiState by repositoryViewModel.uiState.collectAsState()

    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Input field
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                placeholder = { Text("Enter GitHub username") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Search button
            Button(onClick = { repositoryViewModel.searchRepos(usernameInput) }) {
                Text("Search")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Display different UI states
        when (uiState) {
            is RepoUiState.Idle -> {
                Text(text = "Enter a username and search for repositories.", fontSize = 18.sp)
            }
            is RepoUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RepoUiState.Error -> {
                val message = (uiState as RepoUiState.Error).message
                Text(text = message, fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
            }
            is RepoUiState.Success -> {
                val state = uiState as RepoUiState.Success
                if (state.repos.isEmpty()) {
                    Text(text = "No repositories found.", fontSize = 18.sp)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.repos) { repo ->
                            RepoItem(repo = repo)
                        }
                        // If there are more pages, display a Load More button
                        if (state.hasMore) {
                            item {
                                Button(onClick = { repositoryViewModel.loadMore() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)) {
                                    Text("Load More")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable def for a repo container
@Composable
fun RepoItem(repo: Repo) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Text(text = repo.name, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = repo.description ?: "No description", fontSize = 16.sp)
        Divider(modifier = Modifier.padding(vertical = 4.dp))
    }
}
