package com.example.tvtimeneverdie.domain.model

data class EpisodeComment(
    val id: String,
    val uid: String,
    val displayName: String,
    val text: String,
    val createdAtMillis: Long,
)
