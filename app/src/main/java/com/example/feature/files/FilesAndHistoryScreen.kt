package com.example.feature.files

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.filemanager.LocalFileManager
import com.example.data.local.PdfDocumentEntity
import com.example.data.local.ProcessingHistoryEntity
import com.example.domain.model.DocumentSortOption
import com.example.feature.common.PdfDocumentRowCard
import com.example.feature.common.PdflyTopBar
import com.example.ui.motion.StaggeredEntrance
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.pdflyColors
import java.io.File

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DocumentsManagerScreen(
    allDocuments: List<PdfDocumentEntity>,
    favoriteDocuments: List<PdfDocumentEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onImportFromSaf: () -> Unit,
    onOpenDocument: (PdfDocumentEntity) -> Unit,
    onToggleFavorite: (PdfDocumentEntity) -> Unit,
    onRenameDocument: (PdfDocumentEntity, String) -> Unit,
    onShareFile: (File) -> Unit,
    onExportSafFile: (File) -> Unit,
    onDeleteDocument: (PdfDocumentEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    var favoritesOnly by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(DocumentSortOption.RECENT) }
    var selectedCategoryTag by remember { mutableStateOf<String?>(null) }

    val availableCategories = remember(allDocuments) {
        allDocuments.map { it.categoryTag }.filter { it.isNotBlank() }.distinct()
    }

    val sourceList = if (favoritesOnly) favoriteDocuments else allDocuments
    val filtered = remember(sourceList, searchQuery, sortOption, selectedCategoryTag) {
        val step1 = if (searchQuery.isBlank()) sourceList
        else sourceList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.categoryTag.contains(searchQuery, ignoreCase = true) ||
                it.searchableSummary.contains(searchQuery, ignoreCase = true)
        }
        val step2 = if (selectedCategoryTag == null) step1
        else step1.filter { it.categoryTag.equals(selectedCategoryTag, ignoreCase = true) }

        when (sortOption) {
            DocumentSortOption.RECENT -> step2.sortedByDescending { it.lastOpenedAt }
            DocumentSortOption.NAME_ASC -> step2.sortedBy { it.title.lowercase() }
            DocumentSortOption.SIZE_DESC -> step2.sortedByDescending { it.sizeBytes }
            DocumentSortOption.PAGES_DESC -> step2.sortedByDescending { it.pageCount }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Documents & Favorites",
            subtitle = "${allDocuments.size} local PDFs • ${favoriteDocuments.size} starred • Sort: ${sortOption.label}",
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = onImportFromSaf,
                    modifier = Modifier.testTag("docs_import_saf_button")
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = "Import PDF",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StaggeredEntrance(index = 0) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                "Search documents by filename, category, or content...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = pdflyColors.strongBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("docs_search_input")
                    )
                }
            }

            item {
                StaggeredEntrance(index = 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = !favoritesOnly,
                                    onClick = { favoritesOnly = false },
                                    label = { Text("All Files (${allDocuments.size})", fontWeight = FontWeight.SemiBold) },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = !favoritesOnly,
                                        borderColor = pdflyColors.strongBorder,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                FilterChip(
                                    selected = favoritesOnly,
                                    onClick = { favoritesOnly = true },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Starred (${favoriteDocuments.size})", fontWeight = FontWeight.SemiBold) },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = favoritesOnly,
                                        borderColor = pdflyColors.strongBorder,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            TextButton(onClick = onImportFromSaf) {
                                Text("+ Import PDF", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Sort Options Row
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DocumentSortOption.entries.forEach { option ->
                                val isSel = sortOption == option
                                FilterChip(
                                    selected = isSel,
                                    onClick = { sortOption = option },
                                    label = { Text("Sort: ${option.label}", fontWeight = FontWeight.SemiBold) },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSel,
                                        borderColor = pdflyColors.strongBorder,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("docs_sort_${option.name}")
                                )
                            }
                        }

                        // Category Filter Row
                        if (availableCategories.size > 1) {
                            androidx.compose.foundation.layout.FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedCategoryTag == null,
                                    onClick = { selectedCategoryTag = null },
                                    label = { Text("All Tags", fontWeight = FontWeight.SemiBold) }
                                )
                                availableCategories.forEach { tag ->
                                    FilterChip(
                                        selected = selectedCategoryTag == tag,
                                        onClick = {
                                            selectedCategoryTag = if (selectedCategoryTag == tag) null else tag
                                        },
                                        label = { Text(tag, fontWeight = FontWeight.SemiBold) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Documents Match Your Filter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Import a PDF from your device using Android Storage Access Framework (SAF).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(onClick = onImportFromSaf) {
                                Text("Browse Device PDFs", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { doc ->
                    Box(modifier = Modifier.animateItem()) {
                        PdfDocumentRowCard(
                            doc = doc,
                            onOpen = { onOpenDocument(doc) },
                            onToggleFavorite = { onToggleFavorite(doc) },
                            onRename = { newName -> onRenameDocument(doc, newName) },
                            onShare = { onShareFile(File(doc.filePath)) },
                            onExportSaf = { onExportSafFile(File(doc.filePath)) },
                            onDelete = { onDeleteDocument(doc) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessingHistoryScreen(
    historyEntries: List<ProcessingHistoryEntity>,
    onClearHistory: () -> Unit,
    onShareFile: (File) -> Unit,
    onExportSafFile: (File) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Processing History",
            subtitle = "${historyEntries.size} local operations recorded",
            onBack = onBack,
            actions = {
                if (historyEntries.isNotEmpty()) {
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (historyEntries.isEmpty()) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Processing History Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Operations like Merge, Split, Compress, Scan, and Signature stamping will appear here with before/after file sizes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(historyEntries, key = { it.id }) { item ->
                    val outFile = File(item.outputFilePath)
                    val fileExists = item.outputFilePath.isNotBlank() && outFile.exists()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .testTag("history_item_${item.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(pdflyColors.successContainer)
                                            .border(1.dp, pdflyColors.successBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = pdflyColors.successText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.operationType,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.outputFileName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Text(
                                    text = LocalFileManager.formatTimestamp(item.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.detailsMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = pdflyColors.subtleSurface,
                                border = BorderStroke(1.dp, pdflyColors.cardBorder)
                            ) {
                                Text(
                                    text = "Pages: ${item.pageCount} • Input: ${LocalFileManager.formatFileSize(item.inputSizeBytes)} → Output: ${LocalFileManager.formatFileSize(item.outputSizeBytes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            if (fileExists) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onShareFile(outFile) },
                                        border = BorderStroke(1.dp, pdflyColors.strongBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share", fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { onExportSafFile(outFile) },
                                        border = BorderStroke(1.dp, pdflyColors.strongBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save (SAF)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
