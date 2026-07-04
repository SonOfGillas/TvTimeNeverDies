package com.example.tvtimeneverdie.ui.screens.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvtimeneverdie.data.repository.MovieRepository
import com.example.tvtimeneverdie.data.repository.UserMoviesRepository
import com.example.tvtimeneverdie.di.AppContainer
import com.example.tvtimeneverdie.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val movie: Movie? = null,
    val isInWatchlist: Boolean = false,
    val isWatched: Boolean = false,
    val errorMessage: String? = null,
)

class MovieDetailViewModel(
    private val movieId: Int,
    private val uid: String,
    private val movieRepository: MovieRepository = AppContainer.movieRepository,
    private val userMoviesRepository: UserMoviesRepository = AppContainer.userMoviesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val movie = movieRepository.getMovie(movieId)
                _uiState.update { it.copy(isLoading = false, movie = movie) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Errore nel caricamento del film")
                }
            }
        }
        viewModelScope.launch {
            userMoviesRepository.watchlistIds(uid)
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message ?: "Errore Firestore") } }
                .collect { ids -> _uiState.update { it.copy(isInWatchlist = movieId in ids) } }
        }
        viewModelScope.launch {
            userMoviesRepository.watchedIds(uid)
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message ?: "Errore Firestore") } }
                .collect { ids -> _uiState.update { it.copy(isWatched = movieId in ids) } }
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            if (_uiState.value.isInWatchlist) {
                userMoviesRepository.removeFromWatchlist(uid, movieId)
            } else {
                userMoviesRepository.addToWatchlist(uid, movieId)
            }
        }
    }

    fun toggleWatched() {
        viewModelScope.launch {
            if (_uiState.value.isWatched) {
                userMoviesRepository.unmarkWatched(uid, movieId)
            } else {
                userMoviesRepository.markWatched(uid, movieId)
            }
        }
    }
}
