package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AppThemeMode
import com.example.domain.model.ScanFilterPreset
import com.example.feature.PdflyViewModel
import com.example.feature.TopLevelDestination
import com.example.feature.common.ProcessingStatusModal
import com.example.feature.dashboard.AllPdfToolsCatalogScreen
import com.example.feature.dashboard.DashboardScreen
import com.example.feature.donation.SupportDonationScreen
import com.example.feature.files.DocumentsManagerScreen
import com.example.feature.files.ProcessingHistoryScreen
import com.example.feature.reader.PdfReaderScreen
import com.example.feature.reader.ReaderStudioMode
import com.example.feature.scanner.DocumentScannerScreen
import com.example.feature.settings.AboutScreen
import com.example.feature.settings.PrivacyPolicyScreen
import com.example.feature.settings.SettingsScreen
import com.example.feature.tools.CompressPdfScreen
import com.example.feature.tools.ExtractTextScreen
import com.example.feature.tools.ImageToPdfScreen
import com.example.feature.tools.MergePdfScreen
import com.example.feature.tools.PageNumbersPdfScreen
import com.example.feature.tools.PageOrganizerScreen
import com.example.feature.tools.PdfMetadataViewerScreen
import com.example.feature.tools.PdfSecurityScreen
import com.example.feature.tools.PdfToImageScreen
import com.example.feature.tools.SplitPdfScreen
import com.example.feature.tools.WatermarkPdfScreen
import com.example.feature.splash.PdflyCinematicSplashScreen
import com.example.ui.motion.CinematicAmbientContainer
import com.example.ui.motion.LocalReducedMotion
import com.example.ui.motion.PdflyMotion
import com.example.ui.motion.rememberEffectiveReducedMotion
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.pdflyColors
import kotlinx.coroutines.flow.flowOf

class MainActivity : ComponentActivity() {

