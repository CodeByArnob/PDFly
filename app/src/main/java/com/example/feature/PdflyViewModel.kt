package com.example.feature

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.filemanager.LocalFileManager
import com.example.core.processing.PdfProcessingEngine
import com.example.core.processing.ProcessingOperationResult
import com.example.data.local.PdfAnnotationEntity
import com.example.data.local.PdfDocumentEntity
import com.example.data.local.PdflyDatabase
import com.example.data.local.ProcessingHistoryEntity
import com.example.data.local.SavedSignatureEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.UserPreferencesState
import com.example.data.repository.PdflyRepository
import com.example.domain.model.AnnotationType
import com.example.domain.model.AppThemeMode
import com.example.domain.model.CompressionPreset
import com.example.domain.model.ExportedImageItem
import com.example.domain.model.ImageExportFormat
import com.example.domain.model.PageMarginPreset
import com.example.domain.model.PageNumberConfig
import com.example.domain.model.PageOrganizeItem
import com.example.domain.model.PageOrientation
import com.example.domain.model.PaperSizePreset
import com.example.domain.model.PdfToolType
import com.example.domain.model.ScanFilterPreset
import com.example.domain.model.ScannedPageDraft
import com.example.domain.model.SplitMode
import com.example.domain.model.WatermarkConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class TopLevelDestination {
    DASHBOARD,
    ALL_TOOLS,
    DOCUMENTS,
    HISTORY,
    SUPPORT,
    SETTINGS,
    PRIVACY_POLICY,
    ABOUT,
    TOOL_READER,
    TOOL_MERGE,
    TOOL_SPLIT,
    TOOL_ORGANIZE_PAGES,
    TOOL_COMPRESS,
    TOOL_IMAGE_TO_PDF,
    TOOL_PDF_TO_IMAGE,
    TOOL_EXTRACT_TEXT,
    TOOL_EXTRACT_IMAGES,
    TOOL_SCANNER,
    TOOL_WATERMARK,
    TOOL_PAGE_NUMBERS,
    TOOL_ANNOTATE,
    TOOL_SIGNATURE,
    TOOL_SECURITY_LOCK_UNLOCK,
    TOOL_METADATA_VIEWER
}

data class ActiveProcessingState(
    val isRunning: Boolean = false,
    val operationTitle: String = "",
    val progress: Float = 0f,
    val statusDetail: String = "",
    val completedResult: ProcessingOperationResult? = null,
    val completedDoc: PdfDocumentEntity? = null
)

class PdflyViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = PdflyDatabase.getInstance(context)
    val repository = PdflyRepository(context, database.pdflyDao())
    private val preferencesRepo = UserPreferencesRepository(context)

    val preferencesState: StateFlow<UserPreferencesState> = preferencesRepo.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesState())

    val allDocuments: StateFlow<List<PdfDocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDocuments: StateFlow<List<PdfDocumentEntity>> = repository.favoriteDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val processingHistory: StateFlow<List<ProcessingHistoryEntity>> = repository.processingHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedSignatures: StateFlow<List<SavedSignatureEntity>> = repository.savedSignatures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentDestination = MutableStateFlow(TopLevelDestination.DASHBOARD)
    val currentDestination: StateFlow<TopLevelDestination> = _currentDestination.asStateFlow()

    private val navigationBackStack = ArrayDeque<TopLevelDestination>()

    private val _selectedDocument = MutableStateFlow<PdfDocumentEntity?>(null)
    val selectedDocument: StateFlow<PdfDocumentEntity?> = _selectedDocument.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Merge PDF state
    private val _mergeQueue = MutableStateFlow<List<PdfDocumentEntity>>(emptyList())
    val mergeQueue: StateFlow<List<PdfDocumentEntity>> = _mergeQueue.asStateFlow()

    // Scanner & Image-to-PDF drafts state
    private val _scannedPages = MutableStateFlow<List<ScannedPageDraft>>(
        listOf(
            ScannedPageDraft(
                id = "scan-page-1",
                title = "Scanned Contract Receipt #01",
                sourceType = "SCANNER",
                filter = ScanFilterPreset.MAGIC_ENHANCE,
                contentLines = listOf(
                    "Vendor: Acme Precision Optics Ltd.",
                    "Invoice Date: October 30, 2026 • Ref #INV-4092",
                    "Item: Portable Document Scanner Calibration Kit",
                    "Amount Paid: $384.00 USD (Verified Local Scan)",
                    "Status: Approved for Local Archive"
                )
            ),
            ScannedPageDraft(
                id = "scan-page-2",
                title = "Field Inspection Notes #02",
                sourceType = "SCANNER",
                filter = ScanFilterPreset.BW_SHARP,
                contentLines = listOf(
                    "Site Inspection Checklist — Section B",
                    "1. Structural Frame Alignment: PASS (99.4%)",
                    "2. Emergency Power Backup Verification: PASS",
                    "3. Optical Sensor Calibration Check: PASS",
                    "Inspector Sign-off: Verified on Device"
                )
            )
        )
    )
    val scannedPages: StateFlow<List<ScannedPageDraft>> = _scannedPages.asStateFlow()

    // Exported images from PDF to JPG/PNG & Extract Images
    private val _exportedImages = MutableStateFlow<List<ExportedImageItem>>(emptyList())
    val exportedImages: StateFlow<List<ExportedImageItem>> = _exportedImages.asStateFlow()

    // Processing execution state & cancellation Job
    private val _processingState = MutableStateFlow(ActiveProcessingState())
    val processingState: StateFlow<ActiveProcessingState> = _processingState.asStateFlow()
    private var activeProcessingJob: Job? = null

    // File pending SAF export
    private val _pendingSafExportFile = MutableStateFlow<File?>(null)
    val pendingSafExportFile: StateFlow<File?> = _pendingSafExportFile.asStateFlow()

    // Startup readiness & Cinematic Launch Experience state (Cold Launch vs Warm Resume)
    private val _isInitializationComplete = MutableStateFlow(false)
    val isInitializationComplete: StateFlow<Boolean> = _isInitializationComplete.asStateFlow()

    private val _showCinematicIntro = MutableStateFlow(true)
    val showCinematicIntro: StateFlow<Boolean> = _showCinematicIntro.asStateFlow()

    private val _isSplashPausedForInspection = MutableStateFlow(false)
    val isSplashPausedForInspection: StateFlow<Boolean> = _isSplashPausedForInspection.asStateFlow()

    private val _warmResumeCount = MutableStateFlow(0)
    val warmResumeCount: StateFlow<Int> = _warmResumeCount.asStateFlow()

    private var hasCompletedColdStartIntro: Boolean = false
    private var wasInBackground: Boolean = false

    init {
        viewModelScope.launch {
            runCatching {
                repository.initializeSampleDataIfNeeded()
                repository.pruneMissingDocuments()
                LocalFileManager.cleanTempFiles(context)
            }
            _isInitializationComplete.value = true
        }
    }

    fun completeCinematicIntro() {
        hasCompletedColdStartIntro = true
        _isSplashPausedForInspection.value = false
        _showCinematicIntro.value = false
    }

    fun onAppBackgrounded() {
        if (hasCompletedColdStartIntro) {
            wasInBackground = true
        }
    }

    fun onAppResumed() {
        if (hasCompletedColdStartIntro && wasInBackground) {
            wasInBackground = false
            // Do not replay the full cold-start intro on warm resume; increment fast-resume counter instead
            _showCinematicIntro.value = false
            _warmResumeCount.update { it + 1 }
        }
    }

    fun replayCinematicIntro(holdForInspection: Boolean = false) {
        _isSplashPausedForInspection.value = holdForInspection
        _showCinematicIntro.value = true
    }

    fun navigateTo(destination: TopLevelDestination) {
        val current = _currentDestination.value
        if (current != destination) {
            navigationBackStack.addLast(current)
            _currentDestination.value = destination
        }
    }

    fun navigateBack(): Boolean {
        return if (navigationBackStack.isNotEmpty()) {
            _currentDestination.value = navigationBackStack.removeLast()
            true
        } else if (_currentDestination.value != TopLevelDestination.DASHBOARD) {
            _currentDestination.value = TopLevelDestination.DASHBOARD
            true
        } else {
            false
        }
    }

    fun openTool(tool: PdfToolType, preselectedDoc: PdfDocumentEntity? = null) {
        val docToUse = preselectedDoc ?: _selectedDocument.value ?: allDocuments.value.firstOrNull()
        _selectedDocument.value = docToUse
        when (tool) {
            PdfToolType.READER -> navigateTo(TopLevelDestination.TOOL_READER)
            PdfToolType.MERGE -> {
                if (_mergeQueue.value.isEmpty() && allDocuments.value.size >= 2) {
                    _mergeQueue.value = allDocuments.value.take(2)
                }
                navigateTo(TopLevelDestination.TOOL_MERGE)
            }
            PdfToolType.SPLIT -> navigateTo(TopLevelDestination.TOOL_SPLIT)
            PdfToolType.ORGANIZE_PAGES -> navigateTo(TopLevelDestination.TOOL_ORGANIZE_PAGES)
            PdfToolType.COMPRESS -> navigateTo(TopLevelDestination.TOOL_COMPRESS)
            PdfToolType.IMAGE_TO_PDF -> navigateTo(TopLevelDestination.TOOL_IMAGE_TO_PDF)
            PdfToolType.PDF_TO_IMAGE -> navigateTo(TopLevelDestination.TOOL_PDF_TO_IMAGE)
            PdfToolType.EXTRACT_TEXT -> navigateTo(TopLevelDestination.TOOL_EXTRACT_TEXT)
            PdfToolType.EXTRACT_IMAGES -> navigateTo(TopLevelDestination.TOOL_EXTRACT_IMAGES)
            PdfToolType.SCANNER -> navigateTo(TopLevelDestination.TOOL_SCANNER)
            PdfToolType.WATERMARK -> navigateTo(TopLevelDestination.TOOL_WATERMARK)
            PdfToolType.PAGE_NUMBERS -> navigateTo(TopLevelDestination.TOOL_PAGE_NUMBERS)
            PdfToolType.ANNOTATE -> navigateTo(TopLevelDestination.TOOL_ANNOTATE)
            PdfToolType.SIGNATURE -> navigateTo(TopLevelDestination.TOOL_SIGNATURE)
            PdfToolType.SECURITY_LOCK_UNLOCK -> navigateTo(TopLevelDestination.TOOL_SECURITY_LOCK_UNLOCK)
            PdfToolType.METADATA_VIEWER -> navigateTo(TopLevelDestination.TOOL_METADATA_VIEWER)
        }
    }

    fun openDocumentInReader(doc: PdfDocumentEntity) {
        if (!File(doc.filePath).exists()) {
            showMessage("File '${doc.title}' was moved or deleted from local storage.")
            viewModelScope.launch { repository.pruneMissingDocuments() }
            return
        }
        _selectedDocument.value = doc
        navigateTo(TopLevelDestination.TOOL_READER)
    }

    fun selectDocumentForTool(doc: PdfDocumentEntity) {
        if (!File(doc.filePath).exists()) {
            showMessage("File '${doc.title}' was moved or deleted from local storage.")
            viewModelScope.launch { repository.pruneMissingDocuments() }
            return
        }
        _selectedDocument.value = doc
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun importPdfFromSaf(uri: Uri, openImmediately: Boolean = false) {
        viewModelScope.launch {
            val result = repository.importExternalPdfUri(uri)
            result.onSuccess { entity ->
                _selectedDocument.value = entity
                if (_currentDestination.value == TopLevelDestination.TOOL_MERGE) {
                    addDocumentToMergeQueue(entity)
                } else if (openImmediately) {
                    navigateTo(TopLevelDestination.TOOL_READER)
                }
                showMessage("Imported '${entity.title}' (${entity.pageCount} pages)")
            }.onFailure { err ->
                showMessage(err.message ?: "Could not import PDF.")
            }
        }
    }

    fun requestSafExport(file: File) {
        if (!file.exists()) {
            showMessage("File not found on local storage.")
            return
        }
        _pendingSafExportFile.value = file
    }

    fun completeSafExport(targetUri: Uri?) {
        val source = _pendingSafExportFile.value
        _pendingSafExportFile.value = null
        if (source == null || targetUri == null) return
        viewModelScope.launch {
            val ok = LocalFileManager.exportLocalFileToSafUri(context, source, targetUri)
            if (ok) {
                showMessage("Saved '${source.name}' to selected device location.")
            } else {
                showMessage("Failed to export file to selected location.")
            }
        }
    }

    fun shareFile(file: File) {
        LocalFileManager.sharePdfFile(context, file)
    }

    fun toggleFavorite(doc: PdfDocumentEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(doc)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = doc.copy(isFavorite = !doc.isFavorite)
            }
        }
    }

    fun toggleBookmark(doc: PdfDocumentEntity, pageIndex: Int) {
        viewModelScope.launch {
            val updated = repository.togglePageBookmark(doc, pageIndex)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = updated
            }
            val action = if (updated.bookmarkedPages.contains(pageIndex)) "Bookmarked" else "Removed bookmark on"
            showMessage("$action page ${pageIndex + 1}")
        }
    }

    fun updatePageAndRotation(doc: PdfDocumentEntity, pageIndex: Int, rotationDegrees: Int) {
        viewModelScope.launch {
            repository.updateReadingProgress(doc, pageIndex, rotationDegrees)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = doc.copy(
                    lastReadPage = pageIndex,
                    rotationDegrees = rotationDegrees
                )
            }
        }
    }

    fun renameDocument(doc: PdfDocumentEntity, newName: String) {
        viewModelScope.launch {
            repository.renameDocument(doc, newName).onSuccess { updated ->
                if (_selectedDocument.value?.id == doc.id) {
                    _selectedDocument.value = updated
                }
                _mergeQueue.update { list ->
                    list.map { if (it.id == doc.id) updated else it }
                }
                showMessage("Renamed to ${updated.title}")
            }.onFailure { err ->
                showMessage(err.message ?: "Could not rename document.")
            }
        }
    }

    fun deleteDocument(doc: PdfDocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            _mergeQueue.update { list -> list.filterNot { it.id == doc.id } }
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = allDocuments.value.firstOrNull { it.id != doc.id }
            }
            showMessage("Removed '${doc.title}' from workspace.")
        }
    }

    // --- Merge Queue Controls ---
    fun addDocumentToMergeQueue(doc: PdfDocumentEntity) {
        _mergeQueue.update { current ->
            if (current.any { it.id == doc.id }) current else current + doc
        }
    }

    fun removeDocumentFromMergeQueue(docId: Long) {
        _mergeQueue.update { current -> current.filterNot { it.id == docId } }
    }

    fun moveMergeQueueItem(fromIndex: Int, toIndex: Int) {
        _mergeQueue.update { current ->
            if (fromIndex !in current.indices || toIndex !in current.indices) return@update current
            val mutable = current.toMutableList()
            val item = mutable.removeAt(fromIndex)
            mutable.add(toIndex, item)
            mutable
        }
    }

    // --- Processing Operations ---
    fun cancelActiveProcessing() {
        activeProcessingJob?.cancel()
        activeProcessingJob = null
        _processingState.value = ActiveProcessingState(
            isRunning = false,
            operationTitle = "Operation Cancelled",
            statusDetail = "Processing was safely cancelled and temporary files were cleaned up."
        )
        LocalFileManager.cleanTempFiles(context)
        showMessage("Processing cancelled safely.")
    }

    fun dismissProcessingResult() {
        _processingState.value = ActiveProcessingState()
    }

    fun runMergePdfs(outputFileName: String) {
        val docs = _mergeQueue.value
        if (docs.size < 2) {
            showMessage("Select at least 2 PDF files to merge.")
            return
        }
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = "Merging ${docs.size} PDF Documents",
                progress = 0.05f,
                statusDetail = "Validating PDF headers and page streams..."
            )
            try {
                val files = docs.map { File(it.filePath) }
                val res = PdfProcessingEngine.mergePdfs(
                    context = context,
                    inputFiles = files,
                    outputFileName = outputFileName
                ) { prog, status ->
                    _processingState.update {
                        it.copy(progress = prog, statusDetail = status)
                    }
                }

                var registered: PdfDocumentEntity? = null
                if (res.success && res.primaryOutputFile != null) {
                    registered = repository.registerGeneratedPdfFile(
                        file = res.primaryOutputFile,
                        pageCount = res.totalPageCount,
                        categoryTag = "Merged",
                        searchableSummary = "Merged from ${docs.joinToString { it.title }}"
                    )
                    _selectedDocument.value = registered
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = "MERGE",
                        inputSummary = "${docs.size} files (${docs.joinToString { it.title }})",
                        outputFileName = res.primaryOutputFile?.name ?: outputFileName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) "Merge Completed" else "Merge Failed",
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = registered
                )
            } catch (_: CancellationException) {
                // Handled in cancelActiveProcessing
            }
        }
    }

    fun runSplitPdf(
        doc: PdfDocumentEntity,
        splitMode: SplitMode,
        rangeInput: String,
        outputBaseName: String
    ) {
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = "Splitting ${doc.title}",
                progress = 0.05f,
                statusDetail = "Validating page ranges..."
            )
            try {
                val res = PdfProcessingEngine.splitPdf(
                    context = context,
                    sourceFile = File(doc.filePath),
                    splitMode = splitMode,
                    rangeInput = rangeInput,
                    outputBaseName = outputBaseName
                ) { prog, status ->
                    _processingState.update { it.copy(progress = prog, statusDetail = status) }
                }

                var primaryDoc: PdfDocumentEntity? = null
                if (res.success) {
                    res.outputFiles.forEachIndexed { idx, outFile ->
                        val inspection = PdfProcessingEngine.inspectPdfFile(outFile)
                        val count = (inspection as? com.example.core.processing.PdfInspectionResult.Valid)?.pageCount ?: 1
                        val entity = repository.registerGeneratedPdfFile(
                            file = outFile,
                            pageCount = count,
                            categoryTag = "Split",
                            searchableSummary = "Split from ${doc.title} (${splitMode.label})"
                        )
                        if (idx == 0) primaryDoc = entity
                    }
                    if (primaryDoc != null) _selectedDocument.value = primaryDoc
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = "SPLIT",
                        inputSummary = "${doc.title} (${splitMode.label}: $rangeInput)",
                        outputFileName = res.primaryOutputFile?.name ?: outputBaseName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) "Split Completed" else "Split Failed",
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = primaryDoc
                )
            } catch (_: CancellationException) {
                // Handled in cancelActiveProcessing
            }
        }
    }

    fun runCompressPdf(
        doc: PdfDocumentEntity,
        preset: CompressionPreset,
        outputFileName: String
    ) {
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = "Compressing ${doc.title}",
                progress = 0.05f,
                statusDetail = "Applying ${preset.label}..."
            )
            try {
                val res = PdfProcessingEngine.compressPdf(
                    context = context,
                    sourceFile = File(doc.filePath),
                    preset = preset,
                    outputFileName = outputFileName
                ) { prog, status ->
                    _processingState.update { it.copy(progress = prog, statusDetail = status) }
                }

                var registered: PdfDocumentEntity? = null
                if (res.success && res.primaryOutputFile != null) {
                    registered = repository.registerGeneratedPdfFile(
                        file = res.primaryOutputFile,
                        pageCount = res.totalPageCount,
                        categoryTag = "Compressed",
                        searchableSummary = "Compressed (${preset.name}) from ${doc.title}"
                    )
                    _selectedDocument.value = registered
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = "COMPRESS",
                        inputSummary = "${doc.title} • ${preset.label}",
                        outputFileName = res.primaryOutputFile?.name ?: outputFileName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) "Compression Finished" else "Compression Failed",
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = registered
                )
            } catch (_: CancellationException) {
                // Handled
            }
        }
    }

    // --- Scanner & Image-to-PDF Controls ---
    fun addScannedPageFromCameraOrTemplate(
        title: String,
        bitmapPath: String? = null,
        filter: ScanFilterPreset = ScanFilterPreset.MAGIC_ENHANCE
    ) {
        val index = _scannedPages.value.size + 1
        val draft = ScannedPageDraft(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Scanned Page #$index" },
            sourceType = if (bitmapPath != null) "CAMERA" else "SCANNER",
            filter = filter,
            bitmapPath = bitmapPath,
            contentLines = listOf(
                "Captured Document Page #$index",
                "Timestamp: ${LocalFileManager.formatTimestamp(System.currentTimeMillis())}",
                "Auto-Edge Perspective & Shadow Cleanup Applied",
                "Processed 100% Offline on Android Device"
            )
        )
        _scannedPages.update { it + draft }
        showMessage("Added '${draft.title}' to batch (${_scannedPages.value.size} pages)")
    }

    fun importImagesForPdf(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val added = uris.mapIndexed { idx, uri ->
            ScannedPageDraft(
                id = UUID.randomUUID().toString(),
                title = "Selected Image #${_scannedPages.value.size + idx + 1}",
                sourceType = "GALLERY",
                filter = ScanFilterPreset.ORIGINAL,
                bitmapPath = uri.toString()
            )
        }
        _scannedPages.update { it + added }
        showMessage("Added ${added.size} image(s) to queue.")
    }

    fun updateScannedPageDraft(updated: ScannedPageDraft) {
        _scannedPages.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
    }

    fun removeScannedPageDraft(id: String) {
        _scannedPages.update { list -> list.filterNot { it.id == id } }
    }

    fun reorderScannedPageDraft(fromIdx: Int, toIdx: Int) {
        _scannedPages.update { list ->
            if (fromIdx !in list.indices || toIdx !in list.indices) return@update list
            val mutable = list.toMutableList()
            val item = mutable.removeAt(fromIdx)
            mutable.add(toIdx, item)
            mutable
        }
    }

    fun exportScannedOrImagesToPdf(
        paperSize: PaperSizePreset,
        orientation: PageOrientation,
        marginPreset: PageMarginPreset,
        imageQualityPercent: Int,
        outputFileName: String,
        operationTag: String = "SCAN_TO_PDF"
    ) {
        val pages = _scannedPages.value
        if (pages.isEmpty()) {
            showMessage("Add at least 1 page or image before exporting.")
            return
        }
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = "Generating Multi-Page PDF",
                progress = 0.08f,
                statusDetail = "Formatting ${pages.size} page(s) to ${paperSize.label}..."
            )
            try {
                val res = PdfProcessingEngine.createPdfFromScannedPages(
                    context = context,
                    pages = pages,
                    paperSize = paperSize,
                    orientation = orientation,
                    marginPreset = marginPreset,
                    imageQualityPercent = imageQualityPercent,
                    outputFileName = outputFileName
                ) { prog, status ->
                    _processingState.update { it.copy(progress = prog, statusDetail = status) }
                }

                var registered: PdfDocumentEntity? = null
                if (res.success && res.primaryOutputFile != null) {
                    registered = repository.registerGeneratedPdfFile(
                        file = res.primaryOutputFile,
                        pageCount = res.totalPageCount,
                        categoryTag = if (operationTag == "IMAGE_TO_PDF") "Images" else "Scanned",
                        searchableSummary = pages.joinToString(" ") { it.title + " " + it.contentLines.joinToString(" ") }
                    )
                    _selectedDocument.value = registered
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = operationTag,
                        inputSummary = "${pages.size} pages • ${paperSize.label} • ${orientation.label}",
                        outputFileName = res.primaryOutputFile?.name ?: outputFileName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) "PDF Export Ready" else "Export Failed",
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = registered
                )
            } catch (_: CancellationException) {
                // Handled
            }
        }
    }

    // --- Annotations & Signatures ---
    fun addAnnotationToDocument(
        documentId: Long,
        pageIndex: Int,
        type: AnnotationType,
        textContent: String = "",
        colorHex: Long = 0xFFFBBF24,
        normX: Float = 0.15f,
        normY: Float = 0.25f,
        normW: Float = 0.55f,
        normH: Float = 0.08f,
        pointsSerialized: String = ""
    ) {
        viewModelScope.launch {
            repository.addAnnotation(
                PdfAnnotationEntity(
                    documentId = documentId,
                    pageIndex = pageIndex,
                    annotationType = type.name,
                    textContent = textContent,
                    colorHex = colorHex,
                    normalizedX = normX,
                    normalizedY = normY,
                    normalizedWidth = normW,
                    normalizedHeight = normH,
                    pointsSerialized = pointsSerialized
                )
            )
            showMessage("Added ${type.label} on Page ${pageIndex + 1}")
        }
    }

    fun deleteAnnotation(annotationId: Long) {
        viewModelScope.launch {
            repository.removeAnnotation(annotationId)
            showMessage("Removed annotation.")
        }
    }

    fun createNewSignature(label: String, inkColorHex: Long, strokesSerialized: String) {
        if (strokesSerialized.isBlank()) {
            showMessage("Draw your signature on the pad first.")
            return
        }
        viewModelScope.launch {
            repository.saveSignature(label, inkColorHex, strokesSerialized)
            showMessage("Saved signature '${label.ifBlank { "Custom Signature" }}'")
        }
    }

    fun deleteSavedSignature(id: Long) {
        viewModelScope.launch {
            repository.deleteSignature(id)
            showMessage("Deleted saved signature.")
        }
    }

    fun flattenAndExportAnnotatedPdf(doc: PdfDocumentEntity, outputFileName: String) {
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            val anns = repository.getAnnotationsSync(doc.id)
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = "Saving Annotated & Signed PDF",
                progress = 0.1f,
                statusDetail = "Burning ${anns.size} markups onto PDF pages..."
            )
            try {
                val res = PdfProcessingEngine.flattenAnnotationsToNewPdf(
                    context = context,
                    sourceFile = File(doc.filePath),
                    annotations = anns,
                    outputFileName = outputFileName
                ) { prog, status ->
                    _processingState.update { it.copy(progress = prog, statusDetail = status) }
                }

                var registered: PdfDocumentEntity? = null
                if (res.success && res.primaryOutputFile != null) {
                    registered = repository.registerGeneratedPdfFile(
                        file = res.primaryOutputFile,
                        pageCount = res.totalPageCount,
                        categoryTag = "Signed & Annotated",
                        searchableSummary = "${doc.searchableSummary} Annotated Signed"
                    )
                    _selectedDocument.value = registered
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = "ANNOTATE_SIGN",
                        inputSummary = "${doc.title} (${anns.size} annotations/signatures)",
                        outputFileName = res.primaryOutputFile?.name ?: outputFileName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) "Annotated PDF Saved" else "Export Failed",
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = registered
                )
            } catch (_: CancellationException) {
                // Handled
            }
        }
    }

    // --- Settings & Preferences ---
    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { preferencesRepo.setThemeMode(mode) }
    }

    fun setDefaultCompression(preset: CompressionPreset) {
        viewModelScope.launch { preferencesRepo.setDefaultCompression(preset) }
    }

    fun setDefaultPaperSize(preset: PaperSizePreset) {
        viewModelScope.launch { preferencesRepo.setDefaultPaperSize(preset) }
    }

    fun setReducedMotionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setReducedMotionEnabled(enabled)
            showMessage(if (enabled) "Reduced motion enabled" else "Cinematic motion enabled")
        }
    }

    fun clearProcessingHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showMessage("Cleared processing history.")
        }
    }

    fun cleanTempCacheNow() {
        val count = LocalFileManager.cleanTempFiles(context)
        showMessage("Cleaned temporary cache ($count temporary files removed).")
    }

    // --- New Toolkit Operations (Organize Pages, Watermark, Page Numbers, PDF to Image, Security) ---
    fun runOrganizePdfPages(
        doc: PdfDocumentEntity,
        items: List<PageOrganizeItem>,
        extractSelectedOnly: Boolean,
        outputFileName: String
    ) {
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = "Organizing Pages in ${doc.title}",
                progress = 0.08f,
                statusDetail = "Reordering and rotating selected pages..."
            )
            try {
                val res = PdfProcessingEngine.organizePdfPages(
                    context = context,
                    sourceFile = File(doc.filePath),
                    organizedItems = items,
                    extractSelectedOnly = extractSelectedOnly,
                    outputFileName = outputFileName
                ) { prog, status ->
                    _processingState.update { it.copy(progress = prog, statusDetail = status) }
                }

                var registered: PdfDocumentEntity? = null
                if (res.success && res.primaryOutputFile != null) {
                    registered = repository.registerGeneratedPdfFile(
                        file = res.primaryOutputFile,
                        pageCount = res.totalPageCount,
                        categoryTag = "Organized",
                        searchableSummary = "Organized from ${doc.title}"
                    )
                    _selectedDocument.value = registered
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = "ORGANIZE_PAGES",
                        inputSummary = "${doc.title} (${res.totalPageCount} active pages)",
                        outputFileName = res.primaryOutputFile?.name ?: outputFileName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) "Pages Organized" else "Organization Failed",
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = registered
                )
            } catch (_: CancellationException) {
                // Handled
            }
        }
    }

    fun runWatermarkPdf(
        doc: PdfDocumentEntity,
        config: WatermarkConfig,
        outputFileName: String
    ) {
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = "Stamping Watermark on ${doc.title}",
                progress = 0.08f,
                statusDetail = "Applying '${config.text}'..."
            )
            try {
                val res = PdfProcessingEngine.applyWatermarkToPdf(
                    context = context,
                    sourceFile = File(doc.filePath),
                    config = config,
                    outputFileName = outputFileName
                ) { prog, status ->
                    _processingState.update { it.copy(progress = prog, statusDetail = status) }
                }

                var registered: PdfDocumentEntity? = null
                if (res.success && res.primaryOutputFile != null) {
                    registered = repository.registerGeneratedPdfFile(
                        file = res.primaryOutputFile,
                        pageCount = res.totalPageCount,
                        categoryTag = "Watermarked",
                        searchableSummary = "Watermarked (${config.text}) from ${doc.title}"
                    )
                    _selectedDocument.value = registered
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = "WATERMARK",
                        inputSummary = "${doc.title} • '${config.text}'",
                        outputFileName = res.primaryOutputFile?.name ?: outputFileName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) "Watermark Applied" else "Watermark Failed",
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = registered
                )
            } catch (_: CancellationException) {
                // Handled
            }
        }
    }

    fun runPageNumbersPdf(
        doc: PdfDocumentEntity,
        config: PageNumberConfig,
        outputFileName: String
    ) {
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = "Stamping Page Numbers on ${doc.title}",
                progress = 0.08f,
                statusDetail = "Formatting ${config.format.label}..."
            )
            try {
                val res = PdfProcessingEngine.addPageNumbersToPdf(
                    context = context,
                    sourceFile = File(doc.filePath),
                    config = config,
                    outputFileName = outputFileName
                ) { prog, status ->
                    _processingState.update { it.copy(progress = prog, statusDetail = status) }
                }

                var registered: PdfDocumentEntity? = null
                if (res.success && res.primaryOutputFile != null) {
                    registered = repository.registerGeneratedPdfFile(
                        file = res.primaryOutputFile,
                        pageCount = res.totalPageCount,
                        categoryTag = "Numbered",
                        searchableSummary = "Page numbered (${config.format.label}) from ${doc.title}"
                    )
                    _selectedDocument.value = registered
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = "PAGE_NUMBERS",
                        inputSummary = "${doc.title} • ${config.format.label}",
                        outputFileName = res.primaryOutputFile?.name ?: outputFileName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) "Page Numbers Added" else "Numbering Failed",
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = registered
                )
            } catch (_: CancellationException) {
                // Handled
            }
        }
    }

    fun runConvertPdfToImages(
        doc: PdfDocumentEntity,
        format: ImageExportFormat,
        qualityPercent: Int,
        baseFileName: String
    ) {
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = "Exporting ${doc.title} to ${format.extension.uppercase()}",
                progress = 0.08f,
                statusDetail = "Rendering high-resolution page bitmaps..."
            )
            try {
                val (res, images) = PdfProcessingEngine.convertPdfToImages(
                    context = context,
                    sourceFile = File(doc.filePath),
                    format = format,
                    qualityPercent = qualityPercent,
                    baseFileName = baseFileName
                ) { prog, status ->
                    _processingState.update { it.copy(progress = prog, statusDetail = status) }
                }

                if (res.success) {
                    _exportedImages.value = images
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = "PDF_TO_IMAGE",
                        inputSummary = "${doc.title} (${format.extension.uppercase()})",
                        outputFileName = res.primaryOutputFile?.name ?: baseFileName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) "Images Exported" else "Image Export Failed",
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = null
                )
            } catch (_: CancellationException) {
                // Handled
            }
        }
    }

    fun runSecurityAction(
        doc: PdfDocumentEntity,
        password: String,
        unlockMode: Boolean,
        outputFileName: String
    ) {
        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            _processingState.value = ActiveProcessingState(
                isRunning = true,
                operationTitle = if (unlockMode) "Unlocking ${doc.title}" else "Protecting ${doc.title}",
                progress = 0.1f,
                statusDetail = if (unlockMode) "Verifying password hash..." else "Applying SHA-256 access seal..."
            )
            try {
                val res = PdfProcessingEngine.protectOrUnlockPdf(
                    context = context,
                    sourceFile = File(doc.filePath),
                    password = password,
                    unlockMode = unlockMode,
                    outputFileName = outputFileName
                ) { prog, status ->
                    _processingState.update { it.copy(progress = prog, statusDetail = status) }
                }

                var registered: PdfDocumentEntity? = null
                if (res.success && res.primaryOutputFile != null) {
                    registered = repository.registerGeneratedPdfFile(
                        file = res.primaryOutputFile,
                        pageCount = res.totalPageCount,
                        categoryTag = if (unlockMode) "Unlocked" else "Protected",
                        searchableSummary = "${doc.title} ${if (unlockMode) "Unlocked" else "Protected"}"
                    )
                    _selectedDocument.value = registered
                }

                repository.recordHistory(
                    ProcessingHistoryEntity(
                        operationType = if (unlockMode) "UNLOCK_PDF" else "PROTECT_PDF",
                        inputSummary = doc.title,
                        outputFileName = res.primaryOutputFile?.name ?: outputFileName,
                        outputFilePath = res.primaryOutputFile?.absolutePath ?: "",
                        inputSizeBytes = res.inputSizeBytes,
                        outputSizeBytes = res.outputSizeBytes,
                        pageCount = res.totalPageCount,
                        status = if (res.success) "SUCCESS" else "FAILED",
                        detailsMessage = res.message
                    )
                )

                _processingState.value = ActiveProcessingState(
                    isRunning = false,
                    operationTitle = if (res.success) {
                        if (unlockMode) "PDF Unlocked" else "PDF Protected"
                    } else {
                        "Security Operation Failed"
                    },
                    progress = 1f,
                    statusDetail = res.message,
                    completedResult = res,
                    completedDoc = registered
                )
            } catch (_: CancellationException) {
                // Handled
            }
        }
    }
}
