package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FirestoreDiscussionRepository
import com.example.model.AcademicClass
import com.example.model.DiscussionReply
import com.example.model.DiscussionThread
import com.example.model.Subject
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ForumUiState<out T> {
    data object Loading : ForumUiState<Nothing>
    data class Success<T>(val data: T) : ForumUiState<T>
    data class Error(val message: String, val isPermissionDenied: Boolean = false) : ForumUiState<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class DiscussionViewModel(
    private val repository: FirestoreDiscussionRepository
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)
    private val _activeThreadId = MutableStateFlow<String?>(null)
    val activeThreadId: StateFlow<String?> = _activeThreadId.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    // Local session tracker for feed card upvote highlights while active detail checks exact marker doc
    private val _sessionUpvotedThreadIds = MutableStateFlow<Set<String>>(emptySet())
    val sessionUpvotedThreadIds: StateFlow<Set<String>> = _sessionUpvotedThreadIds.asStateFlow()

    private val _sessionUpvotedReplyIds = MutableStateFlow<Set<String>>(emptySet())
    val sessionUpvotedReplyIds: StateFlow<Set<String>> = _sessionUpvotedReplyIds.asStateFlow()

    val threadsState: StateFlow<ForumUiState<List<DiscussionThread>>> = retryTrigger
        .flatMapLatest {
            repository.observeThreads()
                .map<List<DiscussionThread>, ForumUiState<List<DiscussionThread>>> {
                    ForumUiState.Success(it)
                }
                .catch { e ->
                    val fsEx = e as? FirebaseFirestoreException ?: e.cause as? FirebaseFirestoreException
                    val isPerm = fsEx?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                    emit(
                        ForumUiState.Error(
                            message = fsEx?.localizedMessage
                                ?: e.localizedMessage
                                ?: "Unable to load discussions from Firestore.",
                            isPermissionDenied = isPerm
                        )
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ForumUiState.Loading
        )

    val activeThread: StateFlow<DiscussionThread?> = combine(
        threadsState,
        _activeThreadId
    ) { state, threadId ->
        if (threadId == null || state !is ForumUiState.Success) null
        else state.data.firstOrNull { it.id == threadId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val repliesState: StateFlow<ForumUiState<List<DiscussionReply>>> = combine(
        _activeThreadId,
        retryTrigger
    ) { threadId, _ -> threadId }
        .flatMapLatest { threadId ->
            if (threadId.isNullOrBlank()) {
                flowOf(ForumUiState.Success(emptyList()))
            } else {
                repository.observeReplies(threadId)
                    .map<List<DiscussionReply>, ForumUiState<List<DiscussionReply>>> {
                        ForumUiState.Success(it)
                    }
                    .catch { e ->
                        val fsEx = e as? FirebaseFirestoreException ?: e.cause as? FirebaseFirestoreException
                        emit(
                            ForumUiState.Error(
                                message = fsEx?.localizedMessage
                                    ?: e.localizedMessage
                                    ?: "Unable to load replies."
                            )
                        )
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ForumUiState.Success(emptyList())
        )

    val isActiveThreadUpvoted: StateFlow<Boolean> = _activeThreadId
        .flatMapLatest { threadId ->
            if (threadId.isNullOrBlank()) flowOf(false)
            else repository.observeMyThreadUpvote(threadId).catch { emit(false) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    fun openThread(threadId: String) {
        _activeThreadId.value = threadId
    }

    fun closeThread() {
        _activeThreadId.value = null
    }

    fun retry() {
        retryTrigger.update { it + 1 }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun createThread(
        title: String,
        content: String,
        subject: Subject,
        academicClass: AcademicClass,
        chapterName: String,
        tags: List<String>,
        authorName: String?,
        authorSchool: String,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.createThread(
                title = title,
                content = content,
                subject = subject,
                academicClass = academicClass,
                chapterName = chapterName,
                tags = tags,
                authorNameOverride = authorName,
                authorSchool = authorSchool
            )
            _isSubmitting.value = false
            result.onSuccess { newId ->
                _actionMessage.value = "Question posted to Firestore discussion board!"
                onSuccess(newId)
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to post question."
            }
        }
    }

    fun postReply(
        threadId: String,
        content: String,
        authorName: String?,
        authorSchool: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.postReply(
                threadId = threadId,
                content = content,
                authorNameOverride = authorName,
                authorSchool = authorSchool
            )
            _isSubmitting.value = false
            result.onSuccess {
                _actionMessage.value = "Reply posted!"
                onSuccess()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to post reply."
            }
        }
    }

    fun toggleThreadUpvote(threadId: String) {
        viewModelScope.launch {
            val result = repository.toggleThreadUpvote(threadId)
            result.onSuccess { nowUpvoted ->
                _sessionUpvotedThreadIds.update { set ->
                    if (nowUpvoted) set + threadId else set - threadId
                }
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not update upvote."
            }
        }
    }

    fun toggleReplyUpvote(threadId: String, replyId: String) {
        viewModelScope.launch {
            val result = repository.toggleReplyUpvote(threadId, replyId)
            result.onSuccess { nowUpvoted ->
                _sessionUpvotedReplyIds.update { set ->
                    if (nowUpvoted) set + replyId else set - replyId
                }
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not update vote."
            }
        }
    }

    fun toggleAcceptedSolution(thread: DiscussionThread, reply: DiscussionReply) {
        viewModelScope.launch {
            val result = repository.toggleAcceptedSolution(thread, reply)
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not update solution status."
            }
        }
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            val result = repository.deleteThread(threadId)
            result.onSuccess {
                if (_activeThreadId.value == threadId) {
                    _activeThreadId.value = null
                }
                _actionMessage.value = "Thread deleted."
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not delete thread."
            }
        }
    }

    fun deleteReply(threadId: String, replyId: String) {
        viewModelScope.launch {
            val result = repository.deleteReply(threadId, replyId)
            result.onSuccess {
                _actionMessage.value = "Reply deleted."
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not delete reply."
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiscussionViewModel(FirestoreDiscussionRepository(context.applicationContext)) as T
        }
    }
}
