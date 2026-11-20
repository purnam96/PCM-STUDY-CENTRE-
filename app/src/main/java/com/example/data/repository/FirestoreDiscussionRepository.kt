package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.model.AcademicClass
import com.example.model.DiscussionReply
import com.example.model.DiscussionThread
import com.example.model.Subject
import com.example.util.OperationType
import com.example.util.logFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Cloud Firestore repository powering the real-time Physics & Chemistry Peer Discussion Board.
 * All queries bind explicitly to the provisioned named Firestore database via R.string.firestore_database_id.
 */
class FirestoreDiscussionRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    constructor(context: Context) : this(
        firestore = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = FirebaseAuth.getInstance()
    )

    companion object {
        const val COLLECTION_THREADS = "threads"
        const val SUBCOLLECTION_REPLIES = "replies"
        const val SUBCOLLECTION_UPVOTES = "upvotes"
        const val VISIBILITY_PUBLIC = "public"
    }

    /**
     * Real-time stream of public discussion threads sorted newest first.
     * Matches firestore.rules: allow list: if isSignedIn() && existing().visibility == 'public';
     * Avoids multi-field orderBy so zero custom composite indexes are required.
     */
    fun observeThreads(): Flow<List<DiscussionThread>> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) return flowOf(emptyList())

        return firestore.collection(COLLECTION_THREADS)
            .whereEqualTo("visibility", VISIBILITY_PUBLIC)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc ->
                        doc.toObject(
                            DiscussionThread::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    }
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
            }
    }

    /**
     * Real-time stream of replies for a specific thread.
     * Accepted solutions appear first, followed by chronological order.
     */
    fun observeReplies(threadId: String): Flow<List<DiscussionReply>> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank() || threadId.isBlank()) return flowOf(emptyList())

        return firestore.collection(COLLECTION_THREADS)
            .document(threadId)
            .collection(SUBCOLLECTION_REPLIES)
            .whereEqualTo("visibility", VISIBILITY_PUBLIC)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc ->
                        doc.toObject(
                            DiscussionReply::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    }
                    .sortedWith(
                        compareByDescending<DiscussionReply> { it.acceptedSolution }
                            .thenBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                    )
            }
    }

    /**
     * Observes whether the current signed-in user has upvoted the given thread.
     */
    fun observeMyThreadUpvote(threadId: String): Flow<Boolean> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank() || threadId.isBlank()) return flowOf(false)

        return firestore.collection(COLLECTION_THREADS)
            .document(threadId)
            .collection(SUBCOLLECTION_UPVOTES)
            .document(uid)
            .snapshots()
            .map { it.exists() }
    }

    /**
     * Checks whether the current user has upvoted a specific reply.
     */
    fun observeMyReplyUpvote(threadId: String, replyId: String): Flow<Boolean> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank() || threadId.isBlank() || replyId.isBlank()) return flowOf(false)

        return firestore.collection(COLLECTION_THREADS)
            .document(threadId)
            .collection(SUBCOLLECTION_REPLIES)
            .document(replyId)
            .collection(SUBCOLLECTION_UPVOTES)
            .document(uid)
            .snapshots()
            .map { it.exists() }
    }

    /**
     * Creates a new discussion thread in /threads/{threadId}.
     */
    suspend fun createThread(
        title: String,
        content: String,
        subject: Subject,
        academicClass: AcademicClass,
        chapterName: String,
        tags: List<String>,
        authorNameOverride: String? = null,
        authorSchool: String = ""
    ): Result<String> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("Please sign in with Google to post a thread."))
        val uid = user.uid
        if (uid.isBlank()) {
            return Result.failure(IllegalStateException("Invalid user session."))
        }

        val cleanTitle = title.trim().take(160)
        val cleanContent = content.trim().take(4000)
        val cleanChapter = chapterName.trim().ifBlank { "General Syllabus" }.take(120)
        val cleanTags = tags.map { it.trim().take(32) }.filter { it.isNotBlank() }.distinct().take(5)
        val cleanAuthorName = (authorNameOverride?.trim()?.ifBlank { null }
            ?: user.displayName?.trim()?.ifBlank { null }
            ?: "PCM Student").take(80)
        val cleanSchool = authorSchool.trim().take(100)

        if (cleanTitle.isEmpty() || cleanContent.isEmpty()) {
            return Result.failure(IllegalArgumentException("Title and question details cannot be empty."))
        }

        val docRef = firestore.collection(COLLECTION_THREADS).document()
        val payload = hashMapOf<String, Any>(
            "title" to cleanTitle,
            "content" to cleanContent,
            "subject" to subject.name,
            "academicClass" to academicClass.name,
            "chapterName" to cleanChapter,
            "tags" to cleanTags,
            "authorId" to uid,
            "authorName" to cleanAuthorName,
            "authorSchool" to cleanSchool,
            "visibility" to VISIBILITY_PUBLIC,
            "solved" to false,
            "upvotesCount" to 0,
            "repliesCount" to 0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logFirestoreError(e, OperationType.CREATE, "$COLLECTION_THREADS/${docRef.id}")
            Result.failure(e)
        }
    }

    /**
     * Posts a peer reply under /threads/{threadId}/replies/{replyId} and atomically
     * increments the thread's repliesCount using a WriteBatch.
     */
    suspend fun postReply(
        threadId: String,
        content: String,
        authorNameOverride: String? = null,
        authorSchool: String = "",
        authorBadge: String = "Student Peer"
    ): Result<String> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("Please sign in with Google to post a reply."))
        val uid = user.uid
        if (uid.isBlank() || threadId.isBlank()) {
            return Result.failure(IllegalArgumentException("Thread ID or user session is missing."))
        }

        val cleanContent = content.trim().take(3000)
        if (cleanContent.isEmpty()) {
            return Result.failure(IllegalArgumentException("Reply cannot be empty."))
        }

        val cleanAuthorName = (authorNameOverride?.trim()?.ifBlank { null }
            ?: user.displayName?.trim()?.ifBlank { null }
            ?: "Peer Student").take(80)
        val cleanSchool = authorSchool.trim().take(100)
        val cleanBadge = authorBadge.trim().ifBlank { "Student Peer" }.take(40)

        val threadRef = firestore.collection(COLLECTION_THREADS).document(threadId)
        val replyRef = threadRef.collection(SUBCOLLECTION_REPLIES).document()

        val replyPayload = hashMapOf<String, Any>(
            "threadId" to threadId,
            "authorId" to uid,
            "authorName" to cleanAuthorName,
            "authorSchool" to cleanSchool,
            "authorBadge" to cleanBadge,
            "content" to cleanContent,
            "visibility" to VISIBILITY_PUBLIC,
            "acceptedSolution" to false,
            "upvotesCount" to 0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val batch = firestore.batch()
        batch.set(replyRef, replyPayload)
        batch.update(
            threadRef,
            mapOf(
                "repliesCount" to FieldValue.increment(1),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        return try {
            batch.commit().await()
            Result.success(replyRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logFirestoreError(
                e,
                OperationType.WRITE,
                "$COLLECTION_THREADS/$threadId/$SUBCOLLECTION_REPLIES/${replyRef.id}"
            )
            Result.failure(e)
        }
    }

    /**
     * Atomically toggles the current user's upvote marker on a thread and updates upvotesCount.
     */
    suspend fun toggleThreadUpvote(threadId: String): Result<Boolean> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank() || threadId.isBlank()) {
            return Result.failure(IllegalStateException("Sign in required to upvote."))
        }

        val threadRef = firestore.collection(COLLECTION_THREADS).document(threadId)
        val upvoteRef = threadRef.collection(SUBCOLLECTION_UPVOTES).document(uid)

        return try {
            val alreadyUpvoted = upvoteRef.get().await().exists()
            val batch = firestore.batch()
            if (alreadyUpvoted) {
                batch.delete(upvoteRef)
                batch.update(
                    threadRef,
                    mapOf(
                        "upvotesCount" to FieldValue.increment(-1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.set(
                    upvoteRef,
                    mapOf(
                        "userId" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    threadRef,
                    mapOf(
                        "upvotesCount" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(!alreadyUpvoted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logFirestoreError(e, OperationType.WRITE, "$COLLECTION_THREADS/$threadId/$SUBCOLLECTION_UPVOTES/$uid")
            Result.failure(e)
        }
    }

    /**
     * Atomically toggles the current user's upvote marker on a reply and updates upvotesCount.
     */
    suspend fun toggleReplyUpvote(threadId: String, replyId: String): Result<Boolean> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank() || threadId.isBlank() || replyId.isBlank()) {
            return Result.failure(IllegalStateException("Sign in required to upvote."))
        }

        val replyRef = firestore.collection(COLLECTION_THREADS)
            .document(threadId)
            .collection(SUBCOLLECTION_REPLIES)
            .document(replyId)
        val upvoteRef = replyRef.collection(SUBCOLLECTION_UPVOTES).document(uid)

        return try {
            val alreadyUpvoted = upvoteRef.get().await().exists()
            val batch = firestore.batch()
            if (alreadyUpvoted) {
                batch.delete(upvoteRef)
                batch.update(
                    replyRef,
                    mapOf(
                        "upvotesCount" to FieldValue.increment(-1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.set(
                    upvoteRef,
                    mapOf(
                        "userId" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    replyRef,
                    mapOf(
                        "upvotesCount" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(!alreadyUpvoted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logFirestoreError(
                e,
                OperationType.WRITE,
                "$COLLECTION_THREADS/$threadId/$SUBCOLLECTION_REPLIES/$replyId/$SUBCOLLECTION_UPVOTES/$uid"
            )
            Result.failure(e)
        }
    }

    /**
     * Marks or unmarks a reply as the accepted solution for a thread.
     * Restricted by firestore.rules to the thread's original author.
     */
    suspend fun toggleAcceptedSolution(
        thread: DiscussionThread,
        reply: DiscussionReply
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank() || thread.authorId != uid) {
            return Result.failure(IllegalStateException("Only the question author can mark an accepted solution."))
        }

        val threadRef = firestore.collection(COLLECTION_THREADS).document(thread.id)
        val replyRef = threadRef.collection(SUBCOLLECTION_REPLIES).document(reply.id)
        val willAccept = !reply.acceptedSolution

        val batch = firestore.batch()
        if (willAccept) {
            val previousAcceptedId = thread.acceptedReplyId
            if (!previousAcceptedId.isNullOrBlank() && previousAcceptedId != reply.id) {
                val prevRef = threadRef.collection(SUBCOLLECTION_REPLIES).document(previousAcceptedId)
                batch.update(
                    prevRef,
                    mapOf(
                        "acceptedSolution" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.update(
                replyRef,
                mapOf(
                    "acceptedSolution" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                threadRef,
                mapOf(
                    "solved" to true,
                    "acceptedReplyId" to reply.id,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        } else {
            batch.update(
                replyRef,
                mapOf(
                    "acceptedSolution" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                threadRef,
                mapOf(
                    "solved" to false,
                    "acceptedReplyId" to FieldValue.delete(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        return try {
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logFirestoreError(e, OperationType.UPDATE, "$COLLECTION_THREADS/${thread.id}")
            Result.failure(e)
        }
    }

    /**
     * Deletes a thread authored by the current user.
     */
    suspend fun deleteThread(threadId: String): Result<Unit> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank() || threadId.isBlank()) {
            return Result.failure(IllegalStateException("Sign in required."))
        }

        return try {
            firestore.collection(COLLECTION_THREADS).document(threadId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logFirestoreError(e, OperationType.DELETE, "$COLLECTION_THREADS/$threadId")
            Result.failure(e)
        }
    }

    /**
     * Deletes a reply authored by the current user and decrements thread repliesCount.
     */
    suspend fun deleteReply(threadId: String, replyId: String): Result<Unit> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank() || threadId.isBlank() || replyId.isBlank()) {
            return Result.failure(IllegalStateException("Sign in required."))
        }

        val threadRef = firestore.collection(COLLECTION_THREADS).document(threadId)
        val replyRef = threadRef.collection(SUBCOLLECTION_REPLIES).document(replyId)
        val batch = firestore.batch()
        batch.delete(replyRef)
        batch.update(
            threadRef,
            mapOf(
                "repliesCount" to FieldValue.increment(-1),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        return try {
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logFirestoreError(
                e,
                OperationType.DELETE,
                "$COLLECTION_THREADS/$threadId/$SUBCOLLECTION_REPLIES/$replyId"
            )
            Result.failure(e)
        }
    }
}
