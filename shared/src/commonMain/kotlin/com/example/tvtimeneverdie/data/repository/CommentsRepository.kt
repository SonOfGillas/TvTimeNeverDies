package com.example.tvtimeneverdie.data.repository

import com.example.tvtimeneverdie.domain.model.EpisodeComment
import com.example.tvtimeneverdie.util.currentTimeMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class CommentEntry(
    val uid: String = "",
    val displayName: String = "",
    val text: String = "",
    val createdAtMillis: Long = 0,
)

class CommentsRepository {
    private val firestore = Firebase.firestore

    private fun commentsCollection(episodeId: Int) =
        firestore.collection("episodeComments").document(episodeId.toString()).collection("comments")

    fun comments(episodeId: Int): Flow<List<EpisodeComment>> =
        commentsCollection(episodeId).snapshots.map { snapshot ->
            snapshot.documents
                .map { doc -> doc.id to doc.data<CommentEntry>() }
                .map { (id, entry) -> entry.toDomain(id) }
                .sortedBy { it.createdAtMillis }
        }

    suspend fun addComment(episodeId: Int, uid: String, displayName: String, text: String) {
        commentsCollection(episodeId).add(
            CommentEntry(
                uid = uid,
                displayName = displayName,
                text = text,
                createdAtMillis = currentTimeMillis(),
            ),
        )
    }

    private fun CommentEntry.toDomain(id: String) = EpisodeComment(
        id = id,
        uid = uid,
        displayName = displayName,
        text = text,
        createdAtMillis = createdAtMillis,
    )
}
