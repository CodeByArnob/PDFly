package com.example.feature.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.filemanager.LocalFileManager
import com.example.data.local.PdfDocumentEntity
import com.example.domain.model.PdfToolCategory
import com.example.domain.model.PdfToolType
import com.example.feature.TopLevelDestination
import com.example.feature.common.PdfDocumentRowCard
import com.example.feature.common.PdflyTopBar
import com.example.ui.motion.StaggeredEntrance
import com.example.ui.motion.pdflyPressable
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.CrimsonPrimaryLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.pdflyColors
import java.io.File

data class ToolCardVisualSpec(
    val tool: PdfToolType,
    val icon: ImageVector,
    val accentColor: Color,
    val containerColor: Color
)

@Composable
fun rememberAllToolCardSpecs(): List<ToolCardVisualSpec> {
    val pdflyColors = MaterialTheme.pdflyColors
    return remember(pdflyColors) {
        listOf(
            // Read & Edit
            ToolCardVisualSpec(
                tool = PdfToolType.READER,
                icon = Icons.Default.PictureAsPdf,
                accentColor = pdflyColors.crimsonIcon,
                containerColor = pdflyColors.crimsonContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.ANNOTATE,
                icon = Icons.Default.EditNote,
                accentColor = pdflyColors.tealIcon,
                containerColor = pdflyColors.tealContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.SIGNATURE,
                icon = Icons.Default.Draw,
                accentColor = pdflyColors.roseIcon,
                containerColor = pdflyColors.roseContainer
            ),

            // Organize PDF
            ToolCardVisualSpec(
                tool = PdfToolType.MERGE,
                icon = Icons.AutoMirrored.Filled.MergeType,
                accentColor = pdflyColors.indigoIcon,
                containerColor = pdflyColors.indigoContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.SPLIT,
                icon = Icons.AutoMirrored.Filled.CallSplit,
                accentColor = pdflyColors.skyIcon,
                containerColor = pdflyColors.skyContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.ORGANIZE_PAGES,
                icon = Icons.Default.ViewModule,
                accentColor = pdflyColors.violetIcon,
                containerColor = pdflyColors.violetContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.COMPRESS,
                icon = Icons.Default.Compress,
                accentColor = pdflyColors.emeraldIcon,
                containerColor = pdflyColors.emeraldContainer
            ),

            // Convert & Extract
            ToolCardVisualSpec(
                tool = PdfToolType.IMAGE_TO_PDF,
                icon = Icons.Default.Collections,
                accentColor = pdflyColors.violetIcon,
                containerColor = pdflyColors.violetContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.PDF_TO_IMAGE,
                icon = Icons.Default.Image,
                accentColor = pdflyColors.indigoIcon,
                containerColor = pdflyColors.indigoContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.EXTRACT_TEXT,
                icon = Icons.AutoMirrored.Filled.Subject,
                accentColor = pdflyColors.amberIcon,
                containerColor = pdflyColors.amberContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.EXTRACT_IMAGES,
                icon = Icons.Default.PhotoLibrary,
                accentColor = pdflyColors.skyIcon,
                containerColor = pdflyColors.skyContainer
            ),

            // Scan & Stamp
            ToolCardVisualSpec(
                tool = PdfToolType.SCANNER,
                icon = Icons.Default.CameraAlt,
                accentColor = pdflyColors.amberIcon,
                containerColor = pdflyColors.amberContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.WATERMARK,
                icon = Icons.Default.TextFields,
                accentColor = pdflyColors.crimsonIcon,
                containerColor = pdflyColors.crimsonContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.PAGE_NUMBERS,
                icon = Icons.Default.FormatListNumbered,
                accentColor = pdflyColors.emeraldIcon,
                containerColor = pdflyColors.emeraldContainer
            ),

            // Security & Utilities
            ToolCardVisualSpec(
                tool = PdfToolType.SECURITY_LOCK_UNLOCK,
                icon = Icons.Default.Lock,
                accentColor = pdflyColors.tealIcon,
                containerColor = pdflyColors.tealContainer
            ),
            ToolCardVisualSpec(
                tool = PdfToolType.METADATA_VIEWER,
                icon = Icons.Default.Info,
                accentColor = pdflyColors.roseIcon,
                containerColor = pdflyColors.roseContainer
            )
        )
    }
}

