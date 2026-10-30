package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val isFavorite: Boolean = false,
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastReadPage: Int = 0,
    val rotationDegrees: Int = 0,
    val bookmarkedPagesCsv: String = "",
    val searchableSummary: String = "",
    val categoryTag: String = "Document"
) {
    val bookmarkedPages: Set<Int>
        get() = bookmarkedPagesCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
}

@Entity(tableName = "processing_history")
data class ProcessingHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String, // MERGE, SPLIT, COMPRESS, IMAGE_TO_PDF, SCAN, ANNOTATE, SIGN
    val inputSummary: String,
    val outputFileName: String,
    val outputFilePath: String,
    val inputSizeBytes: Long,
    val outputSizeBytes: Long,
    val pageCount: Int,
    val status: String, // SUCCESS, CANCELLED, FAILED
    val detailsMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "pdf_annotations")
data class PdfAnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val pageIndex: Int,
    val annotationType: String, // HIGHLIGHT, UNDERLINE, FREEHAND, TEXT_NOTE, SIGNATURE
    val textContent: String = "",
    val colorHex: Long = 0xFFFBBF24,
    val normalizedX: Float = 0.15f,
    val normalizedY: Float = 0.25f,
    val normalizedWidth: Float = 0.55f,
    val normalizedHeight: Float = 0.08f,
    val pointsSerialized: String = "", // "x1,y1;x2,y2;..." in normalized 0..1 page coordinates
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_signatures")
data class SavedSignatureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val inkColorHex: Long = 0xFF0F172A,
    val strokesSerialized: String, // strokes separated by "|" and points by ";"
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface PdflyDao {
    @Query("SELECT * FROM pdf_documents ORDER BY lastOpenedAt DESC")
    fun observeAllDocuments(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents ORDER BY lastOpenedAt DESC")
    suspend fun observeAllDocumentsSnapshot(): List<PdfDocumentEntity>

    @Query("SELECT * FROM pdf_documents WHERE isFavorite = 1 ORDER BY lastOpenedAt DESC")
    fun observeFavoriteDocuments(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): PdfDocumentEntity?

    @Query("SELECT COUNT(*) FROM pdf_documents")
    suspend fun getDocumentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: PdfDocumentEntity): Long

    @Update
    suspend fun updateDocument(doc: PdfDocumentEntity)

    @Query("DELETE FROM pdf_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("SELECT * FROM processing_history ORDER BY timestamp DESC")
    fun observeProcessingHistory(): Flow<List<ProcessingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: ProcessingHistoryEntity): Long

    @Query("DELETE FROM processing_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM pdf_annotations WHERE documentId = :documentId ORDER BY createdAt ASC")
    fun observeAnnotationsForDocument(documentId: Long): Flow<List<PdfAnnotationEntity>>

    @Query("SELECT * FROM pdf_annotations WHERE documentId = :documentId ORDER BY createdAt ASC")
    suspend fun getAnnotationsForDocumentSync(documentId: Long): List<PdfAnnotationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: PdfAnnotationEntity): Long

    @Query("DELETE FROM pdf_annotations WHERE id = :annotationId")
    suspend fun deleteAnnotationById(annotationId: Long)

    @Query("DELETE FROM pdf_annotations WHERE documentId = :documentId")
    suspend fun deleteAnnotationsForDocument(documentId: Long)

    @Query("SELECT * FROM saved_signatures ORDER BY createdAt DESC")
    fun observeSavedSignatures(): Flow<List<SavedSignatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignature(signature: SavedSignatureEntity): Long

    @Query("DELETE FROM saved_signatures WHERE id = :id")
    suspend fun deleteSignatureById(id: Long)
}

@Database(
    entities = [
        PdfDocumentEntity::class,
        ProcessingHistoryEntity::class,
        PdfAnnotationEntity::class,
        SavedSignatureEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PdflyDatabase : RoomDatabase() {
    abstract fun pdflyDao(): PdflyDao

    companion object {
        @Volatile
        private var INSTANCE: PdflyDatabase? = null

        fun getInstance(context: Context): PdflyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PdflyDatabase::class.java,
                    "pdfly_local_studio.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
