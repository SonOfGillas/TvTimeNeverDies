package com.example.tvtimeneverdie.data.repository

import com.example.tvtimeneverdie.domain.model.Episode
import com.example.tvtimeneverdie.util.currentTimeMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class WatchlistEntry(
    val showId: Int = 0,
    val addedAtMillis: Long = 0,
)

@Serializable
data class WatchedEpisodeEntry(
    val showId: Int = 0,
    val episodeId: Int = 0,
    val season: Int = 0,
    val number: Int? = null,
    val watchedAtMillis: Long = 0,
)

class UserShowsRepository {
    private val firestore = Firebase.firestore

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)
    private fun watchlistCollection(uid: String) = userDoc(uid).collection("watchlist")
    private fun watchedEpisodesCollection(uid: String) = userDoc(uid).collection("watchedEpisodes")

    suspend fun addToWatchlist(uid: String, showId: Int) {
        watchlistCollection(uid).document(showId.toString())
            .set(WatchlistEntry(showId = showId, addedAtMillis = currentTimeMillis()))
    }

    suspend fun removeFromWatchlist(uid: String, showId: Int) {
        watchlistCollection(uid).document(showId.toString()).delete()
    }

    fun watchlistIds(uid: String): Flow<Set<Int>> =
        watchlistCollection(uid).snapshots.map { snapshot ->
            snapshot.documents.map { it.data<WatchlistEntry>().showId }.toSet()
        }

    suspend fun setEpisodeWatched(
        uid: String,
        showId: Int,
        episodeId: Int,
        season: Int,
        number: Int?,
        watched: Boolean,
    ) {
        val doc = watchedEpisodesCollection(uid).document(episodeId.toString())
        if (watched) {
            doc.set(
                WatchedEpisodeEntry(
                    showId = showId,
                    episodeId = episodeId,
                    season = season,
                    number = number,
                    watchedAtMillis = currentTimeMillis(),
                ),
            )
        } else {
            doc.delete()
        }
    }

    suspend fun setSeasonWatched(uid: String, showId: Int, episodes: List<Episode>, watched: Boolean) {
        episodes.forEach { episode ->
            setEpisodeWatched(uid, showId, episode.id, episode.season, episode.number, watched)
        }
    }

    /** All episodes the user has marked as watched, across every show. */
    fun allWatchedEpisodes(uid: String): Flow<List<WatchedEpisodeEntry>> =
        watchedEpisodesCollection(uid).snapshots.map { snapshot ->
            snapshot.documents.map { it.data<WatchedEpisodeEntry>() }
        }
}
