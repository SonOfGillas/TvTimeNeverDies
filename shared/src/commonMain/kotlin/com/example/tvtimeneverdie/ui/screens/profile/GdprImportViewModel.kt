package com.example.tvtimeneverdie.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvtimeneverdie.data.gdpr.GdprImportParser
import com.example.tvtimeneverdie.data.gdpr.ParsedMovie
import com.example.tvtimeneverdie.data.gdpr.ParsedSeries
import com.example.tvtimeneverdie.data.gdpr.ZipExtractor
import com.example.tvtimeneverdie.data.repository.MovieRepository
import com.example.tvtimeneverdie.data.repository.TvShowRepository
import com.example.tvtimeneverdie.data.repository.UserMoviesRepository
import com.example.tvtimeneverdie.data.repository.UserShowsRepository
import com.example.tvtimeneverdie.di.AppContainer
import com.example.tvtimeneverdie.domain.model.Movie
import com.example.tvtimeneverdie.domain.model.Show
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

enum class ImportPhase {
    PICK_FILE,
    PARSING,
    MATCHING,
    REVIEW,
    IMPORTING,
    DONE,
    ERROR,
}

data class SeriesMatch(
    val originalName: String,
    val nbEpisodesSeen: Int,
    val matchedShow: Show?,
    val included: Boolean,
)

data class MovieMatch(
    val originalName: String,
    val matchedMovie: Movie?,
    val included: Boolean,
)

data class GdprImportUiState(
    val phase: ImportPhase = ImportPhase.PICK_FILE,
    val matchProgress: Int = 0,
    val matchTotal: Int = 0,
    val seriesMatches: List<SeriesMatch> = emptyList(),
    val movieMatches: List<MovieMatch> = emptyList(),
    val importProgress: Int = 0,
    val importTotal: Int = 0,
    val importedCount: Int = 0,
    val errorMessage: String? = null,
)

private const val MATCH_CONCURRENCY = 4

