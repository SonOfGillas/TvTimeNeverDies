package com.example.tvtimeneverdie.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination

@Serializable
data object HomeDestination

@Serializable
data object SearchDestination

@Serializable
data object ProfileDestination

@Serializable
data class ShowDetailDestination(val showId: Int)

@Serializable
data class MovieDetailDestination(val movieId: Int)

@Serializable
data class EpisodeDetailDestination(
    val episodeId: Int,
    val showId: Int,
    val season: Int,
    val number: Int?,
)
