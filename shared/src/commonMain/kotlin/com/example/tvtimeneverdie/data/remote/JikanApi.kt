package com.example.tvtimeneverdie.data.remote

import com.example.tvtimeneverdie.data.remote.dto.JikanAnimeDto
import com.example.tvtimeneverdie.data.remote.dto.JikanAnimeSingleResponseDto
import com.example.tvtimeneverdie.data.remote.dto.JikanEpisodeDto
import com.example.tvtimeneverdie.data.remote.dto.JikanEpisodesResponseDto
import com.example.tvtimeneverdie.data.remote.dto.JikanSearchResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

private const val JIKAN_BASE_URL = "https://api.jikan.moe/v4"

/** Client per Jikan (https://jikan.moe), API non ufficiale di MyAnimeList: nessuna chiave richiesta. */
class JikanApi(
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    },
) {
    suspend fun searchAnime(query: String): List<JikanAnimeDto> =
        httpClient.get("$JIKAN_BASE_URL/anime") {
            parameter("q", query)
            parameter("limit", 10)
        }.body<JikanSearchResponseDto>().data

    suspend fun getAnime(malId: Int): JikanAnimeDto =
        httpClient.get("$JIKAN_BASE_URL/anime/$malId").body<JikanAnimeSingleResponseDto>().data

    /** Pagina tutti gli episodi rispettando il rate limit di Jikan (~3 richieste/sec). */
    suspend fun getEpisodes(malId: Int): List<JikanEpisodeDto> {
        val all = mutableListOf<JikanEpisodeDto>()
        var page = 1
        while (true) {
            val response = httpClient.get("$JIKAN_BASE_URL/anime/$malId/episodes") {
                parameter("page", page)
            }.body<JikanEpisodesResponseDto>()
            all += response.data
            if (response.pagination?.hasNextPage != true) break
            page++
            delay(350)
        }
        return all
    }
}