class GdprImportViewModel(
    private val uid: String,
    private val tvShowRepository: TvShowRepository = AppContainer.tvShowRepository,
    private val movieRepository: MovieRepository = AppContainer.movieRepository,
    private val userShowsRepository: UserShowsRepository = AppContainer.userShowsRepository,
    private val userMoviesRepository: UserMoviesRepository = AppContainer.userMoviesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GdprImportUiState())
    val uiState: StateFlow<GdprImportUiState> = _uiState.asStateFlow()

    private val progressMutex = Mutex()

    fun onZipPicked(bytes: ByteArray?) {
        if (bytes == null) {
            _uiState.update { it.copy(errorMessage = "Selezione annullata") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(phase = ImportPhase.PARSING, errorMessage = null) }
            try {
                val files = ZipExtractor.extractTextFiles(bytes, GdprImportParser.WANTED_FILE_NAMES)
                val parsedSeries = GdprImportParser.parseSeries(files)
                val parsedMovies = GdprImportParser.parseMovies(files)
                matchAndReview(parsedSeries, parsedMovies)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(phase = ImportPhase.ERROR, errorMessage = e.message ?: "Errore durante la lettura dello zip")
                }
            }
        }
    }

    private suspend fun matchAndReview(parsedSeries: List<ParsedSeries>, parsedMovies: List<ParsedMovie>) {
        val total = parsedSeries.size + parsedMovies.size
        _uiState.update { it.copy(phase = ImportPhase.MATCHING, matchProgress = 0, matchTotal = total) }

        val seriesMatches = parsedSeries.mapConcurrently(MATCH_CONCURRENCY) { parsed ->
            val matchedShow = runCatching { tvShowRepository.findBestMatch(parsed.name) }.getOrNull()
            incrementProgress()
            SeriesMatch(parsed.name, parsed.nbEpisodesSeen, matchedShow, included = matchedShow != null)
        }
        val movieMatches = parsedMovies.mapConcurrently(MATCH_CONCURRENCY) { parsed ->
            val matchedMovie = runCatching { movieRepository.searchMovies(parsed.name).firstOrNull() }.getOrNull()
            incrementProgress()
            MovieMatch(parsed.name, matchedMovie, included = matchedMovie != null)
        }

        _uiState.update {
            it.copy(phase = ImportPhase.REVIEW, seriesMatches = seriesMatches, movieMatches = movieMatches)
        }
    }

    private suspend fun incrementProgress() {
        progressMutex.withLock {
            _uiState.update { it.copy(matchProgress = it.matchProgress + 1) }
        }
    }

    private suspend fun incrementImportProgress() {
        progressMutex.withLock {
            _uiState.update { it.copy(importProgress = it.importProgress + 1) }
        }
    }

    fun toggleSeriesIncluded(index: Int) {
        _uiState.update { state ->
            val updated = state.seriesMatches.toMutableList()
            val current = updated[index]
            if (current.matchedShow != null) {
                updated[index] = current.copy(included = !current.included)
            }
            state.copy(seriesMatches = updated)
        }
    }

    fun toggleMovieIncluded(index: Int) {
        _uiState.update { state ->
            val updated = state.movieMatches.toMutableList()
            val current = updated[index]
            if (current.matchedMovie != null) {
                updated[index] = current.copy(included = !current.included)
            }
            state.copy(movieMatches = updated)
        }
    }

    fun setSeriesManualMatch(index: Int, show: Show) {
        _uiState.update { state ->
            val updated = state.seriesMatches.toMutableList()
            updated[index] = updated[index].copy(matchedShow = show, included = true)
            state.copy(seriesMatches = updated)
        }
    }

    fun setMovieManualMatch(index: Int, movie: Movie) {
        _uiState.update { state ->
            val updated = state.movieMatches.toMutableList()
            updated[index] = updated[index].copy(matchedMovie = movie, included = true)
            state.copy(movieMatches = updated)
        }
    }

    fun confirmImport() {
        viewModelScope.launch {
            val seriesToImport = _uiState.value.seriesMatches.filter { it.included && it.matchedShow != null }
            val moviesToImport = _uiState.value.movieMatches.filter { it.included && it.matchedMovie != null }
            val total = seriesToImport.size + moviesToImport.size
            _uiState.update {
                it.copy(phase = ImportPhase.IMPORTING, importProgress = 0, importTotal = total)
            }
            var imported = 0

            for (match in seriesToImport) {
                val show = match.matchedShow ?: continue
                try {
                    if (match.nbEpisodesSeen <= 0) {
                        userShowsRepository.addToWatchlist(uid, show.id)
                    } else {
                        val orderedEpisodes = tvShowRepository.getEpisodes(show.id)
                            .sortedWith(compareBy({ it.season }, { it.number ?: 0 }))
                        orderedEpisodes.take(match.nbEpisodesSeen).forEach { episode ->
                            userShowsRepository.setEpisodeWatched(
                                uid = uid,
                                showId = show.id,
                                episodeId = episode.id,
                                season = episode.season,
                                number = episode.number,
                                watched = true,
                            )
                        }
                    }
                    imported++
                } catch (_: Exception) {
                    // Elemento saltato, si continua con gli altri.
                }
                incrementImportProgress()
            }

            for (match in moviesToImport) {
                val movie = match.matchedMovie ?: continue
                try {
                    userMoviesRepository.markWatched(uid, movie.id)
                    imported++
                } catch (_: Exception) {
                    // Elemento saltato, si continua con gli altri.
                }
                incrementImportProgress()
            }

            _uiState.update { it.copy(phase = ImportPhase.DONE, importedCount = imported) }
        }
    }
}

private suspend fun <T, R> List<T>.mapConcurrently(maxConcurrency: Int, transform: suspend (T) -> R): List<R> {
    val semaphore = Semaphore(maxConcurrency)
    return coroutineScope {
        map { item -> async { semaphore.withPermit { transform(item) } } }.awaitAll()
    }
}
