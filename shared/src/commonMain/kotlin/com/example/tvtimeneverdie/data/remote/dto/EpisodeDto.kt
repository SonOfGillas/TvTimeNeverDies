package com.example.tvtimeneverdie.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EpisodeDto(
    val id: Int,
    val name: String,
    val season: Int,
    val number: Int? = null,
    val type: String? = null,
    val airdate: String? = null,
    val airtime: String? = null,
    val runtime: Int? = null,
    val image: ImageDto? = null,
    val summary: String? = null,
)
