package com.example.tvtimeneverdie.data.gdpr

/** Estrae dallo zip solo i file di testo il cui nome (senza percorso) e' in [wantedNames]. */
expect object ZipExtractor {
    fun extractTextFiles(zipBytes: ByteArray, wantedNames: Set<String>): Map<String, String>
}
