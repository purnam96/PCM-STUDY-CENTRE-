package com.example.data.local

import com.example.model.StudyMaterial

/**
 * Typealias for backward compatibility, mapping StudyMaterialEntity directly
 * to the unified StudyMaterial Room Entity and domain model.
 */
typealias StudyMaterialEntity = StudyMaterial

/**
 * Idempotent converter from StudyMaterialEntity to StudyMaterial domain model.
 */
fun StudyMaterial.toDomainModel(): StudyMaterial = this

/**
 * Converts StudyMaterial domain model to local entity instance with bookmark/download flags.
 */
fun StudyMaterial.toEntity(
    isBookmarked: Boolean = this.isBookmarked,
    isDownloaded: Boolean = this.isDownloaded,
    downloadedAt: Long? = this.downloadedAt,
    syncedWithCloud: Boolean = this.syncedWithCloud
): StudyMaterial = this.copy(
    isBookmarked = isBookmarked,
    isDownloaded = isDownloaded,
    downloadedAt = downloadedAt,
    syncedWithCloud = syncedWithCloud
)
