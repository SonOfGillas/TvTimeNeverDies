package com.example.tvtimeneverdie.ui.components

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class MediaType {
    SERIES,
    MOVIES,
}

@Composable
fun MediaTypeTabRow(selected: MediaType, onSelect: (MediaType) -> Unit) {
    TabRow(selectedTabIndex = selected.ordinal) {
        Tab(
            selected = selected == MediaType.SERIES,
            onClick = { onSelect(MediaType.SERIES) },
            text = { Text("Serie TV") },
        )
        Tab(
            selected = selected == MediaType.MOVIES,
            onClick = { onSelect(MediaType.MOVIES) },
            text = { Text("Film") },
        )
    }
}
