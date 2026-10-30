package com.example.feature.reader

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.processing.PdfProcessingEngine
import com.example.data.local.PdfAnnotationEntity
import com.example.data.local.PdfDocumentEntity
import com.example.data.local.SavedSignatureEntity
import com.example.domain.model.AnnotationType
import com.example.feature.common.ActiveDocumentSelectorCard
import com.example.feature.common.PdflyTopBar
import com.example.ui.motion.LocalReducedMotion
import com.example.ui.motion.PdflyMotion
import com.example.ui.theme.pdflyColors
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.Locale

enum class ReaderStudioMode {
    READER,
    ANNOTATE,
    SIGNATURE
}

@Composable
fun PdfReaderScreen(
    mode: ReaderStudioMode,
    selectedDoc: PdfDocumentEntity?,
    allDocs: List<PdfDocumentEntity>,
    annotationsFlow: Flow<List<PdfAnnotationEntity>>,
    savedSignatures: List<SavedSignatureEntity>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportFromSaf: () -> Unit,
    onUpdatePageAndRotation: (PdfDocumentEntity, Int, Int) -> Unit,
    onToggleBookmark: (PdfDocumentEntity, Int) -> Unit,
    onAddAnnotation: (Long, Int, AnnotationType, String, Long, Float, Float, Float, Float, String) -> Unit,
    onDeleteAnnotation: (Long) -> Unit,
    onSaveNewSignature: (String, Long, String) -> Unit,
    onDeleteSavedSignature: (Long) -> Unit,
    onFlattenAndExportPdf: (PdfDocumentEntity, String) -> Unit,
    onShareFile: (File) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val annotations by annotationsFlow.collectAsState(initial = emptyList())
    val isReducedMotion = LocalReducedMotion.current
    val pdflyColors = MaterialTheme.pdflyColors

    var currentPage by remember(selectedDoc?.id) {
        mutableIntStateOf((selectedDoc?.lastReadPage ?: 0).coerceAtLeast(0))
    }
    var rotationDegrees by remember(selectedDoc?.id) {
        mutableIntStateOf(selectedDoc?.rotationDegrees ?: 0)
    }
    var zoomScale by remember(selectedDoc?.id, currentPage) { mutableFloatStateOf(1f) }
    var panOffset by remember(selectedDoc?.id, currentPage) { mutableStateOf(Offset.Zero) }

    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRendering by remember { mutableStateOf(false) }

    var showSearchBar by remember { mutableStateOf(false) }
    var readerSearchQuery by remember { mutableStateOf("") }

    var showNoteDialog by remember { mutableStateOf(false) }
    var noteInputText by remember { mutableStateOf("Approved for release") }

    var showSignaturePadDialog by remember { mutableStateOf(mode == ReaderStudioMode.SIGNATURE && savedSignatures.isEmpty()) }
    var isFreehandDrawingActive by remember { mutableStateOf(false) }
    val activeFreehandPoints = remember { mutableStateListOf<Offset>() }

    var showExportAnnotatedDialog by remember { mutableStateOf(false) }
    var exportNameInput by remember(selectedDoc?.title) {
        mutableStateOf("${selectedDoc?.title?.removeSuffix(".pdf") ?: "Document"}_Annotated")
    }

    // Re-render current PDF page whenever document, page, rotation, or annotations change
    LaunchedEffect(selectedDoc?.id, selectedDoc?.filePath, currentPage, rotationDegrees, annotations) {
        val doc = selectedDoc ?: return@LaunchedEffect
        isRendering = true
        val bmp = PdfProcessingEngine.renderPageToBitmap(
            file = File(doc.filePath),
            pageIndex = currentPage.coerceIn(0, (doc.pageCount - 1).coerceAtLeast(0)),
            targetWidthPx = 1100,
            rotationDegrees = rotationDegrees,
            annotations = annotations
        )
        pageBitmap = bmp
        isRendering = false
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newZoom = (zoomScale * zoomChange).coerceIn(1f, 3.5f)
        zoomScale = newZoom
        panOffset = if (newZoom > 1.02f) {
            Offset(
                x = (panOffset.x + offsetChange.x).coerceIn(-600f, 600f),
                y = (panOffset.y + offsetChange.y).coerceIn(-800f, 800f)
            )
        } else {
            Offset.Zero
        }
    }

    val topTitle = when (mode) {
        ReaderStudioMode.READER -> selectedDoc?.title ?: "PDF Reader"
        ReaderStudioMode.ANNOTATE -> "Annotate PDF Studio"
        ReaderStudioMode.SIGNATURE -> "Digital Signature Studio"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = topTitle,
            subtitle = selectedDoc?.let {
                "Page ${currentPage + 1} of ${it.pageCount} • ${annotations.count { a -> a.pageIndex == currentPage }} markups on page"
            },
            onBack = onBack,
            actions = {
                if (selectedDoc != null) {
                    val isBookmarked = selectedDoc.bookmarkedPages.contains(currentPage)
                    val bookmarkScale by animateFloatAsState(
                        targetValue = if (isBookmarked && !isReducedMotion) 1.2f else 1f,
                        animationSpec = PdflyMotion.tactileSpring(),
                        label = "bookmark_scale"
                    )
                    IconButton(
                        onClick = { onToggleBookmark(selectedDoc, currentPage) },
                        modifier = Modifier.testTag("reader_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Toggle Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.graphicsLayer {
                                scaleX = bookmarkScale
                                scaleY = bookmarkScale
                            }
                        )
                    }
                    IconButton(
                        onClick = {
                            val nextRot = (rotationDegrees + 90) % 360
                            rotationDegrees = nextRot
                            onUpdatePageAndRotation(selectedDoc, currentPage, nextRot)
                        },
                        modifier = Modifier.testTag("reader_rotate_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.RotateRight,
                            contentDescription = "Rotate Page 90°",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showSearchBar = !showSearchBar },
                        modifier = Modifier.testTag("reader_search_toggle_button")
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search in PDF",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { onShareFile(File(selectedDoc.filePath)) },
                        modifier = Modifier.testTag("reader_share_button")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share PDF",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )

        if (selectedDoc == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                ActiveDocumentSelectorCard(
                    selectedDoc = null,
                    allDocs = allDocs,
                    onSelectDoc = onSelectDoc,
                    onImportFromSaf = onImportFromSaf
                )
            }
            return@Column
        }

        // Smooth Animated Reading Progress Indicator
        val rawProgress = (currentPage + 1).toFloat() / selectedDoc.pageCount.coerceAtLeast(1)
        val animatedProgress by animateFloatAsState(
            targetValue = rawProgress,
            animationSpec = PdflyMotion.snappySpring(),
            label = "reading_progress"
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )

        // Optional Search & Switch PDF Bar
        AnimatedVisibility(visible = showSearchBar) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = readerSearchQuery,
                        onValueChange = { readerSearchQuery = it },
                        placeholder = { Text("Search keywords or annotation notes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = {
                                readerSearchQuery = ""
                                showSearchBar = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close search")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reader_search_input")
                    )
                    if (readerSearchQuery.isNotBlank()) {
                        val matchingNotes = annotations.filter {
                            it.textContent.contains(readerSearchQuery, ignoreCase = true)
                        }
                        val docMatches = selectedDoc.searchableSummary.contains(readerSearchQuery, ignoreCase = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = buildString {
                                append(if (docMatches) "✓ Found in document text index. " else "No exact match in summary. ")
                                if (matchingNotes.isNotEmpty()) {
                                    append("Found on page(s): ${matchingNotes.map { it.pageIndex + 1 }.distinct().joinToString()}")
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = pdflyColors.cardBorder)
                }
            }
        }

        // Main PDF Canvas Viewport with Directional Page-Turn Transitions
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val bmp = pageBitmap
            if (bmp != null) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (isReducedMotion) {
                            fadeIn(tween(90)) togetherWith fadeOut(tween(90))
                        } else {
                            val forward = targetState >= initialState
                            val dir = if (forward) 1 else -1
                            (slideInHorizontally(
                                animationSpec = tween(250, easing = PdflyMotion.EmphasizedDecelerate),
                                initialOffsetX = { fullWidth -> (fullWidth * 0.22f * dir).toInt() }
                            ) + fadeIn(tween(220))) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(190, easing = PdflyMotion.EmphasizedAccelerate),
                                    targetOffsetX = { fullWidth -> (-fullWidth * 0.22f * dir).toInt() }
                                ) + fadeOut(tween(150)))
                        }
                    },
                    label = "pdf_page_turn_transition"
                ) { pageIdx ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .let { mod ->
                                if (!isFreehandDrawingActive) {
                                    mod.transformable(state = transformState)
                                } else mod
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = zoomScale,
                                    scaleY = zoomScale,
                                    translationX = panOffset.x,
                                    translationY = panOffset.y
                                )
                                .let { mod ->
                                    if (isFreehandDrawingActive) {
                                        mod.pointerInput(pageIdx, rotationDegrees) {
                                            detectDragGestures(
                                                onDragStart = { startOffset ->
                                                    activeFreehandPoints.clear()
                                                    val normX = (startOffset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                    val normY = (startOffset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                                    activeFreehandPoints.add(Offset(normX, normY))
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    val normX = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                    val normY = (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                                                    activeFreehandPoints.add(Offset(normX, normY))
                                                },
                                                onDragEnd = {
                                                    if (activeFreehandPoints.size >= 2) {
                                                        val normRot = ((rotationDegrees % 360) + 360) % 360
                                                        val serialized = activeFreehandPoints.joinToString(";") { pt ->
                                                            val (unrotX, unrotY) = when (normRot) {
                                                                90 -> pt.y to (1f - pt.x)
                                                                180 -> (1f - pt.x) to (1f - pt.y)
                                                                270 -> (1f - pt.y) to pt.x
                                                                else -> pt.x to pt.y
                                                            }
                                                            String.format(
                                                                Locale.US,
                                                                "%.4f,%.4f",
                                                                unrotX.coerceIn(0f, 1f),
                                                                unrotY.coerceIn(0f, 1f)
                                                            )
                                                        }
                                                        onAddAnnotation(
                                                            selectedDoc.id,
                                                            pageIdx,
                                                            AnnotationType.FREEHAND,
                                                            "Freehand Ink",
                                                            0xFFE11D48,
                                                            0f,
                                                            0f,
                                                            1f,
                                                            1f,
                                                            serialized
                                                        )
                                                    }
                                                    activeFreehandPoints.clear()
                                                    isFreehandDrawingActive = false
                                                }
                                            )
                                        }
                                    } else mod
                                }
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "PDF Page ${pageIdx + 1}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .testTag("pdf_page_bitmap_image")
                            )

                            // Live Freehand preview overlay while dragging
                            if (isFreehandDrawingActive && activeFreehandPoints.size >= 2) {
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    val path = Path()
                                    val first = activeFreehandPoints.first()
                                    path.moveTo(first.x * size.width, first.y * size.height)
                                    for (i in 1 until activeFreehandPoints.size) {
                                        val pt = activeFreehandPoints[i]
                                        path.lineTo(pt.x * size.width, pt.y * size.height)
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color(0xFFE11D48),
                                        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isRendering) {
                CircularProgressIndicator(color = Color.White)
            }

            // Floating Zoom Controls & Freehand Banner
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0B1120).copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(1f); if (zoomScale == 1f) panOffset = Offset.Zero },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clickable {
                            zoomScale = 1f
                            panOffset = Offset.Zero
                        }
                        .padding(horizontal = 6.dp)
                )
                IconButton(
                    onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(3.5f) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            if (isFreehandDrawingActive) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Draw directly on the PDF page with your finger",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Page Thumbnail Strip + Page Navigation Bar (Pure Surface with Crisp Top Divider)
        HorizontalDivider(thickness = 1.dp, color = pdflyColors.cardBorder)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Page Thumbnails Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items((0 until selectedDoc.pageCount).toList()) { pageIdx ->
                        val isCurrent = pageIdx == currentPage
                        val isBookmarked = selectedDoc.bookmarkedPages.contains(pageIdx)
                        val pageMarkupCount = annotations.count { it.pageIndex == pageIdx }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                            else pdflyColors.subtleSurface,
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else pdflyColors.cardBorder
                            ),
                            modifier = Modifier
                                .clickable {
                                    currentPage = pageIdx
                                    onUpdatePageAndRotation(selectedDoc, pageIdx, rotationDegrees)
                                }
                                .testTag("page_thumb_${pageIdx + 1}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "P.${pageIdx + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                if (isBookmarked) {
                                    Icon(
                                        Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                if (pageMarkupCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Annotation & Signature Action Toolbar
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = {
                                val count = annotations.count { it.pageIndex == currentPage }
                                val yPos = (0.22f + (count % 5) * 0.09f).coerceIn(0.15f, 0.78f)
                                onAddAnnotation(
                                    selectedDoc.id,
                                    currentPage,
                                    AnnotationType.HIGHLIGHT,
                                    "Highlighted Clause",
                                    0xFFFBBF24,
                                    0.12f,
                                    yPos,
                                    0.72f,
                                    0.045f,
                                    ""
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Highlight,
                                    contentDescription = null,
                                    tint = pdflyColors.amberIcon,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Highlight", fontWeight = FontWeight.SemiBold) },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = pdflyColors.strongBorder
                            ),
                            modifier = Modifier.testTag("btn_add_highlight")
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = {
                                val count = annotations.count { it.pageIndex == currentPage }
                                val yPos = (0.28f + (count % 5) * 0.08f).coerceIn(0.18f, 0.82f)
                                onAddAnnotation(
                                    selectedDoc.id,
                                    currentPage,
                                    AnnotationType.UNDERLINE,
                                    "Underlined Point",
                                    0xFFE11D48,
                                    0.12f,
                                    yPos,
                                    0.68f,
                                    0.03f,
                                    ""
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FormatUnderlined,
                                    contentDescription = null,
                                    tint = pdflyColors.crimsonIcon,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Underline", fontWeight = FontWeight.SemiBold) },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = pdflyColors.strongBorder
                            ),
                            modifier = Modifier.testTag("btn_add_underline")
                        )
                    }
                    item {
                        FilterChip(
                            selected = isFreehandDrawingActive,
                            onClick = { isFreehandDrawingActive = !isFreehandDrawingActive },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Brush,
                                    contentDescription = null,
                                    tint = if (isFreehandDrawingActive) MaterialTheme.colorScheme.primary else pdflyColors.violetIcon,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    if (isFreehandDrawingActive) "Drawing Ink..." else "Freehand Ink",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isFreehandDrawingActive,
                                borderColor = pdflyColors.strongBorder,
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("btn_toggle_freehand")
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { showNoteDialog = true },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.StickyNote2,
                                    contentDescription = null,
                                    tint = pdflyColors.indigoIcon,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Text Note", fontWeight = FontWeight.SemiBold) },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = pdflyColors.strongBorder
                            ),
                            modifier = Modifier.testTag("btn_add_text_note")
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { showSignaturePadDialog = true },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Draw,
                                    contentDescription = null,
                                    tint = pdflyColors.roseIcon,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Stamp Signature", fontWeight = FontWeight.SemiBold) },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = pdflyColors.strongBorder
                            ),
                            modifier = Modifier.testTag("btn_open_signature_pad")
                        )
                    }
                }

                // Bottom Page Controls + Flatten Export Button
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (currentPage > 0) {
                                    currentPage--
                                    onUpdatePageAndRotation(selectedDoc, currentPage, rotationDegrees)
                                }
                            },
                            enabled = currentPage > 0,
                            modifier = Modifier.testTag("prev_page_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Previous Page")
                        }
                        Text(
                            text = "${currentPage + 1} / ${selectedDoc.pageCount}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = {
                                if (currentPage < selectedDoc.pageCount - 1) {
                                    currentPage++
                                    onUpdatePageAndRotation(selectedDoc, currentPage, rotationDegrees)
                                }
                            },
                            enabled = currentPage < selectedDoc.pageCount - 1,
                            modifier = Modifier.testTag("next_page_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next Page")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val pageMarkups = annotations.filter { it.pageIndex == currentPage }
                        if (pageMarkups.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { onDeleteAnnotation(pageMarkups.last().id) },
                                border = BorderStroke(1.dp, pdflyColors.strongBorder),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("undo_last_markup_button")
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Undo (${pageMarkups.size})", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = { showExportAnnotatedDialog = true },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("save_annotated_pdf_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add Text Note Dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Add Text Callout on Page ${currentPage + 1}", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = noteInputText,
                    onValueChange = { noteInputText = it },
                    label = { Text("Annotation Note Text") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_text_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedDoc != null) {
                            val count = annotations.count { it.pageIndex == currentPage }
                            val yPos = (0.35f + (count % 4) * 0.11f).coerceIn(0.2f, 0.78f)
                            onAddAnnotation(
                                selectedDoc.id,
                                currentPage,
                                AnnotationType.TEXT_NOTE,
                                noteInputText.ifBlank { "Reviewed & Verified" },
                                0xFFD97706,
                                0.14f,
                                yPos,
                                0.65f,
                                0.07f,
                                ""
                            )
                        }
                        showNoteDialog = false
                    },
                    modifier = Modifier.testTag("confirm_add_note_button")
                ) {
                    Text("Place Note", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Signature Pad & Saved Signatures Dialog
    if (showSignaturePadDialog && selectedDoc != null) {
        SignatureStudioDialog(
            savedSignatures = savedSignatures,
            onDismiss = { showSignaturePadDialog = false },
            onSaveSignature = onSaveNewSignature,
            onDeleteSignature = onDeleteSavedSignature,
            onStampSignatureOnPage = { sig ->
                onAddAnnotation(
                    selectedDoc.id,
                    currentPage,
                    AnnotationType.SIGNATURE,
                    sig.label,
                    sig.inkColorHex,
                    0.48f,
                    0.74f,
                    0.42f,
                    0.14f,
                    sig.strokesSerialized
                )
                showSignaturePadDialog = false
            }
        )
    }

    // Save / Flatten Annotated PDF Dialog
    if (showExportAnnotatedDialog && selectedDoc != null) {
        AlertDialog(
            onDismissRequest = { showExportAnnotatedDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Export Flattened PDF", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Burns all ${annotations.size} highlights, notes, ink strokes, and signatures directly into a new local PDF document.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = exportNameInput,
                        onValueChange = { exportNameInput = it },
                        label = { Text("Output PDF Filename") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_annotated_filename_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportAnnotatedDialog = false
                        onFlattenAndExportPdf(selectedDoc, exportNameInput)
                    },
                    modifier = Modifier.testTag("confirm_flatten_export_button")
                ) {
                    Text("Export PDF", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportAnnotatedDialog = false }) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun SignatureStudioDialog(
    savedSignatures: List<SavedSignatureEntity>,
    onDismiss: () -> Unit,
    onSaveSignature: (String, Long, String) -> Unit,
    onDeleteSignature: (Long) -> Unit,
    onStampSignatureOnPage: (SavedSignatureEntity) -> Unit
) {
    val pdflyColors = MaterialTheme.pdflyColors
    var signatureLabel by remember { mutableStateOf("Authorized Signature") }
    var selectedInkColor by remember { mutableLongStateOf(0xFF1E3A8A) }
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Digital Signature Pad", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (savedSignatures.isNotEmpty()) {
                    Text(
                        text = "Tap a saved signature to stamp on page:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    savedSignatures.take(3).forEach { sig ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = pdflyColors.subtleSurface,
                            border = BorderStroke(1.dp, pdflyColors.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStampSignatureOnPage(sig) }
                                .testTag("stamp_saved_signature_${sig.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Draw,
                                        contentDescription = null,
                                        tint = Color(sig.inkColorHex or 0xFF000000)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        sig.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                TextButton(onClick = { onStampSignatureOnPage(sig) }) {
                                    Text("Stamp", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Or draw a new vector signature below:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Interactive Signature Drawing Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.5.dp, pdflyColors.strongBorder, RoundedCornerShape(14.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentStroke.clear()
                                    currentStroke.add(
                                        Offset(
                                            (offset.x / size.width.toFloat()).coerceIn(0f, 1f),
                                            (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                        )
                                    )
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentStroke.add(
                                        Offset(
                                            (change.position.x / size.width.toFloat()).coerceIn(0f, 1f),
                                            (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                                        )
                                    )
                                },
                                onDragEnd = {
                                    if (currentStroke.size >= 2) {
                                        strokes.add(currentStroke.toList())
                                    }
                                    currentStroke.clear()
                                }
                            )
                        }
                        .testTag("signature_drawing_canvas")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Baseline guide
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(24f, size.height * 0.76f),
                            end = Offset(size.width - 24f, size.height * 0.76f),
                            strokeWidth = 2f
                        )

                        val inkColor = Color(selectedInkColor or 0xFF000000)
                        val allStrokes = strokes + if (currentStroke.size >= 2) listOf(currentStroke.toList()) else emptyList()
                        for (s in allStrokes) {
                            val path = Path()
                            path.moveTo(s.first().x * size.width, s.first().y * size.height)
                            for (i in 1 until s.size) {
                                path.lineTo(s[i].x * size.width, s[i].y * size.height)
                            }
                            drawPath(
                                path = path,
                                color = inkColor,
                                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            strokes.clear()
                            currentStroke.clear()
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                // Ink Color Picker
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ink Color:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    listOf(
                        0xFF1E3A8A to "Royal Navy",
                        0xFF0F172A to "Carbon Black",
                        0xFFB91C1C to "Crimson Ink"
                    ).forEach { (hex, _) ->
                        val selected = selectedInkColor == hex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(hex or 0xFF000000))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else pdflyColors.strongBorder,
                                    shape = CircleShape
                                )
                                .clickable { selectedInkColor = hex }
                        )
                    }
                }

                OutlinedTextField(
                    value = signatureLabel,
                    onValueChange = { signatureLabel = it },
                    label = { Text("Signature Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val serialized = if (strokes.isNotEmpty()) {
                        strokes.joinToString("|") { s ->
                            s.joinToString(";") { pt ->
                                String.format(Locale.US, "%.3f,%.3f", pt.x, pt.y)
                            }
                        }
                    } else {
                        // Default stylized calligraphic stroke if user taps Save & Stamp directly
                        "0.08,0.68;0.18,0.26;0.29,0.62;0.42,0.34;0.56,0.55;0.72,0.38;0.90,0.48|0.12,0.74;0.88,0.68"
                    }
                    onSaveSignature(signatureLabel, selectedInkColor, serialized)
                    onStampSignatureOnPage(
                        SavedSignatureEntity(
                            label = signatureLabel,
                            inkColorHex = selectedInkColor,
                            strokesSerialized = serialized
                        )
                    )
                },
                modifier = Modifier.testTag("save_and_stamp_signature_button")
            ) {
                Text("Save & Stamp on Page", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
