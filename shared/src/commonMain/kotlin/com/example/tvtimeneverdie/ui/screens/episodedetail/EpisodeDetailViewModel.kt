package com.example.tvtimeneverdie.ui.screens.episodedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvtimeneverdie.data.repository.CommentsRepository
import com.example.tvtimeneverdie.data.repository.TvShowRepository
import com.example.tvtimeneverdie.di.AppContainer
import com.example.tvtimeneverdie.domain.model.Episode
import com.example.tvtimeneverdie.domain.model.EpisodeComment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EpisodeDetailUiState(
    val isLoading: Boolean = true,
    val episode: Episode? = null,
    val comments: List<EpisodeComment> = emptyList(),
    val commentDraft: String = "",
    val errorMessage: String? = null,
)

class EpisodeDetailViewModel(
    private val episodeId: Int,
    private val showId: Int,
    private val uid: String,
    private val displayName: String,
    private val tvShowRepository: TvShowRepository = AppContainer.tvShowRepository,
    private val commentsRepository: CommentsRepository = AppContainer.commentsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpisodeDetailUiState())
    val uiState: StateFlow<EpisodeDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val episode = tvShowRepository.getEpisodes(showId).firstOrNull { it.id == episodeId }
                _uiState.update { it.copy(isLoading = false, episode = episode) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Errore nel caricamento dell'episodio")
                }
            }
        }
        viewModelScope.launch {
            commentsRepository.comments(episodeId)
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Errore nel caricamento dei commenti") }
                }
                .collect { comments -> _uiState.update { it.copy(comments = comments) } }
        }
    }

    fun onCommentTextChange(value: String) {
        _uiState.update { it.copy(commentDraft = value) }
    }

    fun submitComment() {
        val text = _uiState.value.commentDraft.trim()
        if (text.isEmpty()) return
        _uiState.update { it.copy(commentDraft = "") }
        viewModelScope.launch {
            val name = displayName.ifBlank { "Utente" }
            commentsRepository.addComment(episodeId, uid, name, text)
        }
    }
}
