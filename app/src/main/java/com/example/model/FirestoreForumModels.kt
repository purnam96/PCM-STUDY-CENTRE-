package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Cloud Firestore document model for a Physics/Chemistry peer discussion thread.
 * Avoids `is`-prefixed Booleans so CustomClassMapper field names match firestore.rules verbatim.
 */
@Keep
@IgnoreExtraProperties
data class DiscussionThread(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val subject: String = Subject.PHYSICS.name,
    val academicClass: String = AcademicClass.CLASS_10.name,
    val chapterName: String = "",
    val tags: List<String> = emptyList(),
    val authorId: String = "",
    val authorName: String = "PCM Student",
    val authorSchool: String = "",
    val visibility: String = "public",
    val solved: Boolean = false,
    val acceptedReplyId: String? = null,
    val upvotesCount: Int = 0,
    val repliesCount: Int = 0,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun resolvedSubject(): Subject =
        Subject.entries.firstOrNull { it.name == subject } ?: Subject.PHYSICS

    fun resolvedClass(): AcademicClass =
        AcademicClass.entries.firstOrNull { it.name == academicClass } ?: AcademicClass.CLASS_10

    fun createdMillis(): Long = createdAt?.toDate()?.time ?: 0L
}

/**
 * Cloud Firestore document model for a peer reply/solution under /threads/{threadId}/replies/{replyId}.
 */
@Keep
@IgnoreExtraProperties
data class DiscussionReply(
    @DocumentId
    val id: String = "",
    val threadId: String = "",
    val authorId: String = "",
    val authorName: String = "PCM Student",
    val authorSchool: String = "",
    val authorBadge: String = "Student Peer",
    val content: String = "",
    val visibility: String = "public",
    val acceptedSolution: Boolean = false,
    val upvotesCount: Int = 0,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun createdMillis(): Long = createdAt?.toDate()?.time ?: 0L
}
