package com.example.tvtimeneverdie.ui.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tvtimeneverdie.data.repository.MovieRepository
import com.example.tvtimeneverdie.data.repository.TvShowRepository
import com.example.tvtimeneverdie.di.AppContainer
import com.example.tvtimeneverdie.domain.model.Movie
import com.example.tvtimeneverdie.domain.model.Show
import com.example.tvtimeneverdie.ui.components.MovieListRow
import com.example.tvtimeneverdie.ui.components.ShowListRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualSeriesMatchDialog(
    initialQuery: String,
    onDismiss: () -> Unit,
    onSelected: (Show) -> Unit,
    tvShowRepository: TvShowRepository = AppContainer.tvShowRepository,
) {
    var query by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<Show>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun search() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                results = tvShowRepository.searchShows(query)
                hasSearched = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore nella ricerca"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { search() }

    ManualMatchDialogScaffold(
        title = "Cerca serie",
        query = query,
        onQueryChange = { query = it },
        placeholder = "Titolo della serie",
        onSearch = ::search,
        onDismiss = onDismiss,
        isLoading = isLoading,
        errorMessage = errorMessage,
        hasSearched = hasSearched,
        isEmpty = results.isEmpty(),
    ) {
        items(results, key = { it.id }) { show ->
            ShowListRow(show = show, onClick = { onSelected(show); onDismiss() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualMovieMatchDialog(
    initialQuery: String,
    onDismiss: () -> Unit,
    onSelected: (Movie) -> Unit,
    movieRepository: MovieRepository = AppContainer.movieRepository,
) {
    var query by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun search() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                results = movieRepository.searchMovies(query)
                hasSearched = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore nella ricerca"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { search() }

    ManualMatchDialogScaffold(
        title = "Cerca film",
        query = query,
        onQueryChange = { query = it },
        placeholder = "Titolo del film",
        onSearch = ::search,
        onDismiss = onDismiss,
        isLoading = isLoading,
        errorMessage = errorMessage,
        hasSearched = hasSearched,
        isEmpty = results.isEmpty(),
    ) {
        items(results, key = { it.id }) { movie ->
            MovieListRow(movie = movie, onClick = { onSelected(movie); onDismiss() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualMatchDialogScaffold(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onSearch: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    hasSearched: Boolean,
    isEmpty: Boolean,
    resultsContent: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(title) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = "Chiudi")
                            }
                        },
                    )
                },
            ) { padding ->
                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        label = { Text(placeholder) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        trailingIcon = {
                            IconButton(onClick = onSearch) {
                                Icon(Icons.Filled.Search, contentDescription = "Cerca")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            errorMessage != null -> Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            )
                            hasSearched && isEmpty -> Text(
                                text = "Nessun risultato",
                                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            )
                            else -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                                resultsContent()
                            }
                        }
                    }
                }
            }
        }
    }
}
