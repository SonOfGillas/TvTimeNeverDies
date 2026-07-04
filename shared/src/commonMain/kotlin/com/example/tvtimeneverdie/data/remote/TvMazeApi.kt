package com.example.tvtimeneverdie.data.remote

import com.example.tvtimeneverdie.data.remote.dto.EpisodeDto
import com.example.tvtimeneverdie.data.remote.dto.SearchResultDto
import com.example.tvtimeneverdie.data.remote.dto.ShowDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://api.tvmaze.com"

class TvMazeApi(
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    },
) {
    /** Show ids ordered by most recently updated/added on TVmaze, most recent first. */
    suspend fun getRecentlyUpdatedShowIds(limit: Int = 30): List<Int> {
        val updates: Map<String, Long> = httpClient.get("$BASE_URL/updates/shows").body()
        return updates.entries
            .sortedByDescending { it.value }
            .take(limit)
            .mapNotNull { it.key.toIntOrNull() }
    }

    suspend fun getShow(showId: Int): ShowDto =
        httpClient.get("$BASE_URL/shows/$showId").body()

    suspend fun getEpisodes(showId: Int): List<EpisodeDto> =
        httpClient.get("$BASE_URL/shows/$showId/episodes").body()

    suspend fun searchShows(query: String): List<SearchResultDto> =
        httpClient.get("$BASE_URL/search/shows") { parameter("q", query) }.body()
}
