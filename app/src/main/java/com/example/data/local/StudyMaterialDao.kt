package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyMaterialDao {

    @Query("SELECT * FROM study_materials ORDER BY chapterNumber ASC, title ASC")
    fun getAllMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE academicClass = :academicClass ORDER BY chapterNumber ASC, title ASC")
    fun getMaterialsByClass(academicClass: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE academicClass = :academicClass AND subject = :subject ORDER BY chapterNumber ASC, title ASC")
    fun getMaterialsByClassAndSubject(academicClass: String, subject: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE academicClass = :academicClass AND category = :category ORDER BY examYear DESC, title ASC")
    fun getMaterialsByCategory(academicClass: String, category: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE isBookmarked = 1 ORDER BY lastModified DESC")
    fun getBookmarkedMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE isDownloaded = 1 ORDER BY downloadedAt DESC")
    fun getDownloadedMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE id = :id LIMIT 1")
    suspend fun getMaterialById(id: String): StudyMaterialEntity?

    @Query("SELECT COUNT(*) FROM study_materials")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<StudyMaterialEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: StudyMaterialEntity)

    @Update
    suspend fun updateMaterial(material: StudyMaterialEntity)

    @Query("UPDATE study_materials SET isBookmarked = :isBookmarked, lastModified = :timestamp WHERE id = :id")
    suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE study_materials SET isDownloaded = :isDownloaded, downloadedAt = :downloadedAt, lastModified = :timestamp WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean, downloadedAt: Long?, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM study_materials WHERE id = :id")
    suspend fun deleteMaterialById(id: String)

    @Query("UPDATE study_materials SET isDownloaded = 0, downloadedAt = NULL, lastModified = :timestamp WHERE isDownloaded = 1")
    suspend fun clearAllDownloads(timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM study_materials")
    suspend fun deleteAll()
}
