package com.example.feature.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.filemanager.LocalFileManager
import com.example.core.processing.PdfProcessingEngine
import com.example.data.local.PdfDocumentEntity
import com.example.domain.model.ExportedImageItem
import com.example.domain.model.ExtractedPdfTextResult
import com.example.domain.model.ImageExportFormat
import com.example.domain.model.PageNumberConfig
import com.example.domain.model.PageNumberFormat
import com.example.domain.model.PageNumberPosition
import com.example.domain.model.PageOrganizeItem
import com.example.domain.model.PdfMetadataReport
import com.example.domain.model.WatermarkConfig
import com.example.domain.model.WatermarkPosition
import com.example.feature.common.ActiveDocumentSelectorCard
import com.example.feature.common.PdflyTopBar
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.pdflyColors
import java.io.File
import kotlin.math.roundToInt

/**
 * 1. PDF PAGE ORGANIZER SCREEN:
 * Reorder pages, rotate individual pages (0°/90°/180°/270°), delete/restore pages,
 * or extract selected pages into a new PDF document.
 */
@Composable
fun PageOrganizerScreen(
    selectedDoc: PdfDocumentEntity?,
    allDocuments: List<PdfDocumentEntity>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onRunOrganize: (PdfDocumentEntity, List<PageOrganizeItem>, Boolean, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    var pageItems by remember(selectedDoc?.id, selectedDoc?.pageCount) {
        val count = (selectedDoc?.pageCount ?: 1).coerceAtLeast(1)
        mutableStateOf(
            (0 until count).map { idx ->
                PageOrganizeItem(
                    originalPageIndex = idx,
                    rotationDegrees = 0,
                    isDeleted = false,
                    isSelectedForExtract = idx == 0
                )
            }
        )
    }

    var outputFileName by remember(selectedDoc?.title) {
        mutableStateOf("${selectedDoc?.title?.removeSuffix(".pdf") ?: "Document"}_Organized")
    }

    val activeCount = pageItems.count { !it.isDeleted }
    val selectedForExtractCount = pageItems.count { !it.isDeleted && it.isSelectedForExtract }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "PDF Page Organizer",
            subtitle = selectedDoc?.let { "$activeCount of ${it.pageCount} pages active • Reorder, Rotate & Delete" }
                ?: "Reorder, rotate, delete, or extract PDF pages",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("organizer_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ActiveDocumentSelectorCard(
                    selectedDoc = selectedDoc,
                    allDocs = allDocuments,
                    onSelectDoc = onSelectDoc,
                    onImportFromSaf = onImportPdfFromSaf
                )
            }

            if (selectedDoc != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Page Sequence & Orientation",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$activeCount Active • $selectedForExtractCount Checked",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use the Up/Down arrows to reorder pages, Rotate 90° to fix orientation, or Delete to omit pages from the exported PDF.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                itemsIndexed(pageItems, key = { _, item -> item.id }) { index, item ->
                    val cardBg by animateColorAsState(
                        targetValue = if (item.isDeleted) pdflyColors.subtleSurface else MaterialTheme.colorScheme.surface,
                        label = "page_org_bg"
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("organize_page_card_${index + 1}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(
                            width = if (item.isSelectedForExtract && !item.isDeleted) 1.5.dp else 1.dp,
                            color = if (item.isDeleted) pdflyColors.cardBorder
                            else if (item.isSelectedForExtract) MaterialTheme.colorScheme.primary
                            else pdflyColors.cardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isSelectedForExtract && !item.isDeleted,
                                enabled = !item.isDeleted,
                                onCheckedChange = { checked ->
                                    pageItems = pageItems.toMutableList().also { list ->
                                        list[index] = item.copy(isSelectedForExtract = checked)
                                    }
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (item.isDeleted) MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "P.${item.originalPageIndex + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isDeleted) MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (item.isDeleted) {
                                        "Removed (Source Page ${item.originalPageIndex + 1})"
                                    } else {
                                        "Output Page #${index + 1} (Source P.${item.originalPageIndex + 1})"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isDeleted) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Rotation: ${item.rotationDegrees}° • ${if (item.isDeleted) "Excluded from export" else "Included"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        val mutable = pageItems.toMutableList()
                                        val moved = mutable.removeAt(index)
                                        mutable.add(index - 1, moved)
                                        pageItems = mutable
                                    }
                                },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                            }

                            IconButton(
                                onClick = {
                                    if (index < pageItems.lastIndex) {
                                        val mutable = pageItems.toMutableList()
                                        val moved = mutable.removeAt(index)
                                        mutable.add(index + 1, moved)
                                        pageItems = mutable
                                    }
                                },
                                enabled = index < pageItems.lastIndex
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                            }

                            IconButton(
                                onClick = {
                                    pageItems = pageItems.toMutableList().also { list ->
                                        list[index] = item.copy(rotationDegrees = (item.rotationDegrees + 90) % 360)
                                    }
                                },
                                modifier = Modifier.testTag("rotate_page_btn_${index + 1}")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.RotateRight,
                                    contentDescription = "Rotate 90 Degrees",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = {
                                    pageItems = pageItems.toMutableList().also { list ->
                                        list[index] = item.copy(isDeleted = !item.isDeleted)
                                    }
                                },
                                modifier = Modifier.testTag("delete_page_btn_${index + 1}")
                            ) {
                                Icon(
                                    imageVector = if (item.isDeleted) Icons.Default.RestoreFromTrash else Icons.Default.DeleteOutline,
                                    contentDescription = if (item.isDeleted) "Restore Page" else "Delete Page",
                                    tint = if (item.isDeleted) pdflyColors.successText else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = outputFileName,
                        onValueChange = { outputFileName = it },
                        label = { Text("Organized Output Filename") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("organize_output_filename_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                onRunOrganize(selectedDoc, pageItems, true, "${outputFileName}_Extracted")
                            },
                            enabled = selectedForExtractCount > 0,
                            border = BorderStroke(1.dp, pdflyColors.strongBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("extract_checked_pages_button")
                        ) {
                            Text("Extract Checked ($selectedForExtractCount)", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onRunOrganize(selectedDoc, pageItems, false, outputFileName)
                            },
                            enabled = activeCount > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("execute_organize_pdf_button")
                        ) {
                            Icon(Icons.Default.ViewModule, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save PDF ($activeCount Pages)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. PDF WATERMARK SCREEN:
 * Custom text watermark with live preview, opacity, font size, color, and position controls.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WatermarkPdfScreen(
    selectedDoc: PdfDocumentEntity?,
    allDocuments: List<PdfDocumentEntity>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onRunWatermark: (PdfDocumentEntity, WatermarkConfig, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    var watermarkText by remember { mutableStateOf("CONFIDENTIAL • DO NOT DISTRIBUTE") }
    var opacity by remember { mutableFloatStateOf(0.28f) }
    var fontSizePt by remember { mutableFloatStateOf(32f) }
    var position by remember { mutableStateOf(WatermarkPosition.DIAGONAL_CENTER) }
    var selectedColorArgb by remember { mutableLongStateOf(0xFFDC2626L) }
    var outputFileName by remember(selectedDoc?.title) {
        mutableStateOf("${selectedDoc?.title?.removeSuffix(".pdf") ?: "Document"}_Watermarked")
    }

    val colorPresets = listOf(
        "Crimson Red" to 0xFFDC2626L,
        "Slate Charcoal" to 0xFF1E293BL,
        "Royal Blue" to 0xFF1D4ED8L,
        "Emerald Stamp" to 0xFF047857L
    )

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "PDF Watermark",
            subtitle = "Stamp custom text watermarks across all PDF pages",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("watermark_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ActiveDocumentSelectorCard(
                    selectedDoc = selectedDoc,
                    allDocs = allDocuments,
                    onSelectDoc = onSelectDoc,
                    onImportFromSaf = onImportPdfFromSaf
                )
            }

            if (selectedDoc != null) {
                // Live Watermark Page Preview Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Live Page Watermark Preview",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, pdflyColors.strongBorder, RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = when (position) {
                                    WatermarkPosition.DIAGONAL_CENTER -> Alignment.Center
                                    WatermarkPosition.TOP_HEADER -> Alignment.TopCenter
                                    WatermarkPosition.BOTTOM_FOOTER -> Alignment.BottomCenter
                                }
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Simulated document text lines
                                    for (i in 0..8) {
                                        val y = 24f + i * 24f
                                        drawRoundRect(
                                            color = Color(0xFFE2E8F0),
                                            topLeft = Offset(8f, y),
                                            size = Size(if (i % 3 == 0) size.width * 0.7f else size.width * 0.92f, 8f),
                                            cornerRadius = CornerRadius(4f, 4f)
                                        )
                                    }
                                    if (position == WatermarkPosition.DIAGONAL_CENTER) {
                                        rotate(-32f) {
                                            drawRoundRect(
                                                color = Color(selectedColorArgb).copy(alpha = opacity.coerceIn(0.1f, 0.9f)),
                                                topLeft = Offset(size.width * 0.05f, size.height * 0.44f),
                                                size = Size(size.width * 0.9f, 28f),
                                                cornerRadius = CornerRadius(6f, 6f),
                                                style = Stroke(width = 3f)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = watermarkText.ifBlank { "WATERMARK" },
                                    color = Color(selectedColorArgb).copy(alpha = opacity.coerceIn(0.12f, 0.9f)),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Watermark Configuration Controls
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = watermarkText,
                                onValueChange = { watermarkText = it },
                                label = { Text("Watermark Stamp Text") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("watermark_text_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Quick Stamp Presets",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("CONFIDENTIAL", "DRAFT COPY", "DO NOT COPY", "APPROVED").forEach { preset ->
                                    FilterChip(
                                        selected = watermarkText == preset,
                                        onClick = { watermarkText = preset },
                                        label = { Text(preset, fontWeight = FontWeight.SemiBold) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Watermark Position",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WatermarkPosition.entries.forEach { pos ->
                                    FilterChip(
                                        selected = position == pos,
                                        onClick = { position = pos },
                                        label = { Text(pos.label, fontWeight = FontWeight.SemiBold) },
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = position == pos,
                                            borderColor = pdflyColors.strongBorder,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Stamp Color",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                colorPresets.forEach { (name, hex) ->
                                    FilterChip(
                                        selected = selectedColorArgb == hex,
                                        onClick = { selectedColorArgb = hex },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(hex))
                                            )
                                        },
                                        label = { Text(name, fontWeight = FontWeight.SemiBold) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Opacity: ${(opacity * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Slider(
                                value = opacity,
                                onValueChange = { opacity = it },
                                valueRange = 0.10f..0.80f
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Font Size: ${fontSizePt.roundToInt()} pt",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Slider(
                                value = fontSizePt,
                                onValueChange = { fontSizePt = it },
                                valueRange = 18f..64f
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = outputFileName,
                        onValueChange = { outputFileName = it },
                        label = { Text("Watermarked Output Filename") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onRunWatermark(
                                selectedDoc,
                                WatermarkConfig(
                                    text = watermarkText,
                                    opacity = opacity,
                                    fontSizePt = fontSizePt,
                                    colorArgb = selectedColorArgb,
                                    position = position
                                ),
                                outputFileName
                            )
                        },
                        enabled = watermarkText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("execute_watermark_pdf_button")
                    ) {
                        Icon(Icons.Default.TextFields, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Watermark (${selectedDoc.pageCount} Pages)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 3. ADD PAGE NUMBERS SCREEN:
 * Stamp formatted page numbers across all pages of a PDF document.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PageNumbersPdfScreen(
    selectedDoc: PdfDocumentEntity?,
    allDocuments: List<PdfDocumentEntity>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onRunPageNumbers: (PdfDocumentEntity, PageNumberConfig, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    var position by remember { mutableStateOf(PageNumberPosition.BOTTOM_RIGHT) }
    var format by remember { mutableStateOf(PageNumberFormat.PAGE_X_OF_Y) }
    var startNumber by remember { mutableIntStateOf(1) }
    var fontSizePt by remember { mutableFloatStateOf(11f) }
    var outputFileName by remember(selectedDoc?.title) {
        mutableStateOf("${selectedDoc?.title?.removeSuffix(".pdf") ?: "Document"}_Numbered")
    }

    val previewLabel = PdfProcessingEngine.formatPageNumberLabel(
        format = format,
        current = startNumber,
        total = startNumber + ((selectedDoc?.pageCount ?: 5) - 1).coerceAtLeast(0)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Add Page Numbers",
            subtitle = "Stamp formatted page numbers across every page",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("page_numbers_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ActiveDocumentSelectorCard(
                    selectedDoc = selectedDoc,
                    allDocs = allDocuments,
                    onSelectDoc = onSelectDoc,
                    onImportFromSaf = onImportPdfFromSaf
                )
            }

            if (selectedDoc != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "First Page Stamp Preview",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(
                                        text = previewLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Numbering Format",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PageNumberFormat.entries.forEach { fmt ->
                                    FilterChip(
                                        selected = format == fmt,
                                        onClick = { format = fmt },
                                        label = { Text("${fmt.label} (${fmt.example})", fontWeight = FontWeight.SemiBold) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Stamp Position on Page",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PageNumberPosition.entries.forEach { pos ->
                                    FilterChip(
                                        selected = position == pos,
                                        onClick = { position = pos },
                                        label = { Text(pos.label, fontWeight = FontWeight.SemiBold) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Starting Page Number: $startNumber",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 2, 3, 5, 10).forEach { num ->
                                    FilterChip(
                                        selected = startNumber == num,
                                        onClick = { startNumber = num },
                                        label = { Text("Start at $num", fontWeight = FontWeight.SemiBold) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = outputFileName,
                        onValueChange = { outputFileName = it },
                        label = { Text("Numbered Output Filename") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onRunPageNumbers(
                                selectedDoc,
                                PageNumberConfig(
                                    position = position,
                                    format = format,
                                    startNumber = startNumber,
                                    fontSizePt = fontSizePt
                                ),
                                outputFileName
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("execute_page_numbers_button")
                    ) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Page Numbers (${selectedDoc.pageCount} Pages)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 4. PDF TO JPG/PNG & EXTRACT IMAGES SCREEN:
 * Renders and exports PDF pages and visual streams to PNG or JPG image files.
 */
@Composable
fun PdfToImageScreen(
    isExtractImagesMode: Boolean,
    selectedDoc: PdfDocumentEntity?,
    allDocuments: List<PdfDocumentEntity>,
    exportedImages: List<ExportedImageItem>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onRunPdfToImages: (PdfDocumentEntity, ImageExportFormat, Int, String) -> Unit,
    onShareFile: (File) -> Unit,
    onExportSafFile: (File) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    var format by remember { mutableStateOf(ImageExportFormat.PNG) }
    var qualityPercent by remember { mutableFloatStateOf(92f) }
    var baseFileName by remember(selectedDoc?.title) {
        mutableStateOf(selectedDoc?.title?.removeSuffix(".pdf") ?: "Document")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = if (isExtractImagesMode) "Extract Images from PDF" else "PDF to JPG / PNG",
            subtitle = if (isExtractImagesMode) {
                "Extract page visuals and graphics into standalone PNG or JPG files"
            } else {
                "Render and export PDF pages into high-resolution PNG or JPG images"
            },
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("pdf_to_image_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ActiveDocumentSelectorCard(
                    selectedDoc = selectedDoc,
                    allDocs = allDocuments,
                    onSelectDoc = onSelectDoc,
                    onImportFromSaf = onImportPdfFromSaf
                )
            }

            if (selectedDoc != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select Image Export Format",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ImageExportFormat.entries.forEach { fmt ->
                                    FilterChip(
                                        selected = format == fmt,
                                        onClick = { format = fmt },
                                        label = { Text(fmt.label, fontWeight = FontWeight.SemiBold) },
                                        modifier = Modifier.testTag("img_format_chip_${fmt.extension}")
                                    )
                                }
                            }

                            if (format == ImageExportFormat.JPG) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "JPEG Quality: ${qualityPercent.roundToInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Slider(
                                    value = qualityPercent,
                                    onValueChange = { qualityPercent = it },
                                    valueRange = 60f..100f
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = baseFileName,
                                onValueChange = { baseFileName = it },
                                label = { Text("Image Filename Prefix") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onRunPdfToImages(selectedDoc, format, qualityPercent.roundToInt(), baseFileName)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("execute_pdf_to_image_button")
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isExtractImagesMode) {
                                        "Extract ${selectedDoc.pageCount} Image(s) as ${format.extension.uppercase()}"
                                    } else {
                                        "Convert ${selectedDoc.pageCount} Page(s) to ${format.extension.uppercase()}"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (exportedImages.isNotEmpty()) {
                    item {
                        Text(
                            text = "Exported Image Files (${exportedImages.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    itemsIndexed(exportedImages, key = { _, item -> item.filePath }) { _, img ->
                        val file = File(img.filePath)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, pdflyColors.cardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(pdflyColors.successContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        tint = pdflyColors.successText
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = img.fileName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Page ${img.pageIndex + 1} • ${img.widthPx}×${img.heightPx} px • ${LocalFileManager.formatFileSize(img.sizeBytes)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onShareFile(file) }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share Image")
                                }
                                IconButton(onClick = { onExportSafFile(file) }) {
                                    Icon(Icons.Default.Download, contentDescription = "Save Image")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. EXTRACT TEXT FROM PDF SCREEN:
 * Parses and displays selectable/copyable text streams from each page of the PDF.
 */
@Composable
fun ExtractTextScreen(
    selectedDoc: PdfDocumentEntity?,
    allDocuments: List<PdfDocumentEntity>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onShareFile: (File) -> Unit,
    onExportSafFile: (File) -> Unit,
    onCopyMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val pdflyColors = MaterialTheme.pdflyColors

    var extractedResult by remember(selectedDoc?.id) {
        mutableStateOf<ExtractedPdfTextResult?>(null)
    }

    LaunchedEffect(selectedDoc?.filePath) {
        val doc = selectedDoc
        if (doc != null) {
            extractedResult = PdfProcessingEngine.extractTextFromPdf(context, File(doc.filePath))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Extract Text from PDF",
            subtitle = "Parse readable text streams and export as .TXT",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("extract_text_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ActiveDocumentSelectorCard(
                    selectedDoc = selectedDoc,
                    allDocs = allDocuments,
                    onSelectDoc = onSelectDoc,
                    onImportFromSaf = onImportPdfFromSaf
                )
            }

            val result = extractedResult
            if (selectedDoc != null && result != null) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Extracted PDF Text", result.combinedText))
                                onCopyMessage("Copied extracted text (${result.pageCount} pages) to clipboard")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("copy_extracted_text_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy All Text", fontWeight = FontWeight.Bold)
                        }

                        val txtPath = result.exportedTxtFilePath
                        if (!txtPath.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = { onExportSafFile(File(txtPath)) },
                                border = BorderStroke(1.dp, pdflyColors.strongBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export .TXT", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                itemsIndexed(result.pageTexts) { pageIdx, pageText ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Page ${pageIdx + 1} of ${result.pageCount}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Copy Page",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Page ${pageIdx + 1}", pageText))
                                        onCopyMessage("Copied Page ${pageIdx + 1} text to clipboard")
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = pdflyColors.subtleSurface,
                                border = BorderStroke(1.dp, pdflyColors.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = pageText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 6. PDF METADATA INSPECTOR SCREEN:
 * Displays PDF specification version, page dimensions, Info dictionary fields, and SHA-256 hash.
 */
@Composable
fun PdfMetadataViewerScreen(
    selectedDoc: PdfDocumentEntity?,
    allDocuments: List<PdfDocumentEntity>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onCopyMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val pdflyColors = MaterialTheme.pdflyColors

    var metadata by remember(selectedDoc?.id) {
        mutableStateOf<PdfMetadataReport?>(null)
    }

    LaunchedEffect(selectedDoc?.filePath) {
        val doc = selectedDoc
        if (doc != null) {
            metadata = PdfProcessingEngine.inspectDetailedMetadata(File(doc.filePath))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "PDF Metadata Inspector",
            subtitle = "ISO 32000-1 Header, Dimensions, Properties & SHA-256",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("metadata_viewer_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ActiveDocumentSelectorCard(
                    selectedDoc = selectedDoc,
                    allDocs = allDocuments,
                    onSelectDoc = onSelectDoc,
                    onImportFromSaf = onImportPdfFromSaf
                )
            }

            val report = metadata
            if (selectedDoc != null && report != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("metadata_report_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Document Structure & Properties",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            MetadataKeyValueRow("File Name", report.fileName)
                            MetadataKeyValueRow("PDF Format Version", report.pdfVersion)
                            MetadataKeyValueRow("Total Page Count", "${report.pageCount} pages")
                            MetadataKeyValueRow("Page Dimensions", report.pageDimensionsSummary)
                            MetadataKeyValueRow("Exact File Size", "${LocalFileManager.formatFileSize(report.fileSizeBytes)} (${report.fileSizeBytes} bytes)")
                            MetadataKeyValueRow("Document Title", report.title)
                            MetadataKeyValueRow("Author", report.author)
                            MetadataKeyValueRow("Creator Application", report.creator)
                            MetadataKeyValueRow("PDF Producer", report.producer)
                            MetadataKeyValueRow(
                                "Encryption / Security",
                                if (report.isEncrypted) "Protected / Encrypted" else "Standard Unencrypted"
                            )
                            MetadataKeyValueRow(
                                "Last Modified",
                                LocalFileManager.formatTimestamp(report.lastModifiedTimestamp)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SHA-256 Cryptographic Checksum",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Copy Hash",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            clipboard?.setPrimaryClip(ClipData.newPlainText("PDF SHA-256", report.sha256Checksum))
                                            onCopyMessage("Copied SHA-256 checksum to clipboard")
                                        }
                                        .testTag("copy_sha256_button")
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = pdflyColors.subtleSurface,
                                border = BorderStroke(1.dp, pdflyColors.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = report.sha256Checksum,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataKeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.58f)
        )
    }
}

/**
 * 7. PDF SECURITY (PASSWORD PROTECT & UNLOCK) SCREEN:
 * Applies a local SHA-256 access verification seal or unlocks authorized documents.
 */
@Composable
fun PdfSecurityScreen(
    selectedDoc: PdfDocumentEntity?,
    allDocuments: List<PdfDocumentEntity>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onRunSecurityAction: (PdfDocumentEntity, String, Boolean, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    var unlockMode by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var outputFileName by remember(selectedDoc?.title, unlockMode) {
        val suffix = if (unlockMode) "Unlocked" else "Protected"
        mutableStateOf("${selectedDoc?.title?.removeSuffix(".pdf") ?: "Document"}_$suffix")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "PDF Security & Unlock",
            subtitle = "Local Access Protection & Authorized Unlock",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("security_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ActiveDocumentSelectorCard(
                    selectedDoc = selectedDoc,
                    allDocs = allDocuments,
                    onSelectDoc = onSelectDoc,
                    onImportFromSaf = onImportPdfFromSaf
                )
            }

            if (selectedDoc != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select Security Operation",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilterChip(
                                    selected = !unlockMode,
                                    onClick = { unlockMode = false },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Protect PDF", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("security_mode_protect")
                                )
                                FilterChip(
                                    selected = unlockMode,
                                    onClick = { unlockMode = true },
                                    leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Unlock Authorized PDF", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("security_mode_unlock")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = {
                                    Text(
                                        if (unlockMode) "Enter Authorized Document Password"
                                        else "Set Document Password (min 4 chars)"
                                    )
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Text(
                                        text = if (showPassword) "Hide" else "Show",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .clickable { showPassword = !showPassword }
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("security_password_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = pdflyColors.subtleSurface,
                                border = BorderStroke(1.dp, pdflyColors.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (unlockMode) {
                                            "Unlock verifies the document password for PDFs you are authorized to access and exports a clean, unsealed local copy."
                                        } else {
                                            "Applies a local SHA-256 access verification seal and security header to the exported PDF on your device."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = outputFileName,
                                onValueChange = { outputFileName = it },
                                label = { Text("Output PDF Filename") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onRunSecurityAction(selectedDoc, password, unlockMode, outputFileName)
                                },
                                enabled = password.length >= 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("execute_security_pdf_button")
                            ) {
                                Icon(
                                    if (unlockMode) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (unlockMode) "Verify Password & Unlock PDF"
                                    else "Apply Password Seal & Export PDF",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
