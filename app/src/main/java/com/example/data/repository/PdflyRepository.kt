package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.core.filemanager.LocalFileManager
import com.example.core.processing.PdfInspectionResult
import com.example.core.processing.PdfProcessingEngine
import com.example.data.local.PdfAnnotationEntity
import com.example.data.local.PdfDocumentEntity
import com.example.data.local.PdflyDao
import com.example.data.local.ProcessingHistoryEntity
import com.example.data.local.SavedSignatureEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class PdflyRepository(
    private val context: Context,
    private val dao: PdflyDao
) {
    val allDocuments: Flow<List<PdfDocumentEntity>> = dao.observeAllDocuments()
    val favoriteDocuments: Flow<List<PdfDocumentEntity>> = dao.observeFavoriteDocuments()
    val processingHistory: Flow<List<ProcessingHistoryEntity>> = dao.observeProcessingHistory()
    val savedSignatures: Flow<List<SavedSignatureEntity>> = dao.observeSavedSignatures()

    fun observeAnnotations(documentId: Long): Flow<List<PdfAnnotationEntity>> =
        dao.observeAnnotationsForDocument(documentId)

    suspend fun getAnnotationsSync(documentId: Long): List<PdfAnnotationEntity> =
        dao.getAnnotationsForDocumentSync(documentId)

    suspend fun initializeSampleDataIfNeeded() = withContext(Dispatchers.IO) {
        if (dao.getDocumentCount() == 0) {
            val (docs, defaultSignature) = LocalFileManager.seedInitialSampleDocumentsIfNeeded(context)
            docs.forEach { dao.insertDocument(it) }
            dao.insertSignature(defaultSignature)
            dao.insertHistory(
                ProcessingHistoryEntity(
                    operationType = "WORKSPACE_INIT",
                    inputSummary = "3 Built-In Reference PDFs",
                    outputFileName = "PDFly_Architecture_Handbook.pdf",
                    outputFilePath = docs.first().filePath,
                    inputSizeBytes = docs.sumOf { it.sizeBytes },
                    outputSizeBytes = docs.sumOf { it.sizeBytes },
                    pageCount = docs.sumOf { it.pageCount },
                    status = "SUCCESS",
                    detailsMessage = "Initialized local offline workspace with 3 sample PDF documents."
                )
            )
        }
    }

    suspend fun importExternalPdfUri(uri: Uri): Result<PdfDocumentEntity> = withContext(Dispatchers.IO) {
        val importedFile = LocalFileManager.importUriToLocalDocuments(context, uri)
            ?: return@withContext Result.failure(IllegalArgumentException("Unable to read selected file from Storage Access Framework."))

        when (val inspection = PdfProcessingEngine.inspectPdfFile(importedFile)) {
            is PdfInspectionResult.Error -> {
                importedFile.delete()
                Result.failure(IllegalArgumentException(inspection.reason))
            }
            is PdfInspectionResult.Valid -> {
                val entity = PdfDocumentEntity(
                    title = importedFile.name,
                    filePath = importedFile.absolutePath,
                    sizeBytes = inspection.fileSizeBytes,
                    pageCount = inspection.pageCount,
                    isFavorite = false,
                    lastOpenedAt = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis(),
                    searchableSummary = "${importedFile.name} Imported PDF Document (${inspection.pageCount} pages)",
                    categoryTag = "Imported"
                )
                val newId = dao.insertDocument(entity)
                Result.success(entity.copy(id = newId))
            }
        }
    }

    suspend fun registerGeneratedPdfFile(
        file: File,
        pageCount: Int,
        categoryTag: String,
        searchableSummary: String
    ): PdfDocumentEntity = withContext(Dispatchers.IO) {
        val entity = PdfDocumentEntity(
            title = file.name,
            filePath = file.absolutePath,
            sizeBytes = file.length(),
            pageCount = pageCount,
            isFavorite = false,
            lastOpenedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            searchableSummary = searchableSummary,
            categoryTag = categoryTag
        )
        val id = dao.insertDocument(entity)
        entity.copy(id = id)
    }

    suspend fun toggleFavorite(doc: PdfDocumentEntity) {
        dao.updateDocument(doc.copy(isFavorite = !doc.isFavorite))
    }

    suspend fun updateReadingProgress(
        doc: PdfDocumentEntity,
        pageIndex: Int,
        rotationDegrees: Int = doc.rotationDegrees
    ) {
        dao.updateDocument(
            doc.copy(
                lastReadPage = pageIndex.coerceIn(0, (doc.pageCount - 1).coerceAtLeast(0)),
                rotationDegrees = rotationDegrees,
                lastOpenedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun togglePageBookmark(doc: PdfDocumentEntity, pageIndex: Int): PdfDocumentEntity {
        val current = doc.bookmarkedPages.toMutableSet()
        if (current.contains(pageIndex)) {
            current.remove(pageIndex)
        } else {
            current.add(pageIndex)
        }
        val updated = doc.copy(bookmarkedPagesCsv = current.sorted().joinToString(","))
        dao.updateDocument(updated)
        return updated
    }

    suspend fun renameDocument(doc: PdfDocumentEntity, newNameRaw: String): Result<PdfDocumentEntity> =
        withContext(Dispatchers.IO) {
            val currentFile = File(doc.filePath)
            if (!currentFile.exists()) {
                return@withContext Result.failure(
                    IllegalStateException("Document file no longer exists on local storage.")
                )
            }
            val safeName = LocalFileManager.sanitizeFileName(newNameRaw, doc.title.removeSuffix(".pdf"))
            val parentDir = currentFile.parentFile ?: LocalFileManager.getDocumentsDir(context)
            val directDest = File(parentDir, safeName)
            val targetFile = when {
                directDest.absolutePath == currentFile.absolutePath -> currentFile
                directDest.exists() -> {
                    val uniqueDest = LocalFileManager.createUniqueFile(parentDir, safeName)
                    if (currentFile.renameTo(uniqueDest)) uniqueDest else currentFile
                }
                currentFile.renameTo(directDest) -> directDest
                else -> currentFile
            }
            val updated = doc.copy(
                title = targetFile.name,
                filePath = targetFile.absolutePath
            )
            dao.updateDocument(updated)
            Result.success(updated)
        }

    suspend fun pruneMissingDocuments(): Int = withContext(Dispatchers.IO) {
        val docs = dao.getDocumentCount()
        if (docs <= 0) return@withContext 0
        var pruned = 0
        for (doc in dao.observeAllDocumentsSnapshot()) {
            if (!File(doc.filePath).exists()) {
                dao.deleteAnnotationsForDocument(doc.id)
                dao.deleteDocumentById(doc.id)
                pruned++
            }
        }
        pruned
    }

    suspend fun deleteDocument(doc: PdfDocumentEntity) = withContext(Dispatchers.IO) {
        val file = File(doc.filePath)
        if (file.exists()) file.delete()
        dao.deleteAnnotationsForDocument(doc.id)
        dao.deleteDocumentById(doc.id)
    }

    suspend fun addAnnotation(annotation: PdfAnnotationEntity): Long =
        dao.insertAnnotation(annotation)

    suspend fun removeAnnotation(annotationId: Long) =
        dao.deleteAnnotationById(annotationId)

    suspend fun saveSignature(label: String, inkColorHex: Long, strokesSerialized: String): Long {
        return dao.insertSignature(
            SavedSignatureEntity(
                label = label.ifBlank { "Custom Signature" },
                inkColorHex = inkColorHex,
                strokesSerialized = strokesSerialized
            )
        )
    }

    suspend fun deleteSignature(id: Long) = dao.deleteSignatureById(id)

    suspend fun recordHistory(entry: ProcessingHistoryEntity) = dao.insertHistory(entry)

    suspend fun clearHistory() = dao.clearHistory()
}