@Composable
fun DashboardScreen(
    allDocuments: List<PdfDocumentEntity>,
    recentDocuments: List<PdfDocumentEntity>,
    favoriteDocuments: List<PdfDocumentEntity>,
    historyCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenTool: (PdfToolType) -> Unit,
    onOpenDocumentInReader: (PdfDocumentEntity) -> Unit,
    onImportPdfFromSaf: () -> Unit,
    onToggleFavorite: (PdfDocumentEntity) -> Unit,
    onRenameDocument: (PdfDocumentEntity, String) -> Unit,
    onShareDocument: (File) -> Unit,
    onExportDocumentSaf: (File) -> Unit,
    onDeleteDocument: (PdfDocumentEntity) -> Unit,
    onNavigate: (TopLevelDestination) -> Unit
) {
    val context = LocalContext.current
    val pdflyColors = MaterialTheme.pdflyColors
    val storageSize = remember(allDocuments) {
        val docBytes = allDocuments.sumOf { it.sizeBytes }
        if (docBytes > 0L) docBytes else LocalFileManager.getWorkspaceStorageSizeBytes(context)
    }

    val allToolSpecs = rememberAllToolCardSpecs()
    var selectedCategory by remember { mutableStateOf<PdfToolCategory?>(null) }

    val filteredRecent = remember(recentDocuments, allDocuments, searchQuery) {
        if (searchQuery.isBlank()) {
            recentDocuments.take(5)
        } else {
            allDocuments.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.categoryTag.contains(searchQuery, ignoreCase = true) ||
                    it.searchableSummary.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val categoriesToDisplay = remember(selectedCategory) {
        if (selectedCategory == null) PdfToolCategory.entries
        else listOf(selectedCategory!!)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Clean Top App Header
        item {
            StaggeredEntrance(index = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(CrimsonPrimaryLight, CrimsonPrimaryDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDFly Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PDFly",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "PDF Editor & Complete Offline Toolkit",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, pdflyColors.strongBorder),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .pdflyPressable(onClick = { onNavigate(TopLevelDestination.SUPPORT) })
                                .testTag("header_support_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Support PDFly",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, pdflyColors.strongBorder),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .pdflyPressable(onClick = { onNavigate(TopLevelDestination.SETTINGS) })
                                .testTag("header_settings_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Primary Action Hero Card (Open PDF, Scan Document, All Tools)
        item {
            StaggeredEntrance(index = 1) {
                HeroPrivacyWorkspaceBanner(
                    totalDocs = allDocuments.size,
                    favoritesCount = favoriteDocuments.size,
                    historyCount = historyCount,
                    storageFormatted = LocalFileManager.formatFileSize(storageSize),
                    onImportSafClick = onImportPdfFromSaf,
                    onQuickScanClick = { onOpenTool(PdfToolType.SCANNER) },
                    onBrowseAllToolsClick = { onNavigate(TopLevelDestination.ALL_TOOLS) }
                )
            }
        }

        // 3. Search Bar for Local PDFs
        item {
            StaggeredEntrance(index = 2) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            "Search local PDFs by name, category, or content...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = pdflyColors.strongBorder,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input")
                )
            }
        }

        // 4. Category Filter Bar + "All 16 Tools" Header
        item {
            StaggeredEntrance(index = 3) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PDF Toolkit Categories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "16 local tools • 100% free & offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = { onNavigate(TopLevelDestination.ALL_TOOLS) },
                            modifier = Modifier.testTag("view_all_tools_button")
                        ) {
                            Icon(
                                Icons.Default.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "All 16 Tools",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                softWrap = false
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = {
                                    Text(
                                        "All Categories (16)",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCategory == null,
                                    borderColor = pdflyColors.strongBorder,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("category_chip_all")
                            )
                        }
                        items(PdfToolCategory.entries) { category ->
                            val selected = selectedCategory == category
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedCategory = if (selected) null else category
                                },
                                label = {
                                    Text(
                                        category.title,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = pdflyColors.strongBorder,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("category_chip_${category.id}")
                            )
                        }
                    }
                }
            }
        }

        // 5. Categorized Tool Sections (2-column cards with full 2-line wrapping descriptions)
        categoriesToDisplay.forEachIndexed { catIdx, category ->
            val categorySpecs = allToolSpecs.filter { it.tool.category == category }
            item(key = "category_section_${category.id}") {
                StaggeredEntrance(index = 4 + catIdx) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = category.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                border = BorderStroke(1.dp, pdflyColors.cardBorder)
                            ) {
                                Text(
                                    text = "${categorySpecs.size} tools",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    softWrap = false,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        val rows = categorySpecs.chunked(2)
                        rows.forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEach { spec ->
                                    ToolBentoCard(
                                        spec = spec,
                                        onClick = { onOpenTool(spec.tool) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Quick Workspace Access Chips
        item {
            StaggeredEntrance(index = 9) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickNavChip(
                        icon = Icons.Default.FolderOpen,
                        label = "All Files (${allDocuments.size})",
                        onClick = { onNavigate(TopLevelDestination.DOCUMENTS) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_chip_all_files")
                    )
                    QuickNavChip(
                        icon = Icons.Default.Star,
                        label = "Starred (${favoriteDocuments.size})",
                        onClick = { onNavigate(TopLevelDestination.DOCUMENTS) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_chip_favorites")
                    )
                    QuickNavChip(
                        icon = Icons.Default.History,
                        label = "History ($historyCount)",
                        onClick = { onNavigate(TopLevelDestination.HISTORY) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_chip_history")
                    )
                }
            }
        }

        // 7. Recent Documents Header
        item {
            StaggeredEntrance(index = 10) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "Recent PDF Documents" else "Search Results (${filteredRecent.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = { onNavigate(TopLevelDestination.DOCUMENTS) },
                        modifier = Modifier.testTag("view_all_documents_button")
                    ) {
                        Text(
                            "Manage Files",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (filteredRecent.isEmpty()) {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No Recent Documents Yet" else "No Matching Documents Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Open a PDF from your device using Android Storage Access Framework or scan a paper document.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onImportPdfFromSaf) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import PDF via SAF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredRecent, key = { it.id }) { doc ->
                PdfDocumentRowCard(
                    doc = doc,
                    onOpen = { onOpenDocumentInReader(doc) },
                    onToggleFavorite = { onToggleFavorite(doc) },
                    onRename = { newName -> onRenameDocument(doc, newName) },
                    onShare = { onShareDocument(File(doc.filePath)) },
                    onExportSaf = { onExportDocumentSaf(File(doc.filePath)) },
                    onDelete = { onDeleteDocument(doc) }
                )
            }
        }

        // 8. Non-Intrusive Voluntary Community Support Card
        item {
            StaggeredEntrance(index = 12) {
                VoluntarySupportCalloutCard(
                    onClick = { onNavigate(TopLevelDestination.SUPPORT) }
                )
            }
        }
    }
}

/**
 * Full-screen catalog of all 16 PDF tools with instant search and category filtering.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllPdfToolsCatalogScreen(
    onOpenTool: (PdfToolType) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors
    val allToolSpecs = rememberAllToolCardSpecs()
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PdfToolCategory?>(null) }

    val filteredSpecs = remember(allToolSpecs, query, selectedCategory) {
        allToolSpecs.filter { spec ->
            val matchesCat = selectedCategory == null || spec.tool.category == selectedCategory
            val matchesQuery = query.isBlank() ||
                spec.tool.title.contains(query, ignoreCase = true) ||
                spec.tool.subtitle.contains(query, ignoreCase = true) ||
                spec.tool.badge.contains(query, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "All 16 PDF Tools",
            subtitle = "Complete Local PDF Toolkit • 100% Free & Offline",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("all_tools_catalog_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Filter tools (e.g. Merge, Watermark, OCR Text, Rotate)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("all_tools_search_input")
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${allToolSpecs.size})", fontWeight = FontWeight.SemiBold) }
                    )
                    PdfToolCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label = { Text(cat.title, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            val grouped = filteredSpecs.groupBy { it.tool.category }
            grouped.forEach { (category, specs) ->
                item(key = "catalog_cat_${category.id}") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = category.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        specs.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEach { spec ->
                                    ToolBentoCard(
                                        spec = spec,
                                        onClick = { onOpenTool(spec.tool) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPrivacyWorkspaceBanner(
    totalDocs: Int,
    favoritesCount: Int,
    historyCount: Int,
    storageFormatted: String,
    onImportSafClick: () -> Unit,
    onQuickScanClick: () -> Unit,
    onBrowseAllToolsClick: () -> Unit
) {
    val pdflyColors = MaterialTheme.pdflyColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_privacy_banner"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFB7185).copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.2f),
                        radius = size.width * 0.55f
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.2f),
                    radius = size.width * 0.55f
                )
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF065F46),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF6EE7B7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% FREE & OFFLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                softWrap = false,
                                maxLines = 1
                            )
                        }
                    }

                    Text(
                        text = "$totalDocs PDFs • $storageFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0),
                        softWrap = false,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Professional On-Device PDF Suite",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Read, annotate, sign, merge, split, organize, watermark, convert, and inspect PDFs locally on your Android device with zero cloud uploads.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onImportSafClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE11D48),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hero_import_pdf_button")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open PDF", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onQuickScanClick,
                        border = BorderStroke(1.5.dp, Color(0xFF94A3B8)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hero_quick_scan_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Page", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ToolBentoCard(
    spec: ToolCardVisualSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pdflyColors = MaterialTheme.pdflyColors

    Card(
        modifier = modifier
            .pdflyPressable(onClick = onClick)
            .testTag("tool_card_${spec.tool.id}"),
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
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(spec.containerColor)
                        .border(1.dp, spec.accentColor.copy(alpha = 0.30f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = spec.icon,
                        contentDescription = spec.tool.title,
                        tint = spec.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Surface(
                    color = spec.containerColor,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, spec.accentColor.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = spec.tool.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = spec.accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = spec.tool.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = spec.tool.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickNavChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pdflyColors = MaterialTheme.pdflyColors

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, pdflyColors.strongBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .height(48.dp)
            .pdflyPressable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VoluntarySupportCalloutCard(onClick: () -> Unit) {
    val pdflyColors = MaterialTheme.pdflyColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pdflyPressable(onClick = onClick)
            .testTag("dashboard_support_callout_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, pdflyColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Support PDFly (100% Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Every PDF tool is free forever with zero ads or paywalls. Support independent development via voluntary USDT or USDC (BEP-20).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
