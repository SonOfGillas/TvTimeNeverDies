package com.example.tvtimeneverdie.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tvtimeneverdie.ui.components.MediaType
import com.example.tvtimeneverdie.ui.components.MediaTypeTabRow
import com.example.tvtimeneverdie.ui.components.MovieGridItem
import com.example.tvtimeneverdie.ui.components.ShowGridItem
import com.example.tvtimeneverdie.ui.rememberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onShowClick: (Int) -> Unit, onMovieClick: (Int) -> Unit) {
    val viewModel = rememberViewModel { HomeViewModel() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(MediaType.SERIES) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Piu' recenti") }) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MediaTypeTabRow(selected = selectedTab, onSelect = { selectedTab = it })
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    MediaType.SERIES -> when {
                        state.isLoadingShows -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        state.showsErrorMessage != null -> Text(
                            text = state.showsErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        )
                        else -> LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(state.shows, key = { it.id }) { show ->
                                ShowGridItem(
                                    show = show,
                                    onClick = { onShowClick(show.id) },
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    }
                    MediaType.MOVIES -> when {
                        state.isLoadingMovies -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        state.moviesErrorMessage != null -> Text(
                            text = state.moviesErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        )
                        else -> LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(state.movies, key = { it.id }) { movie ->
                                MovieGridItem(
                                    movie = movie,
                                    onClick = { onMovieClick(movie.id) },
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
