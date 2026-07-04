package com.example.tvtimeneverdie.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val overview: String,
    val releaseDate: String?,
    val rating: Double?,
)
