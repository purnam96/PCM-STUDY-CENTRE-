package com.example.model

import java.util.UUID

/**
 * Question posted in the peer study discussion forum.
 */
data class ForumQuestion(
    val id: String = UUID.randomUUID().toString(),
    val subject: Subject = Subject.PHYSICS,
    val academicClass: AcademicClass = AcademicClass.CLASS_10,
    val chapterName: String = "",
    val title: String = "",
    val content: String = "",
    val authorName: String = "PCM Student",
    val authorSchool: String = "BOSEM Secondary School",
    val authorRollNo: String = "BOSEM-STUDENT",
    val timestamp: Long = System.currentTimeMillis(),
    val upvotesCount: Int = 0,
    val isSolved: Boolean = false,
    val tags: List<String> = emptyList(),
    val replies: List<ForumReply> = emptyList(),
    val userUpvoted: Boolean = false
)

/**
 * Peer reply/solution to a forum question.
 */
data class ForumReply(
    val id: String = UUID.randomUUID().toString(),
    val questionId: String = "",
    val authorName: String = "",
    val authorSchool: String = "",
    val authorBadge: String = "Peer",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val upvotesCount: Int = 0,
    val isAcceptedSolution: Boolean = false,
    val userUpvoted: Boolean = false
)
