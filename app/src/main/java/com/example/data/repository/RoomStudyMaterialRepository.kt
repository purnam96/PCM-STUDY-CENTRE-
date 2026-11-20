package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.CurriculumData
import com.example.data.local.AppDatabase
import com.example.data.local.StudyMaterialDao
import com.example.data.local.toDomainModel
import com.example.data.local.toEntity
import com.example.data.remote.CloudStudyMaterial
import com.example.data.remote.toCloudModel
import com.example.data.remote.toEntity
import com.example.model.AcademicClass
import com.example.model.StudyMaterial
import com.example.model.Subject
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository integrating local Room SQLite storage with cloud Firestore database.
 * Ensures offline-first capability for students with automatic seeding and optional cloud synchronization.
 */
class RoomStudyMaterialRepository(
    private val dao: StudyMaterialDao,
    private val firestore: FirebaseFirestore? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val TAG = "RoomStudyRepository"
        private const val COLLECTION_STUDY_MATERIALS = "study_materials"

        @Volatile
        private var INSTANCE: RoomStudyMaterialRepository? = null

        fun getInstance(context: Context): RoomStudyMaterialRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val firestoreInstance = try {
                    FirebaseFirestore.getInstance(
                        context.applicationContext.getString(R.string.firestore_database_id)
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore not yet configured or unavailable: ${e.message}")
                    null
                }
                val repo = RoomStudyMaterialRepository(db.studyMaterialDao(), firestoreInstance)
                INSTANCE = repo
                repo
            }
        }
    }

    /**
     * Seeds initial BOSEM curriculum materials into Room if the database is empty.
     */
    suspend fun seedInitialDataIfEmpty() = withContext(ioDispatcher) {
        val count = dao.getCount()
        if (count == 0) {
            val initialMaterials = CurriculumData.getInitialMaterials()
            val entities = initialMaterials.map { mat ->
                mat.toEntity(
                    isBookmarked = (mat.id == "c10-phy-ch1-notes" || mat.id == "bosem-hslc-paper-2024")
                )
            }
            dao.insertMaterials(entities)
            Log.d(TAG, "Seeded ${entities.size} initial study materials into Room database.")
        }
    }

    /**
     * Observes all study materials reactively from Room.
     */
    fun getAllMaterials(): Flow<List<StudyMaterial>> {
        return dao.getAllMaterials().map { list -> list.map { it.toDomainModel() } }
    }

    /**
     * Observes study materials filtered by class.
     */
    fun getMaterialsByClass(academicClass: AcademicClass): Flow<List<StudyMaterial>> {
        return dao.getMaterialsByClass(academicClass.name).map { list -> list.map { it.toDomainModel() } }
    }

    /**
     * Observes study materials filtered by class and subject.
     */
    fun getMaterialsByClassAndSubject(academicClass: AcademicClass, subject: Subject): Flow<List<StudyMaterial>> {
        return dao.getMaterialsByClassAndSubject(academicClass.name, subject.name)
            .map { list -> list.map { it.toDomainModel() } }
    }

    /**
     * Observes bookmarked study materials from Room.
     */
    fun getBookmarkedMaterials(): Flow<List<StudyMaterial>> {
        return dao.getBookmarkedMaterials().map { list -> list.map { it.toDomainModel() } }
    }

    /**
     * Observes downloaded study materials from Room.
     */
    fun getDownloadedMaterials(): Flow<List<StudyMaterial>> {
        return dao.getDownloadedMaterials().map { list -> list.map { it.toDomainModel() } }
    }

    /**
     * Toggles bookmark state for a material in Room.
     */
    suspend fun toggleBookmark(materialId: String, currentIsBookmarked: Boolean) = withContext(ioDispatcher) {
        dao.updateBookmarkStatus(materialId, !currentIsBookmarked)
    }

    /**
     * Updates download status in Room when a PDF is saved offline or deleted.
     */
    suspend fun updateDownloadStatus(materialId: String, isDownloaded: Boolean) = withContext(ioDispatcher) {
        val downloadedAt = if (isDownloaded) System.currentTimeMillis() else null
        dao.updateDownloadStatus(materialId, isDownloaded, downloadedAt)
    }

    /**
     * Clears all downloaded flags from the Room database.
     */
    suspend fun clearAllDownloads() = withContext(ioDispatcher) {
        dao.clearAllDownloads()
    }

    /**
     * Inserts or updates a study material locally in Room.
     */
    suspend fun saveMaterialLocally(material: StudyMaterial) = withContext(ioDispatcher) {
        dao.insertMaterial(material.toEntity())
    }

    /**
     * Deletes a study material from Room.
     */
    suspend fun deleteMaterial(materialId: String) = withContext(ioDispatcher) {
        dao.deleteMaterialById(materialId)
    }

    var isOnline: Boolean = true

    /**
     * Syncs cloud study materials from Firestore into the local Room database.
     */
    suspend fun syncWithFirestore(): Result<Int> = withContext(ioDispatcher) {
        if (!isOnline) {
            return@withContext Result.failure(IllegalStateException("Device is offline. Network requests are prevented."))
        }
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not configured."))
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (uid.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Sign in required to sync cloud materials."))
        }
        try {
            val snapshot = db.collection(COLLECTION_STUDY_MATERIALS)
                .whereEqualTo("visibility", "public")
                .get()
                .await()
            val cloudItems = snapshot.toObjects(CloudStudyMaterial::class.java)

            if (cloudItems.isNotEmpty()) {
                val entities = cloudItems.map { cloudMat ->
                    val existing = dao.getMaterialById(cloudMat.id)
                    cloudMat.toEntity(
                        isBookmarked = existing?.isBookmarked ?: false,
                        isDownloaded = existing?.isDownloaded ?: false,
                        downloadedAt = existing?.downloadedAt
                    )
                }
                dao.insertMaterials(entities)
                Log.d(TAG, "Successfully synced ${entities.size} items from Firestore into Room.")
                Result.success(entities.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing with Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a study material to the Firestore cloud catalog.
     */
    suspend fun uploadToFirestore(material: StudyMaterial): Result<Unit> = withContext(ioDispatcher) {
        if (!isOnline) {
            return@withContext Result.failure(IllegalStateException("Device is offline. Network requests are prevented."))
        }
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not configured."))
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (uid.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Sign in required to upload materials."))
        }
        try {
            val cloudModel = material.toCloudModel().copy(uploaderId = uid, visibility = "public")
            db.collection(COLLECTION_STUDY_MATERIALS)
                .document(material.id)
                .set(cloudModel)
                .await()
            // Mark as synced locally
            dao.updateMaterial(material.toEntity(syncedWithCloud = true))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading material to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
}
