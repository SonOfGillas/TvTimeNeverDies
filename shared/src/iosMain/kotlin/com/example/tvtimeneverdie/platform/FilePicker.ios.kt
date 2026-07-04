package com.example.tvtimeneverdie.platform

import androidx.compose.runtime.Composable

/** Selezione file non ancora implementata su iOS (richiede UIDocumentPickerViewController su Mac). */
@Composable
actual fun rememberZipFilePicker(onResult: (ByteArray?) -> Unit): () -> Unit = { onResult(null) }
