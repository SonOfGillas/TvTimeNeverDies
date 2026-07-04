package com.example.tvtimeneverdie.ui.screens.showdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tvtimeneverdie.domain.model.Episode
import com.example.tvtimeneverdie.ui.rememberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    showId: Int,
    uid: String,
    onBack: () -> Unit,
    onEpisodeClick: (Episode) -> Unit,
) {
    val viewModel = rememberViewModel { ShowDetailViewModel(showId, uid) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.show?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.errorMessage != null -> Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                state.show != null -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    item {
                        ShowHeader(
                            imageUrl = state.show?.imageUrl,
                            summary = state.show?.summary.orEmpty(),
                            genres = state.show?.genres.orEmpty(),
                            status = state.show?.status,
                            network = state.show?.network,
                            rating = state.show?.rating,
                            isInWatchlist = state.isInWatchlist,
                            onToggleWatchlist = viewModel::toggleWatchlist,
                        )
                    }
                    state.episodesBySeason.forEach { (season, episodes) ->
                        item {
                            Text(
                                text = "Stagione $season",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        items(episodes, key = { it.id }) { episode ->
                            EpisodeRow(
                                episode = episode,
                                isWatched = episode.id in state.watchedEpisodeIds,
                                onToggleWatched = { viewModel.toggleEpisodeWatched(episode) },
                                onClick = { onEpisodeClick(episode) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowHeader(
    imageUrl: String?,
    summary: String,
    genres: List<String>,
    status: String?,
    network: String?,
    rating: Double?,
    isInWatchlist: Boolean,
    onToggleWatchlist: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .height(170.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(Modifier.width(16.dp))
            Column {
                listOfNotNull(status, network, rating?.let { "Voto $it" })
                    .forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                if (genres.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(genres.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onToggleWatchlist) {
                    Text(if (isInWatchlist) "Rimuovi da Da vedere" else "Aggiungi a Da vedere")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(summary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    isWatched: Boolean,
    onToggleWatched: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = isWatched, onCheckedChange = { onToggleWatched() })
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        ) {
            val number = episode.number?.let { "Ep. $it" } ?: "Speciale"
            Text("$number - ${episode.name}", style = MaterialTheme.typography.bodyLarge)
            episode.airdate?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
