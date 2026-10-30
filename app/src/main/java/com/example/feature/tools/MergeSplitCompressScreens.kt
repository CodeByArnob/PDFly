package com.example.feature.tools

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.filemanager.LocalFileManager
import com.example.core.processing.PdfProcessingEngine
import com.example.data.local.PdfDocumentEntity
import com.example.domain.model.CompressionPreset
import com.example.domain.model.PageMarginPreset
import com.example.domain.model.PageOrientation
import com.example.domain.model.PaperSizePreset
import com.example.domain.model.ScannedPageDraft
import com.example.domain.model.SplitMode
import com.example.feature.common.ActiveDocumentSelectorCard
import com.example.feature.common.CompressionDensityVisualizer
import com.example.feature.common.ImageToPageMorphVisualizer
import com.example.feature.common.MergeConvergenceVisualizer
import com.example.feature.common.PdflyTopBar
import com.example.feature.common.SplitSeparationVisualizer
import com.example.ui.motion.pdflyPressable
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.pdflyColors
import kotlin.math.roundToInt

@Composable
fun MergePdfScreen(
    mergeQueue: List<PdfDocumentEntity>,
    allDocuments: List<PdfDocumentEntity>,
    onAddDocToQueue: (PdfDocumentEntity) -> Unit,
    onRemoveFromQueue: (Long) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onRunMerge: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors
    var outputFileName by remember { mutableStateOf("Merged_PDFly_Bundle") }

    val totalPages = mergeQueue.sumOf { it.pageCount }
    val totalBytes = mergeQueue.sumOf { it.sizeBytes }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Merge PDF Files",
            subtitle = "${mergeQueue.size} files selected • $totalPages total pages",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("merge_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Functional Motion Visualizer for Merge
            item {
                MergeConvergenceVisualizer(mergeQueue = mergeQueue)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Combine & Reorder PDFs Locally",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select 2 or more PDFs below, adjust their sequence using the up/down arrows, and merge them into a single unified PDF file.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onImportPdfFromSaf,
                                border = BorderStroke(1.dp, pdflyColors.strongBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("merge_import_saf_button")
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Device PDF", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Available Workspace Documents to Add
            val remainingDocs = allDocuments.filter { doc -> mergeQueue.none { it.id == doc.id } }
            if (remainingDocs.isNotEmpty()) {
                item {
                    Text(
                        text = "Tap to Add from Workspace:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        remainingDocs.forEach { doc ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, pdflyColors.cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pdflyPressable(onClick = { onAddDocToQueue(doc) })
                                    .testTag("merge_add_workspace_doc_${doc.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            doc.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${doc.pageCount} pages • ${LocalFileManager.formatFileSize(doc.sizeBytes)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    FilledTonalButton(onClick = { onAddDocToQueue(doc) }) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Merge Sequence (${mergeQueue.size} PDFs • ${LocalFileManager.formatFileSize(totalBytes)}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            itemsIndexed(mergeQueue, key = { _, item -> item.id }) { index, doc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .testTag("merge_queue_item_${index + 1}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${doc.pageCount} pages • ${LocalFileManager.formatFileSize(doc.sizeBytes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onMoveItem(index, index - 1) },
                            enabled = index > 0,
                            modifier = Modifier.testTag("merge_move_up_$index")
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                        }
                        IconButton(
                            onClick = { onMoveItem(index, index + 1) },
                            enabled = index < mergeQueue.size - 1,
                            modifier = Modifier.testTag("merge_move_down_$index")
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                        }
                        IconButton(
                            onClick = { onRemoveFromQueue(doc.id) },
                            modifier = Modifier.testTag("merge_remove_$index")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = outputFileName,
                    onValueChange = { outputFileName = it },
                    label = { Text("Output Merged PDF Filename") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("merge_output_filename_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onRunMerge(outputFileName) },
                    enabled = mergeQueue.size >= 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("execute_merge_pdf_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.MergeType, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Merge ${mergeQueue.size} PDFs ($totalPages Pages)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SplitPdfScreen(
    selectedDoc: PdfDocumentEntity?,
    allDocuments: List<PdfDocumentEntity>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onRunSplit: (PdfDocumentEntity, SplitMode, String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    var splitMode by remember { mutableStateOf(SplitMode.CUSTOM_RANGE) }
    var rangeInput by remember(selectedDoc?.id) {
        val maxP = (selectedDoc?.pageCount ?: 2).coerceAtMost(2)
        mutableStateOf("1-$maxP")
    }
    var outputBaseName by remember(selectedDoc?.title) {
        mutableStateOf("${selectedDoc?.title?.removeSuffix(".pdf") ?: "Document"}_Split")
    }

    val validationResult = remember(rangeInput, selectedDoc?.pageCount, splitMode) {
        val count = selectedDoc?.pageCount ?: 0
        if (splitMode == SplitMode.EVERY_PAGE) {
            Result.success((0 until count).toList())
        } else {
            PdfProcessingEngine.parsePageSelection(rangeInput, count)
        }
    }

    val selectedSet = validationResult.getOrNull()?.toSet() ?: emptySet()

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Split & Extract PDF",
            subtitle = selectedDoc?.let { "${it.title} • ${it.pageCount} pages" },
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("split_lazy_column"),
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
                // Live Functional Motion Visualizer for Split Page Separation
                item {
                    SplitSeparationVisualizer(
                        pageCount = selectedDoc.pageCount,
                        selectedPagesZeroBased = selectedSet,
                        splitMode = splitMode,
                        onTogglePageZeroBased = { pageIdx ->
                            if (splitMode == SplitMode.EVERY_PAGE) {
                                splitMode = SplitMode.CUSTOM_RANGE
                            }
                            if (splitMode == SplitMode.SINGLE_PAGE) {
                                rangeInput = "${pageIdx + 1}"
                            } else {
                                val next = selectedSet.toMutableSet()
                                if (next.contains(pageIdx) && next.size > 1) next.remove(pageIdx)
                                else next.add(pageIdx)
                                rangeInput = next.sorted().map { it + 1 }.joinToString(", ")
                            }
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Choose Split Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SplitMode.entries.forEach { mode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            splitMode = mode
                                            if (mode == SplitMode.SINGLE_PAGE) {
                                                rangeInput = "1"
                                            } else if (mode == SplitMode.CUSTOM_RANGE && rangeInput == "1" && selectedDoc.pageCount > 1) {
                                                rangeInput = "1-2"
                                            }
                                        }
                                        .padding(vertical = 6.dp)
                                        .testTag("split_mode_${mode.name}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = splitMode == mode,
                                        onClick = { splitMode = mode }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            mode.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            mode.helper,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (splitMode != SplitMode.EVERY_PAGE) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, pdflyColors.cardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Select Pages or Enter Range (1..${selectedDoc.pageCount})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = rangeInput,
                                    onValueChange = { rangeInput = it },
                                    label = { Text("Page Range (e.g. 1-2, 4)") },
                                    isError = validationResult.isFailure,
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("split_range_input")
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Interactive Page Pills to toggle pages quickly
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (pageIdx in 0 until selectedDoc.pageCount) {
                                        val isPicked = selectedSet.contains(pageIdx)
                                        FilterChip(
                                            selected = isPicked,
                                            onClick = {
                                                if (splitMode == SplitMode.SINGLE_PAGE) {
                                                    rangeInput = "${pageIdx + 1}"
                                                } else {
                                                    val next = selectedSet.toMutableSet()
                                                    if (isPicked && next.size > 1) next.remove(pageIdx)
                                                    else next.add(pageIdx)
                                                    rangeInput = next.sorted().map { it + 1 }.joinToString(", ")
                                                }
                                            },
                                            label = { Text("Page ${pageIdx + 1}", fontWeight = FontWeight.SemiBold) },
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isPicked,
                                                borderColor = pdflyColors.strongBorder,
                                                selectedBorderColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.testTag("split_page_chip_${pageIdx + 1}")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                if (validationResult.isFailure) {
                                    Text(
                                        text = validationResult.exceptionOrNull()?.message ?: "Invalid range",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    val pages = validationResult.getOrDefault(emptyList())
                                    Text(
                                        text = "✓ Valid selection: ${pages.size} page(s) will be extracted (${pages.map { it + 1 }.joinToString(", ")}).",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = pdflyColors.successText
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = outputBaseName,
                        onValueChange = { outputBaseName = it },
                        label = { Text("Output PDF Filename") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("split_output_filename_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onRunSplit(selectedDoc, splitMode, rangeInput, outputBaseName) },
                        enabled = validationResult.isSuccess,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("execute_split_pdf_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Split & Export PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CompressPdfScreen(
    selectedDoc: PdfDocumentEntity?,
    allDocuments: List<PdfDocumentEntity>,
    defaultPreset: CompressionPreset,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onRunCompress: (PdfDocumentEntity, CompressionPreset, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    var selectedPreset by remember { mutableStateOf(defaultPreset) }
    var outputFileName by remember(selectedDoc?.title, selectedPreset) {
        mutableStateOf("${selectedDoc?.title?.removeSuffix(".pdf") ?: "Document"}_${selectedPreset.name.lowercase()}")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Compress PDF",
            subtitle = "Local Bitmap Downsampling & Stream Optimization",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("compress_lazy_column"),
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
                // Live Functional Motion Visualizer for Compression Stream Density
                item {
                    CompressionDensityVisualizer(
                        inputSizeBytes = selectedDoc.sizeBytes,
                        preset = selectedPreset
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, pdflyColors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Transparency Note: Actual compression ratio depends on document content (such as embedded high-resolution photos vs lightweight vector text).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Select Compression Preset",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        CompressionPreset.entries.forEach { preset ->
                            val isSelected = selectedPreset == preset
                            val cardColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface,
                                label = "preset_card_color"
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pdflyPressable(onClick = { selectedPreset = preset })
                                    .testTag("compress_preset_${preset.name}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = cardColor
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else pdflyColors.cardBorder
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPreset = preset }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = preset.label,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${preset.dpi} DPI • ${preset.jpegQuality}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = preset.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = outputFileName,
                        onValueChange = { outputFileName = it },
                        label = { Text("Compressed Output Filename") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("compress_output_filename_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onRunCompress(selectedDoc, selectedPreset, outputFileName) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("execute_compress_pdf_button")
                    ) {
                        Icon(Icons.Default.Compress, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compress PDF (${LocalFileManager.formatFileSize(selectedDoc.sizeBytes)})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageToPdfScreen(
    pages: List<ScannedPageDraft>,
    defaultPaperSize: PaperSizePreset,
    onPickImagesFromPhotoPicker: () -> Unit,
    onAddSampleGraphicPage: () -> Unit,
    onReorderPage: (Int, Int) -> Unit,
    onRemovePage: (String) -> Unit,
    onExportPdf: (PaperSizePreset, PageOrientation, PageMarginPreset, Int, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    var selectedPaperSize by remember { mutableStateOf(defaultPaperSize) }
    var selectedOrientation by remember { mutableStateOf(PageOrientation.PORTRAIT) }
    var selectedMargin by remember { mutableStateOf(PageMarginPreset.NORMAL) }
    var qualitySlider by remember { mutableFloatStateOf(85f) }
    var outputFileName by remember { mutableStateOf("PDFly_Images_Portfolio") }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Image to PDF Converter",
            subtitle = "${pages.size} image(s) queued • ${selectedPaperSize.label}",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("img2pdf_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Functional Motion Visualizer for Image-to-Page Morphing
            item {
                ImageToPageMorphVisualizer(
                    paperSize = selectedPaperSize,
                    orientation = selectedOrientation,
                    margin = selectedMargin,
                    imageCount = pages.size
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPickImagesFromPhotoPicker,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_gallery_images_button")
                    ) {
                        Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Photos", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onAddSampleGraphicPage,
                        border = BorderStroke(1.dp, pdflyColors.strongBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_diagram_page_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Sample Sheet", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Paper Size, Orientation, Margin & Quality Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Page Layout & Paper Size",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaperSizePreset.entries.forEach { size ->
                                val selected = selectedPaperSize == size
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedPaperSize = size },
                                    label = { Text(size.label, fontWeight = FontWeight.SemiBold) },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = pdflyColors.strongBorder,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("paper_size_${size.name}")
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PageOrientation.entries.forEach { orient ->
                                val selected = selectedOrientation == orient
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedOrientation = orient },
                                    label = { Text(orient.label, fontWeight = FontWeight.SemiBold) },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = pdflyColors.strongBorder,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("orientation_${orient.name}")
                                )
                            }
                        }

                        Text("Page Margins:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PageMarginPreset.entries.forEach { margin ->
                                val selected = selectedMargin == margin
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedMargin = margin },
                                    label = { Text(margin.label, fontWeight = FontWeight.SemiBold) },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = pdflyColors.strongBorder,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("margin_${margin.name}")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Image Render Quality", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "${qualitySlider.roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = qualitySlider,
                            onValueChange = { qualitySlider = it },
                            valueRange = 50f..100f
                        )
                    }
                }
            }

            itemsIndexed(pages, key = { _, p -> p.id }) { index, draft ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                draft.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Source: ${draft.sourceType} • Filter: ${draft.filter.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onReorderPage(index, index - 1) },
                            enabled = index > 0
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                        }
                        IconButton(
                            onClick = { onReorderPage(index, index + 1) },
                            enabled = index < pages.size - 1
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                        }
                        IconButton(onClick = { onRemovePage(draft.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = outputFileName,
                    onValueChange = { outputFileName = it },
                    label = { Text("Output PDF Filename") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("img2pdf_output_filename_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onExportPdf(
                            selectedPaperSize,
                            selectedOrientation,
                            selectedMargin,
                            qualitySlider.roundToInt(),
                            outputFileName
                        )
                    },
                    enabled = pages.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("execute_img2pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Convert ${pages.size} Image(s) to PDF", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
