package com.example.tvtimeneverdie.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tvtimeneverdie.platform.rememberZipFilePicker
import com.example.tvtimeneverdie.ui.rememberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GdprImportDialog(uid: String, onDismiss: () -> Unit) {
    val viewModel = rememberViewModel { GdprImportViewModel(uid) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pickZip = rememberZipFilePicker(onResult = viewModel::onZipPicked)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Carica i tuoi dati di TV Time") },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = "Chiudi")
                            }
                        },
                    )
                },
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    when (state.phase) {
                        ImportPhase.PICK_FILE -> PickFileContent(errorMessage = state.errorMessage, onPick = pickZip)
                        ImportPhase.PARSING -> LoadingContent("Lettura del file in corso...")
                        ImportPhase.MATCHING -> LoadingContent(
                            message = "Confronto con TVmaze/Jikan/TMDB in corso...",
                            progress = state.matchProgress,
                            total = state.matchTotal,
                        )
                        ImportPhase.REVIEW -> ReviewContent(state, viewModel)
                        ImportPhase.IMPORTING -> LoadingContent(
                            message = "Salvataggio in corso...",
                            progress = state.importProgress,
                            total = state.importTotal,
                        )
                        ImportPhase.DONE -> DoneContent(state.importedCount, onDismiss)
                        ImportPhase.ERROR -> ErrorContent(state.errorMessage, onDismiss)
                    }
                }
            }
        }
    }
}

@Composable
private fun PickFileContent(errorMessage: String?, onPick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Seleziona il file gdpr-data.zip scaricato da TV Time (Impostazioni > Richiedi i tuoi dati). " +
                "Importeremo solo l'elenco di serie TV e film visti/da vedere, ignorando tutto il resto.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(16.dp))
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
        }
        Button(onClick = onPick) { Text("Scegli file zip") }
    }
}

@Composable
private fun LoadingContent(message: String, progress: Int = 0, total: Int = 0) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (total > 0) {
            val fraction = progress.toFloat() / total.toFloat()
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Text("$message ($progress/$total)")
        } else {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(message)
        }
    }
}

@Composable
private fun ReviewContent(state: GdprImportUiState, viewModel: GdprImportViewModel) {
    var manualSearchSeriesIndex by remember { mutableStateOf<Int?>(null) }
    var manualSearchMovieIndex by remember { mutableStateOf<Int?>(null) }

    manualSearchSeriesIndex?.let { index ->
        ManualSeriesMatchDialog(
            initialQuery = state.seriesMatches[index].originalName,
            onDismiss = { manualSearchSeriesIndex = null },
            onSelected = { show -> viewModel.setSeriesManualMatch(index, show) },
        )
    }
    manualSearchMovieIndex?.let { index ->
        ManualMovieMatchDialog(
            initialQuery = state.movieMatches[index].originalName,
            onDismiss = { manualSearchMovieIndex = null },
            onSelected = { movie -> viewModel.setMovieManualMatch(index, movie) },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp)) {
            item {
                Text(
                    "Serie TV (${state.seriesMatches.size})",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            itemsIndexed(state.seriesMatches) { index, match ->
                MatchRow(
                    title = match.originalName,
                    subtitle = match.matchedShow?.let { "→ ${it.name}" }
                        ?: "Nessuna corrispondenza — tocca per cercare",
                    checked = match.included,
                    enabled = match.matchedShow != null,
                    onToggle = { viewModel.toggleSeriesIncluded(index) },
                    onRowClick = if (match.matchedShow == null) {
                        { manualSearchSeriesIndex = index }
                    } else {
                        null
                    },
                )
            }
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Film (${state.movieMatches.size})",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            itemsIndexed(state.movieMatches) { index, match ->
                MatchRow(
                    title = match.originalName,
                    subtitle = match.matchedMovie?.let { "→ ${it.title}" }
                        ?: "Nessuna corrispondenza — tocca per cercare",
                    checked = match.included,
                    enabled = match.matchedMovie != null,
                    onToggle = { viewModel.toggleMovieIncluded(index) },
                    onRowClick = if (match.matchedMovie == null) {
                        { manualSearchMovieIndex = index }
                    } else {
                        null
                    },
                )
            }
        }
        HorizontalDivider()
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
            Button(onClick = viewModel::confirmImport) { Text("Importa") }
        }
    }
}

@Composable
private fun MatchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    onRowClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { m -> if (onRowClick != null) m.clickable(onClick = onRowClick) else m }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() }, enabled = enabled)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun DoneContent(importedCount: Int, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Importazione completata: $importedCount elementi aggiunti al tuo account.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text("Chiudi") }
    }
}

@Composable
private fun ErrorContent(errorMessage: String?, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(errorMessage ?: "Errore durante l'importazione", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text("Chiudi") }
    }
}
