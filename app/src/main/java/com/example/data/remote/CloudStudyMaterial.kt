package com.example.data.remote

import com.example.data.local.StudyMaterialEntity
import com.example.model.AcademicClass
import com.example.model.MaterialCategory
import com.example.model.StudyMaterial
import com.example.model.Subject
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Cloud Firestore document data model for study materials.
 * Structured for cloud persistence, multi-device synchronization, and online catalog updates.
 */
@IgnoreExtraProperties
data class CloudStudyMaterial(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val academicClass: String = "CLASS_10",
    val subject: String = "PHYSICS",
    val chapterName: String = "",
    val chapterNumber: Int = 1,
    val category: String = "NOTES",
    val examYear: Int? = null,
    val academicYear: String = "2024-2025",
    val year: String = "2024-2025",
    val fileUrl: String = "",
    val fileSize: String = "1.5 MB",
    val fileSizeBytes: Long = 1024 * 512,
    val isOfficialBoard: Boolean = true,
    val isVerified: Boolean = true,
    val verificationStatus: String = "VERIFIED",
    val pageCount: Int = 8,
    val downloadCount: Int = 0,
    val tags: List<String> = emptyList(),
    val keyTakeaways: List<String> = emptyList(),
    val contentSummary: String = "",
    val storageDownloadUrl: String? = null,
    val uploaderId: String = "",
    val visibility: String = "public",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

fun CloudStudyMaterial.toDomainModel(): StudyMaterial {
    val parsedClass = try {
        AcademicClass.valueOf(academicClass)
    } catch (e: Exception) {
        AcademicClass.CLASS_10
    }

    val parsedSubject = try {
        Subject.valueOf(subject)
    } catch (e: Exception) {
        Subject.PHYSICS
    }

    val parsedCategory = try {
        MaterialCategory.valueOf(category)
    } catch (e: Exception) {
        MaterialCategory.NOTES
    }

    return StudyMaterial(
        id = id,
        title = title,
        description = description,
        academicClass = parsedClass,
        subject = parsedSubject,
        chapterName = chapterName,
        chapterNumber = chapterNumber,
        category = parsedCategory,
        examYear = examYear,
        academicYear = academicYear,
        year = year.ifBlank { academicYear },
        fileUrl = fileUrl.ifBlank { storageDownloadUrl ?: "" },
        fileSize = fileSize,
        fileSizeBytes = fileSizeBytes,
        isOfficialBoard = isOfficialBoard,
        isVerified = isVerified,
        verificationStatus = verificationStatus,
        pageCount = pageCount,
        downloadCount = downloadCount,
        tags = tags,
        keyTakeaways = keyTakeaways,
        contentSummary = contentSummary
    )
}

fun StudyMaterial.toCloudModel(): CloudStudyMaterial {
    return CloudStudyMaterial(
        id = id,
        title = title,
        description = description,
        academicClass = academicClass.name,
        subject = subject.name,
        chapterName = chapterName,
        chapterNumber = chapterNumber,
        category = category.name,
        examYear = examYear,
        academicYear = academicYear,
        year = year.ifBlank { academicYear },
        fileUrl = fileUrl,
        fileSize = fileSize,
        fileSizeBytes = fileSizeBytes,
        isOfficialBoard = isOfficialBoard,
        isVerified = isVerified,
        verificationStatus = verificationStatus,
        pageCount = pageCount,
        downloadCount = downloadCount,
        tags = tags,
        keyTakeaways = keyTakeaways,
        contentSummary = contentSummary,
        storageDownloadUrl = fileUrl.ifBlank { null }
    )
}

fun CloudStudyMaterial.toEntity(
    isBookmarked: Boolean = false,
    isDownloaded: Boolean = false,
    downloadedAt: Long? = null
): StudyMaterialEntity {
    return this.toDomainModel().copy(
        isBookmarked = isBookmarked,
        isDownloaded = isDownloaded,
        downloadedAt = downloadedAt,
        syncedWithCloud = true,
        lastModified = updatedAt?.toDate()?.time ?: System.currentTimeMillis()
    )
}
