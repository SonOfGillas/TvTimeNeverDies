package com.example.tvtimeneverdie.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvtimeneverdie.data.repository.MovieRepository
import com.example.tvtimeneverdie.data.repository.TvShowRepository
import com.example.tvtimeneverdie.di.AppContainer
import com.example.tvtimeneverdie.domain.model.Movie
import com.example.tvtimeneverdie.domain.model.Show
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoadingShows: Boolean = true,
    val shows: List<Show> = emptyList(),
    val showsErrorMessage: String? = null,
    val isLoadingMovies: Boolean = true,
    val movies: List<Movie> = emptyList(),
    val moviesErrorMessage: String? = null,
)

class HomeViewModel(
    private val tvShowRepository: TvShowRepository = AppContainer.tvShowRepository,
    private val movieRepository: MovieRepository = AppContainer.movieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadShows()
        loadMovies()
    }

    fun loadShows() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingShows = true, showsErrorMessage = null) }
            try {
                val shows = tvShowRepository.getRecentShows()
                _uiState.update { it.copy(isLoadingShows = false, shows = shows) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoadingShows = false, showsErrorMessage = e.message ?: "Errore nel caricamento")
                }
            }
        }
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMovies = true, moviesErrorMessage = null) }
            try {
                val movies = movieRepository.getNowPlaying()
                _uiState.update { it.copy(isLoadingMovies = false, movies = movies) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoadingMovies = false, moviesErrorMessage = e.message ?: "Errore nel caricamento")
                }
            }
        }
    }
}
