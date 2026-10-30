package com.example.feature.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.filemanager.LocalFileManager
import com.example.data.local.PdfDocumentEntity
import com.example.feature.ActiveProcessingState
import com.example.ui.motion.PdflyMotion
import com.example.ui.motion.pdflyPressable
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.pdflyColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdflyTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    val pdflyColors = MaterialTheme.pdflyColors
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .pdflyPressable()
                        .testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = { actions() },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = pdflyColors.cardBorder
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProcessingStatusModal(
    state: ActiveProcessingState,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    onOpenDocument: (PdfDocumentEntity) -> Unit,
    onShareFile: (File) -> Unit,
    onSaveToDeviceSaf: (File) -> Unit
) {
    val showModal = state.isRunning || state.completedResult != null
    if (!showModal) return

    val pdflyColors = MaterialTheme.pdflyColors
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress.coerceIn(0.03f, 1f),
        animationSpec = PdflyMotion.snappySpring(),
        label = "real_processing_progress"
    )

    Dialog(onDismissRequest = { if (!state.isRunning) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("processing_status_modal"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, pdflyColors.cardBorder),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            AnimatedContent(
                targetState = state.isRunning,
                transitionSpec = {
                    (fadeIn(tween(PdflyMotion.STANDARD_MS)) +
                        scaleIn(initialScale = 0.94f, animationSpec = PdflyMotion.tactileSpring())) togetherWith
                        fadeOut(tween(PdflyMotion.FAST_MS))
                },
                label = "processing_modal_state"
            ) { isRunning ->
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isRunning) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = state.operationTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.statusDetail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(animatedProgress * 100).toInt()}% • Local On-Device Engine",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = onCancel,
                            border = BorderStroke(1.dp, pdflyColors.strongBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_processing_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel Safely", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        val result = state.completedResult ?: return@Column
                        val iconTint = if (result.success) pdflyColors.successText else MaterialTheme.colorScheme.error
                        val iconBg = if (result.success) pdflyColors.successContainer else MaterialTheme.colorScheme.errorContainer
                        val iconBorder = if (result.success) pdflyColors.successBorder else MaterialTheme.colorScheme.error
                        val badgeScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = PdflyMotion.tactileSpring(),
                            label = "result_badge_scale"
                        )
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = badgeScale
                                    scaleY = badgeScale
                                }
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(iconBg)
                                .border(2.dp, iconBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = state.operationTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (result.success && result.primaryOutputFile != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, pdflyColors.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = result.primaryOutputFile.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Pages: ${result.totalPageCount} • Output Size: ${
                                            LocalFileManager.formatFileSize(result.outputSizeBytes)
                                        }" + if (result.inputSizeBytes > 0) {
                                            " (Input: ${LocalFileManager.formatFileSize(result.inputSizeBytes)})"
                                        } else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (state.completedDoc != null) {
                                    Button(
                                        onClick = {
                                            val doc = state.completedDoc
                                            onDismiss()
                                            onOpenDocument(doc)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("open_output_pdf_button")
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open PDF", fontWeight = FontWeight.Bold)
                                    }
                                }
                                FilledTonalButton(
                                    onClick = { onShareFile(result.primaryOutputFile) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_output_pdf_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share", fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onSaveToDeviceSaf(result.primaryOutputFile) },
                                border = BorderStroke(1.dp, pdflyColors.strongBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_saf_output_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Copy to Device Storage (SAF)", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dismiss_processing_modal_button")
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveDocumentSelectorCard(
    selectedDoc: PdfDocumentEntity?,
    allDocs: List<PdfDocumentEntity>,
    onSelectDoc: (PdfDocumentEntity) -> Unit,
    onImportFromSaf: () -> Unit
) {
    var showPickerDialog by remember { mutableStateOf(false) }
    val pdflyColors = MaterialTheme.pdflyColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_document_selector_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, pdflyColors.cardBorder),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AnimatedContent(
                targetState = selectedDoc,
                transitionSpec = {
                    fadeIn(tween(PdflyMotion.FAST_MS)) togetherWith fadeOut(tween(PdflyMotion.FAST_MS))
                },
                label = "active_doc_header"
            ) { doc ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(pdflyColors.crimsonContainer)
                                .border(1.dp, pdflyColors.crimsonIcon.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = pdflyColors.crimsonIcon
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TARGET PDF DOCUMENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = doc?.title ?: "No document selected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (doc != null) {
                                Text(
                                    text = "${doc.pageCount} pages • ${LocalFileManager.formatFileSize(doc.sizeBytes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { showPickerDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("switch_workspace_pdf_button")
                ) {
                    Text("Switch PDF (${allDocs.size})", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onImportFromSaf,
                    border = BorderStroke(1.dp, pdflyColors.strongBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_device_pdf_button")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Browse Device", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showPickerDialog) {
        AlertDialog(
            onDismissRequest = { showPickerDialog = false },
            title = { Text("Select Workspace PDF", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allDocs.forEach { doc ->
                        val isSelected = doc.id == selectedDoc?.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else pdflyColors.cardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectDoc(doc)
                                    showPickerDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(10.dp))
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
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPickerDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun PdfDocumentRowCard(
    doc: PdfDocumentEntity,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: (String) -> Unit,
    onShare: () -> Unit,
    onExportSaf: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var renameInput by remember(doc.title) { mutableStateOf(doc.title.removeSuffix(".pdf")) }
    val pdflyColors = MaterialTheme.pdflyColors

    val favScale by animateFloatAsState(
        targetValue = if (doc.isFavorite) 1.16f else 1f,
        animationSpec = PdflyMotion.tactileSpring(),
        label = "fav_star_scale"
    )
    val favTint by animateColorAsState(
        targetValue = if (doc.isFavorite) pdflyColors.amberIcon else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(PdflyMotion.FAST_MS),
        label = "fav_star_tint"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pdflyPressable(pressedScale = 0.98f, enableHaptics = true, onClick = onOpen)
            .testTag("pdf_doc_card_${doc.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, pdflyColors.cardBorder),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(pdflyColors.crimsonContainer)
                    .border(
                        width = 1.dp,
                        color = pdflyColors.crimsonIcon.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PDF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = pdflyColors.crimsonIcon
                    )
                    Text(
                        text = "${doc.pageCount}p",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (doc.bookmarkedPages.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Has bookmarks",
                            tint = pdflyColors.amberIcon,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, pdflyColors.cardBorder),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = doc.categoryTag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${LocalFileManager.formatFileSize(doc.sizeBytes)} • Page ${doc.lastReadPage + 1}/${doc.pageCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = favScale
                        scaleY = favScale
                    }
                    .testTag("fav_button_${doc.id}")
            ) {
                Icon(
                    imageVector = if (doc.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (doc.isFavorite) "Remove Favorite" else "Mark Favorite",
                    tint = favTint
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("more_menu_button_${doc.id}")
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Document options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open in Reader", fontWeight = FontWeight.Medium) },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onOpen()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename PDF", fontWeight = FontWeight.Medium) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            showRenameDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share PDF", fontWeight = FontWeight.Medium) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export via SAF", fontWeight = FontWeight.Medium) },
                        leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onExportSaf()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("File Details", fontWeight = FontWeight.Medium) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            showDetailsDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            showDeleteConfirm = true
                        }
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename PDF Document", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Filename (without .pdf)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_pdf_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRename(renameInput)
                        showRenameDialog = false
                    },
                    modifier = Modifier.testTag("confirm_rename_button")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete from PDFly Workspace?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This removes '${doc.title}' from PDFly's local workspace storage. Original files outside the app remain untouched.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = { Text("PDF File Metadata", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Filename: ${doc.title}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Size: ${LocalFileManager.formatFileSize(doc.sizeBytes)} (${doc.sizeBytes} bytes)", style = MaterialTheme.typography.labelSmall)
                    Text("Page Count: ${doc.pageCount} pages", style = MaterialTheme.typography.bodySmall)
                    Text("Reading Progress: Page ${doc.lastReadPage + 1} of ${doc.pageCount}", style = MaterialTheme.typography.bodySmall)
                    Text("Bookmarks: ${if (doc.bookmarkedPages.isEmpty()) "None" else doc.bookmarkedPages.map { it + 1 }.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    Text("Last Opened: ${LocalFileManager.formatTimestamp(doc.lastOpenedAt)}", style = MaterialTheme.typography.bodySmall)
                    Text("Local Path: ${doc.filePath}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
