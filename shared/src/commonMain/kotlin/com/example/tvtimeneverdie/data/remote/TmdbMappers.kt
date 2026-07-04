package com.example.tvtimeneverdie.data.remote

import com.example.tvtimeneverdie.data.remote.dto.TmdbMovieDto
import com.example.tvtimeneverdie.domain.model.Movie

private const val TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"

fun TmdbMovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title,
    posterUrl = posterPath?.let { "$TMDB_POSTER_BASE_URL$it" },
    overview = overview.orEmpty(),
    releaseDate = releaseDate,
    rating = voteAverage,
)
