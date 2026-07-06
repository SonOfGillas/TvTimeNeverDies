package com.example.tvtimeneverdie.ui.screens.showdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvtimeneverdie.data.repository.TvShowRepository
import com.example.tvtimeneverdie.data.repository.UserShowsRepository
import com.example.tvtimeneverdie.di.AppContainer
import com.example.tvtimeneverdie.domain.model.Episode
import com.example.tvtimeneverdie.domain.model.Show
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShowDetailUiState(
    val isLoading: Boolean = true,
    val show: Show? = null,
    val episodesBySeason: Map<Int, List<Episode>> = emptyMap(),
    val watchedEpisodeIds: Set<Int> = emptySet(),
    val isInWatchlist: Boolean = false,
    val errorMessage: String? = null,
)

class ShowDetailViewModel(
    private val showId: Int,
    private val uid: String,
    private val tvShowRepository: TvShowRepository = AppContainer.tvShowRepository,
    private val userShowsRepository: UserShowsRepository = AppContainer.userShowsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowDetailUiState())
    val uiState: StateFlow<ShowDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val show = tvShowRepository.getShow(showId)
                val episodes = tvShowRepository.getEpisodes(showId)
                val bySeason = episodes.groupBy { it.season }
                    .toList()
                    .sortedBy { it.first }
                    .toMap()
                _uiState.update { it.copy(isLoading = false, show = show, episodesBySeason = bySeason) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Errore nel caricamento della serie")
                }
            }
        }
        viewModelScope.launch {
            userShowsRepository.allWatchedEpisodes(uid)
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message ?: "Errore Firestore") } }
                .collect { entries ->
                    val ids = entries.filter { it.showId == showId }.map { it.episodeId }.toSet()
                    _uiState.update { it.copy(watchedEpisodeIds = ids) }
                }
        }
        viewModelScope.launch {
            userShowsRepository.watchlistIds(uid)
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message ?: "Errore Firestore") } }
                .collect { ids -> _uiState.update { it.copy(isInWatchlist = showId in ids) } }
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            if (_uiState.value.isInWatchlist) {
                userShowsRepository.removeFromWatchlist(uid, showId)
            } else {
                userShowsRepository.addToWatchlist(uid, showId)
            }
        }
    }

    fun toggleEpisodeWatched(episode: Episode) {
        viewModelScope.launch {
            val isWatched = episode.id in _uiState.value.watchedEpisodeIds
            userShowsRepository.setEpisodeWatched(
                uid = uid,
                showId = showId,
                episodeId = episode.id,
                season = episode.season,
                number = episode.number,
                watched = !isWatched,
            )
        }
    }

    fun toggleSeasonWatched(season: Int) {
        viewModelScope.launch {
            val episodes = _uiState.value.episodesBySeason[season].orEmpty()
            val allWatched = episodes.isNotEmpty() && episodes.all { it.id in _uiState.value.watchedEpisodeIds }
            userShowsRepository.setSeasonWatched(uid, showId, episodes, watched = !allWatched)
        }
    }
}
