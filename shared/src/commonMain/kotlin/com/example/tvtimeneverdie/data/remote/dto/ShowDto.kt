package com.example.tvtimeneverdie.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShowDto(
    val id: Int,
    val name: String,
    val genres: List<String> = emptyList(),
    val status: String? = null,
    val premiered: String? = null,
    val ended: String? = null,
    val rating: RatingDto? = null,
    val image: ImageDto? = null,
    val summary: String? = null,
    val network: NetworkDto? = null,
    val webChannel: NetworkDto? = null,
)

@Serializable
data class RatingDto(
    val average: Double? = null,
)

@Serializable
data class ImageDto(
    val medium: String? = null,
    val original: String? = null,
)

@Serializable
data class NetworkDto(
    val name: String? = null,
)

@Serializable
data class SearchResultDto(
    val score: Double,
    val show: ShowDto,
)
