package com.example.tvtimeneverdie.data.gdpr

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

actual object ZipExtractor {
    actual fun extractTextFiles(zipBytes: ByteArray, wantedNames: Set<String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val baseName = entry.name.substringAfterLast('/')
                if (!entry.isDirectory && baseName in wantedNames) {
                    result[baseName] = zip.readBytes().toString(Charsets.UTF_8)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return result
    }
}
