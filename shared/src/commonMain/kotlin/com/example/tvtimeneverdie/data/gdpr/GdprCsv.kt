package com.example.tvtimeneverdie.data.gdpr

/** Parser CSV minimale (RFC4180): gestisce campi tra virgolette con virgole/virgolette-escaped/newline interni. */
object GdprCsv {

    fun parseRows(content: String): List<Map<String, String>> {
        val lines = splitCsvRecords(content)
        if (lines.isEmpty()) return emptyList()
        val header = parseLine(lines.first())
        return lines.drop(1).mapNotNull { line ->
            if (line.isBlank()) return@mapNotNull null
            val values = parseLine(line)
            header.indices.associate { i -> header[i] to values.getOrElse(i) { "" } }
        }
    }

    private fun splitCsvRecords(content: String): List<String> {
        val records = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < content.length) {
            val c = content[i]
            when {
                c == '"' -> {
                    inQuotes = !inQuotes
                    current.append(c)
                }
                c == '\n' && !inQuotes -> {
                    records.add(current.toString())
                    current.clear()
                }
                c == '\r' -> Unit
                else -> current.append(c)
            }
            i++
        }
        if (current.isNotEmpty()) records.add(current.toString())
        return records
    }

    private fun parseLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}
