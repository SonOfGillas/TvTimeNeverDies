package com.example.tvtimeneverdie.data.remote

import com.example.tvtimeneverdie.config.TmdbConfig
import com.example.tvtimeneverdie.data.remote.dto.TmdbMovieDto
import com.example.tvtimeneverdie.data.remote.dto.TmdbMovieResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3"

class TmdbApi(
    private val apiKey: String = TmdbConfig.API_KEY,
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    },
) {
    suspend fun getNowPlaying(page: Int = 1): List<TmdbMovieDto> =
        httpClient.get("$TMDB_BASE_URL/movie/now_playing") {
            parameter("api_key", apiKey)
            parameter("language", "it-IT")
            parameter("page", page)
        }.body<TmdbMovieResponseDto>().results

    suspend fun searchMovies(query: String): List<TmdbMovieDto> =
        httpClient.get("$TMDB_BASE_URL/search/movie") {
            parameter("api_key", apiKey)
            parameter("language", "it-IT")
            parameter("query", query)
        }.body<TmdbMovieResponseDto>().results

    suspend fun getMovie(movieId: Int): TmdbMovieDto =
        httpClient.get("$TMDB_BASE_URL/movie/$movieId") {
            parameter("api_key", apiKey)
            parameter("language", "it-IT")
        }.body()
}