    val viewModel: PdflyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                // Immediately remove the system splash overlay so Jetpack Compose's
                // PdflyCinematicSplashScreen takes over seamlessly with zero duplicate splash or flash.
                splashScreenView.remove()
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null && intent?.action == Intent.ACTION_VIEW) {
            intent?.data?.let { uri ->
                viewModel.completeCinematicIntro()
                viewModel.importPdfFromSaf(uri, openImmediately = true)
            }
        }

        setContent {
            val preferences by viewModel.preferencesState.collectAsStateWithLifecycle()
            val darkTheme = when (preferences.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
            val effectiveReducedMotion = rememberEffectiveReducedMotion(preferences.reducedMotionEnabled)

            CompositionLocalProvider(LocalReducedMotion provides effectiveReducedMotion) {
                MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false) {
                    PdflyAppShell(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppResumed()
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            viewModel.onAppBackgrounded()
        }
    }
}

private data class BottomNavSpec(
    val destination: TopLevelDestination,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun PdflyAppShell(viewModel: PdflyViewModel) {
    val currentDest by viewModel.currentDestination.collectAsStateWithLifecycle()
    val allDocs by viewModel.allDocuments.collectAsStateWithLifecycle()
    val favDocs by viewModel.favoriteDocuments.collectAsStateWithLifecycle()
    val history by viewModel.processingHistory.collectAsStateWithLifecycle()
    val savedSignatures by viewModel.savedSignatures.collectAsStateWithLifecycle()
    val selectedDoc by viewModel.selectedDocument.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val mergeQueue by viewModel.mergeQueue.collectAsStateWithLifecycle()
    val scannedPages by viewModel.scannedPages.collectAsStateWithLifecycle()
    val exportedImages by viewModel.exportedImages.collectAsStateWithLifecycle()
    val processingState by viewModel.processingState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferencesState.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val pendingSafExport by viewModel.pendingSafExportFile.collectAsStateWithLifecycle()
    val isInitializationComplete by viewModel.isInitializationComplete.collectAsStateWithLifecycle()
    val showCinematicIntro by viewModel.showCinematicIntro.collectAsStateWithLifecycle()
    val isSplashPausedForInspection by viewModel.isSplashPausedForInspection.collectAsStateWithLifecycle()
    val isReducedMotion = LocalReducedMotion.current
    val pdflyColors = MaterialTheme.pdflyColors

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        val msg = snackbarMsg ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearMessage()
    }

    // SAF OpenDocument Launcher (zero broad storage permissions)
    val safImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importPdfFromSaf(uri, openImmediately = false)
        }
    }

    // SAF CreateDocument Launcher for exporting PDFs safely
    val safExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { targetUri ->
        viewModel.completeSafExport(targetUri)
    }

    LaunchedEffect(pendingSafExport) {
        val file = pendingSafExport ?: return@LaunchedEffect
        safExportLauncher.launch(file.name)
    }

    // Zero-permission Android Photo Picker for Image-to-PDF and Document Scanner
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importImagesForPdf(uris)
        }
    }

    val navItems = remember {
        listOf(
            BottomNavSpec(TopLevelDestination.DASHBOARD, "Dashboard", Icons.Default.Home, "nav_tab_dashboard"),
            BottomNavSpec(TopLevelDestination.DOCUMENTS, "Documents", Icons.Default.Description, "nav_tab_documents"),
            BottomNavSpec(TopLevelDestination.HISTORY, "History", Icons.Default.History, "nav_tab_history"),
            BottomNavSpec(TopLevelDestination.SUPPORT, "Support", Icons.Default.VolunteerActivism, "nav_tab_support"),
            BottomNavSpec(TopLevelDestination.SETTINGS, "Settings", Icons.Default.Settings, "nav_tab_settings")
        )
    }

    val isTopLevelTab = currentDest in listOf(
        TopLevelDestination.DASHBOARD,
        TopLevelDestination.DOCUMENTS,
        TopLevelDestination.HISTORY,
        TopLevelDestination.SUPPORT,
        TopLevelDestination.SETTINGS
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!isExpandedScreen && isTopLevelTab) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = pdflyColors.cardBorder
                        )
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            navItems.forEach { item ->
                                val isSelected = currentDest == item.destination
                                val iconScale by animateFloatAsState(
                                    targetValue = if (isSelected && !isReducedMotion) 1.12f else 1f,
                                    animationSpec = PdflyMotion.tactileSpring(),
                                    label = "nav_icon_scale"
                                )
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.navigateTo(item.destination) },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = iconScale
                                                scaleY = iconScale
                                            }
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen && isTopLevelTab) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            navItems.forEach { item ->
                                val isSelected = currentDest == item.destination
                                val iconScale by animateFloatAsState(
                                    targetValue = if (isSelected && !isReducedMotion) 1.12f else 1f,
                                    animationSpec = PdflyMotion.tactileSpring(),
                                    label = "rail_icon_scale"
                                )
                                NavigationRailItem(
                                    selected = isSelected,
                                    onClick = { viewModel.navigateTo(item.destination) },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = iconScale
                                                scaleY = iconScale
                                            }
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(pdflyColors.cardBorder)
                        )
                    }
                }

                CinematicAmbientContainer(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentDest,
                        transitionSpec = { PdflyMotion.screenTransition(this, isReducedMotion) },
                        label = "pdfly_screen_transition"
                    ) { dest ->
                        when (dest) {
                            TopLevelDestination.DASHBOARD -> {
                                DashboardScreen(
                                    allDocuments = allDocs,
                                    recentDocuments = allDocs,
                                    favoriteDocuments = favDocs,
                                    historyCount = history.size,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = viewModel::updateSearchQuery,
                                    onOpenTool = { tool -> viewModel.openTool(tool) },
                                    onOpenDocumentInReader = viewModel::openDocumentInReader,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onRenameDocument = viewModel::renameDocument,
                                    onShareDocument = viewModel::shareFile,
                                    onExportDocumentSaf = viewModel::requestSafExport,
                                    onDeleteDocument = viewModel::deleteDocument,
                                    onNavigate = viewModel::navigateTo
                                )
                            }

                            TopLevelDestination.ALL_TOOLS -> {
                                AllPdfToolsCatalogScreen(
                                    onOpenTool = { tool -> viewModel.openTool(tool) },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.DOCUMENTS -> {
                                DocumentsManagerScreen(
                                    allDocuments = allDocs,
                                    favoriteDocuments = favDocs,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = viewModel::updateSearchQuery,
                                    onImportFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onOpenDocument = viewModel::openDocumentInReader,
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onRenameDocument = viewModel::renameDocument,
                                    onShareFile = viewModel::shareFile,
                                    onExportSafFile = viewModel::requestSafExport,
                                    onDeleteDocument = viewModel::deleteDocument,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.HISTORY -> {
                                ProcessingHistoryScreen(
                                    historyEntries = history,
                                    onClearHistory = viewModel::clearProcessingHistory,
                                    onShareFile = viewModel::shareFile,
                                    onExportSafFile = viewModel::requestSafExport,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.SUPPORT -> {
                                SupportDonationScreen(
                                    onCopyMessage = viewModel::showMessage,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.SETTINGS -> {
                                SettingsScreen(
                                    preferences = preferences,
                                    onSetThemeMode = viewModel::setThemeMode,
                                    onSetDefaultCompression = viewModel::setDefaultCompression,
                                    onSetDefaultPaperSize = viewModel::setDefaultPaperSize,
                                    onSetReducedMotion = viewModel::setReducedMotionEnabled,
                                    onCleanTempCache = viewModel::cleanTempCacheNow,
                                    onNavigate = viewModel::navigateTo,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.PRIVACY_POLICY -> {
                                PrivacyPolicyScreen(onBack = { viewModel.navigateBack() })
                            }

                            TopLevelDestination.ABOUT -> {
                                AboutScreen(
                                    onOpenSupport = { viewModel.navigateTo(TopLevelDestination.SUPPORT) },
                                    onOpenPrivacy = { viewModel.navigateTo(TopLevelDestination.PRIVACY_POLICY) },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_READER,
                            TopLevelDestination.TOOL_ANNOTATE,
                            TopLevelDestination.TOOL_SIGNATURE -> {
                                val activeDoc = selectedDoc ?: allDocs.firstOrNull()
                                val annotationsFlow = remember(activeDoc?.id) {
                                    if (activeDoc != null) viewModel.repository.observeAnnotations(activeDoc.id)
                                    else flowOf(emptyList())
                                }
                                val studioMode = when (dest) {
                                    TopLevelDestination.TOOL_ANNOTATE -> ReaderStudioMode.ANNOTATE
                                    TopLevelDestination.TOOL_SIGNATURE -> ReaderStudioMode.SIGNATURE
                                    else -> ReaderStudioMode.READER
                                }
                                PdfReaderScreen(
                                    mode = studioMode,
                                    selectedDoc = activeDoc,
                                    allDocs = allDocs,
                                    annotationsFlow = annotationsFlow,
                                    savedSignatures = savedSignatures,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onUpdatePageAndRotation = viewModel::updatePageAndRotation,
                                    onToggleBookmark = viewModel::toggleBookmark,
                                    onAddAnnotation = viewModel::addAnnotationToDocument,
                                    onDeleteAnnotation = viewModel::deleteAnnotation,
                                    onSaveNewSignature = viewModel::createNewSignature,
                                    onDeleteSavedSignature = viewModel::deleteSavedSignature,
                                    onFlattenAndExportPdf = viewModel::flattenAndExportAnnotatedPdf,
                                    onShareFile = viewModel::shareFile,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_MERGE -> {
                                MergePdfScreen(
                                    mergeQueue = mergeQueue,
                                    allDocuments = allDocs,
                                    onAddDocToQueue = viewModel::addDocumentToMergeQueue,
                                    onRemoveFromQueue = viewModel::removeDocumentFromMergeQueue,
                                    onMoveItem = viewModel::moveMergeQueueItem,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onRunMerge = viewModel::runMergePdfs,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_SPLIT -> {
                                SplitPdfScreen(
                                    selectedDoc = selectedDoc ?: allDocs.firstOrNull(),
                                    allDocuments = allDocs,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onRunSplit = viewModel::runSplitPdf,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_COMPRESS -> {
                                CompressPdfScreen(
                                    selectedDoc = selectedDoc ?: allDocs.firstOrNull(),
                                    allDocuments = allDocs,
                                    defaultPreset = preferences.defaultCompression,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onRunCompress = viewModel::runCompressPdf,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_IMAGE_TO_PDF -> {
                                ImageToPdfScreen(
                                    pages = scannedPages,
                                    defaultPaperSize = preferences.defaultPaperSize,
                                    onPickImagesFromPhotoPicker = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onAddSampleGraphicPage = {
                                        viewModel.addScannedPageFromCameraOrTemplate(
                                            title = "High-Res Diagram Sheet #${scannedPages.size + 1}",
                                            bitmapPath = null,
                                            filter = ScanFilterPreset.ORIGINAL
                                        )
                                    },
                                    onReorderPage = viewModel::reorderScannedPageDraft,
                                    onRemovePage = viewModel::removeScannedPageDraft,
                                    onExportPdf = { size, orient, margin, quality, outName ->
                                        viewModel.exportScannedOrImagesToPdf(
                                            paperSize = size,
                                            orientation = orient,
                                            marginPreset = margin,
                                            imageQualityPercent = quality,
                                            outputFileName = outName,
                                            operationTag = "IMAGE_TO_PDF"
                                        )
                                    },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_SCANNER -> {
                                DocumentScannerScreen(
                                    scannedPages = scannedPages,
                                    onCapturePage = viewModel::addScannedPageFromCameraOrTemplate,
                                    onPickFromGallery = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onUpdatePageDraft = viewModel::updateScannedPageDraft,
                                    onRemovePageDraft = viewModel::removeScannedPageDraft,
                                    onReorderPageDraft = viewModel::reorderScannedPageDraft,
                                    onExportScannedPdf = { size, orient, margin, quality, outName ->
                                        viewModel.exportScannedOrImagesToPdf(
                                            paperSize = size,
                                            orientation = orient,
                                            marginPreset = margin,
                                            imageQualityPercent = quality,
                                            outputFileName = outName,
                                            operationTag = "SCAN_TO_PDF"
                                        )
                                    },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_ORGANIZE_PAGES -> {
                                PageOrganizerScreen(
                                    selectedDoc = selectedDoc ?: allDocs.firstOrNull(),
                                    allDocuments = allDocs,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onRunOrganize = viewModel::runOrganizePdfPages,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_WATERMARK -> {
                                WatermarkPdfScreen(
                                    selectedDoc = selectedDoc ?: allDocs.firstOrNull(),
                                    allDocuments = allDocs,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onRunWatermark = viewModel::runWatermarkPdf,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_PAGE_NUMBERS -> {
                                PageNumbersPdfScreen(
                                    selectedDoc = selectedDoc ?: allDocs.firstOrNull(),
                                    allDocuments = allDocs,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onRunPageNumbers = viewModel::runPageNumbersPdf,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_PDF_TO_IMAGE,
                            TopLevelDestination.TOOL_EXTRACT_IMAGES -> {
                                PdfToImageScreen(
                                    isExtractImagesMode = dest == TopLevelDestination.TOOL_EXTRACT_IMAGES,
                                    selectedDoc = selectedDoc ?: allDocs.firstOrNull(),
                                    allDocuments = allDocs,
                                    exportedImages = exportedImages,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onRunPdfToImages = viewModel::runConvertPdfToImages,
                                    onShareFile = viewModel::shareFile,
                                    onExportSafFile = viewModel::requestSafExport,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_EXTRACT_TEXT -> {
                                ExtractTextScreen(
                                    selectedDoc = selectedDoc ?: allDocs.firstOrNull(),
                                    allDocuments = allDocs,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onShareFile = viewModel::shareFile,
                                    onExportSafFile = viewModel::requestSafExport,
                                    onCopyMessage = viewModel::showMessage,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_METADATA_VIEWER -> {
                                PdfMetadataViewerScreen(
                                    selectedDoc = selectedDoc ?: allDocs.firstOrNull(),
                                    allDocuments = allDocs,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onCopyMessage = viewModel::showMessage,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            TopLevelDestination.TOOL_SECURITY_LOCK_UNLOCK -> {
                                PdfSecurityScreen(
                                    selectedDoc = selectedDoc ?: allDocs.firstOrNull(),
                                    allDocuments = allDocs,
                                    onSelectDoc = viewModel::selectDocumentForTool,
                                    onImportPdfFromSaf = { safImportLauncher.launch(arrayOf("application/pdf")) },
                                    onRunSecurityAction = viewModel::runSecurityAction,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }
                        }
                    }

                    // Global Processing Progress & Result Modal
                    ProcessingStatusModal(
                        state = processingState,
                        onCancel = viewModel::cancelActiveProcessing,
                        onDismiss = viewModel::dismissProcessingResult,
                        onOpenDocument = viewModel::openDocumentInReader,
                        onShareFile = viewModel::shareFile,
                        onSaveToDeviceSaf = viewModel::requestSafExport
                    )
                }
            }
        }

        // Full-Screen Choreographed Cinematic Application Launch Experience
        if (showCinematicIntro) {
            PdflyCinematicSplashScreen(
                isInitializationComplete = isInitializationComplete,
                isPausedForInspection = isSplashPausedForInspection,
                onSkipOrComplete = viewModel::completeCinematicIntro
            )
        }
    }
}
