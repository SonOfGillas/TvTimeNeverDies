package com.example.tvtimeneverdie.platform

import androidx.compose.runtime.Composable

/** Ritorna una funzione che avvia la selezione di un file zip; [onResult] riceve i byte letti o null se annullato/fallito. */
@Composable
expect fun rememberZipFilePicker(onResult: (ByteArray?) -> Unit): () -> Unit
