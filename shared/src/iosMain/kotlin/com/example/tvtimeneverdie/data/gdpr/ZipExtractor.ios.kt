package com.example.tvtimeneverdie.data.gdpr

actual object ZipExtractor {
    actual fun extractTextFiles(zipBytes: ByteArray, wantedNames: Set<String>): Map<String, String> {
        throw NotImplementedError(
            "Import GDPR non ancora implementato su iOS: richiede una libreria di decompressione zip nativa su Mac.",
        )
    }
}
