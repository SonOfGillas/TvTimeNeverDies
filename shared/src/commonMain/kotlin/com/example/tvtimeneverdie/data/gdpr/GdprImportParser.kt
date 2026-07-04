package com.example.tvtimeneverdie.data.gdpr

data class ParsedSeries(
    val name: String,
    val nbEpisodesSeen: Int,
    val isFollowed: Boolean,
)

data class ParsedMovie(
    val name: String,
)

/**
 * Estrae SOLO le informazioni richieste dall'export GDPR di TV Time: elenco serie
 * (viste/da vedere) e nomi di film con cui l'utente ha interagito (unico segnale di "vista"
 * disponibile nell'export: TV Time non esporta una lista pulita di film con ID di catalogo,
 * ne' un elenco di film "da vedere"). Tutto il resto del contenuto dello zip viene ignorato.
 */
object GdprImportParser {

    val WANTED_FILE_NAMES = setOf(
        "user_tv_show_data.csv",
        "followed_tv_show.csv",
        "comments-prod-comments.csv",
        "ratings-live-votes.csv",
        "emotions-live-votes.csv",
    )

    fun parseSeries(files: Map<String, String>): List<ParsedSeries> {
        val bySeriesName = mutableMapOf<String, ParsedSeries>()

        files["user_tv_show_data.csv"]?.let(GdprCsv::parseRows)?.forEach { row ->
            val name = row["tv_show_name"]?.trim().orEmpty()
            if (name.isEmpty()) return@forEach
            val nbSeen = row["nb_episodes_seen"]?.toIntOrNull() ?: 0
            val isFollowed = row["is_followed"] == "1"
            bySeriesName[name] = ParsedSeries(name, nbSeen, isFollowed)
        }

        files["followed_tv_show.csv"]?.let(GdprCsv::parseRows)?.forEach { row ->
            val name = row["tv_show_name"]?.trim().orEmpty()
            if (name.isEmpty() || bySeriesName.containsKey(name)) return@forEach
            bySeriesName[name] = ParsedSeries(name, nbEpisodesSeen = 0, isFollowed = true)
        }

        return bySeriesName.values.toList()
    }

    fun parseMovies(files: Map<String, String>): List<ParsedMovie> {
        val names = sortedSetOf<String>()
        for (fileName in listOf("comments-prod-comments.csv", "ratings-live-votes.csv", "emotions-live-votes.csv")) {
            files[fileName]?.let(GdprCsv::parseRows)?.forEach { row ->
                val movieName = row["movie_name"]?.trim().orEmpty()
                if (movieName.isNotEmpty()) names.add(movieName)
            }
        }
        return names.map { ParsedMovie(it) }
    }
}
