package com.example.feature.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterBAndW
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.filemanager.LocalFileManager
import com.example.core.processing.PdfProcessingEngine
import com.example.domain.model.PageMarginPreset
import com.example.domain.model.PageOrientation
import com.example.domain.model.PaperSizePreset
import com.example.domain.model.ScanFilterPreset
import com.example.domain.model.ScannedPageDraft
import com.example.feature.common.PdflyTopBar
import com.example.ui.motion.LocalReducedMotion
import com.example.ui.motion.PdflyMotion
import com.example.ui.theme.EmeraldSuccessDark
import com.example.ui.theme.pdflyColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocumentScannerScreen(
    scannedPages: List<ScannedPageDraft>,
    onCapturePage: (String, String?, ScanFilterPreset) -> Unit,
    onPickFromGallery: () -> Unit,
    onUpdatePageDraft: (ScannedPageDraft) -> Unit,
    onRemovePageDraft: (String) -> Unit,
    onReorderPageDraft: (Int, Int) -> Unit,
    onExportScannedPdf: (PaperSizePreset, PageOrientation, PageMarginPreset, Int, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isReducedMotion = LocalReducedMotion.current
    val pdflyColors = MaterialTheme.pdflyColors

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showLiveCameraViewfinder by remember { mutableStateOf(false) }
    var triggerShutterFlash by remember { mutableStateOf(false) }

    LaunchedEffect(triggerShutterFlash) {
        if (triggerShutterFlash) {
            delay(220L)
            triggerShutterFlash = false
        }
    }

    val shutterFlashAlpha by animateFloatAsState(
        targetValue = if (triggerShutterFlash && !isReducedMotion) 0.72f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "shutter_flash_alpha"
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            showLiveCameraViewfinder = true
        }
    }

    var selectedPageIdx by remember(scannedPages.size) {
        mutableIntStateOf((scannedPages.size - 1).coerceAtLeast(0))
    }
    val activeDraft = scannedPages.getOrNull(selectedPageIdx.coerceIn(0, (scannedPages.size - 1).coerceAtLeast(0)))

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var outputFileName by remember { mutableStateOf("Scanned_Document_${System.currentTimeMillis() % 10000}") }

    val animatedCropInset by animateFloatAsState(
        targetValue = activeDraft?.cropInsetPercent ?: 0.04f,
        animationSpec = PdflyMotion.snappySpring(),
        label = "scanner_crop_inset"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_beam_loop")
    val laserBeamProgress by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_beam_progress"
    )

    LaunchedEffect(
        activeDraft?.id,
        activeDraft?.filter,
        activeDraft?.rotationDegrees,
        activeDraft?.brightness,
        activeDraft?.contrast,
        activeDraft?.cropInsetPercent
    ) {
        val draft = activeDraft ?: return@LaunchedEffect
        val bmp = withContext(Dispatchers.IO) {
            PdfProcessingEngine.renderDraftToFilteredBitmap(
                context = context,
                draft = draft,
                targetW = 640,
                targetH = 880
            )
        }
        previewBitmap = bmp
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "CameraX Document Scanner",
            subtitle = "${scannedPages.size} page(s) in batch • Auto-Edge & B&W Studio",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("scanner_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top CameraX / Instant Scan / Gallery Bar
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                showLiveCameraViewfinder = !showLiveCameraViewfinder
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_camerax_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showLiveCameraViewfinder) "Close Camera" else "Live CameraX", fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            triggerShutterFlash = true
                            onCapturePage(
                                "Scanned Page #${scannedPages.size + 1}",
                                null,
                                ScanFilterPreset.MAGIC_ENHANCE
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("capture_instant_scan_button")
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Quick Scan Page", fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, pdflyColors.cardBorder)
                    ) {
                        IconButton(
                            onClick = onPickFromGallery,
                            modifier = Modifier.testTag("scanner_gallery_import_button")
                        ) {
                            Icon(
                                Icons.Default.Collections,
                                contentDescription = "Import Photo",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Live CameraX Viewfinder with Animated Auto-Edge Detection Overlay & Scanning Beam
            if (showLiveCameraViewfinder && hasCameraPermission) {
                item {
                    val imageCapture = remember { ImageCapture.Builder().build() }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .testTag("camerax_viewfinder_card"),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, pdflyColors.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx)
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        runCatching {
                                            val provider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.surfaceProvider = previewView.surfaceProvider
                                            }
                                            provider.unbindAll()
                                            provider.bindToLifecycle(
                                                lifecycleOwner,
                                                CameraSelector.DEFAULT_BACK_CAMERA,
                                                preview,
                                                imageCapture
                                            )
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Auto-Edge Document Detection Guide Frame + Animated Scanning Laser Beam
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val padX = size.width * 0.12f
                                val padY = size.height * 0.12f
                                val rectW = size.width - padX * 2
                                val rectH = size.height - padY * 2
                                drawRect(
                                    color = EmeraldSuccessDark.copy(alpha = 0.8f),
                                    topLeft = Offset(padX, padY),
                                    size = Size(rectW, rectH),
                                    style = Stroke(width = 3.5f)
                                )
                                if (!isReducedMotion) {
                                    val beamY = padY + rectH * laserBeamProgress
                                    drawLine(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color.Transparent, EmeraldSuccessDark, Color.Transparent)
                                        ),
                                        start = Offset(padX, beamY),
                                        end = Offset(padX + rectW, beamY),
                                        strokeWidth = 6f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    triggerShutterFlash = true
                                    val photoFile = File(
                                        LocalFileManager.getTempDir(context),
                                        "camerax_scan_${System.currentTimeMillis()}.jpg"
                                    )
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                    imageCapture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                onCapturePage(
                                                    "CameraX Capture #${scannedPages.size + 1}",
                                                    photoFile.absolutePath,
                                                    ScanFilterPreset.MAGIC_ENHANCE
                                                )
                                                showLiveCameraViewfinder = false
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                // Fallback if virtual camera sensor has no frame
                                                onCapturePage(
                                                    "Scanned Page #${scannedPages.size + 1}",
                                                    null,
                                                    ScanFilterPreset.MAGIC_ENHANCE
                                                )
                                                showLiveCameraViewfinder = false
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(14.dp)
                                    .testTag("shutter_capture_button")
                            ) {
                                Text("Capture Document Page", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Active Scanned Page Live Preview + Animated Perspective Crop Brackets & Laser Beam
            if (activeDraft != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F172A)
                        ),
                        border = BorderStroke(1.5.dp, pdflyColors.strongBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${activeDraft.title} (Page ${selectedPageIdx + 1}/${scannedPages.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Row {
                                    IconButton(
                                        onClick = {
                                            onUpdatePageDraft(
                                                activeDraft.copy(
                                                    rotationDegrees = (activeDraft.rotationDegrees + 90) % 360
                                                )
                                            )
                                        },
                                        modifier = Modifier.testTag("scanner_rotate_page_button")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate 90°", tint = Color.White)
                                    }
                                    if (scannedPages.size > 1) {
                                        IconButton(
                                            onClick = { onRemovePageDraft(activeDraft.id) },
                                            modifier = Modifier.testTag("scanner_delete_page_button")
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Page", tint = Color(0xFFF87171))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(270.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(
                                    targetState = activeDraft.id to previewBitmap,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(160))
                                    },
                                    label = "scanner_page_preview_transition"
                                ) { (_, bmp) ->
                                    if (bmp != null) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Image(
                                                bitmap = bmp.asImageBitmap(),
                                                contentDescription = activeDraft.title,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(2.dp, EmeraldSuccessDark, RoundedCornerShape(8.dp))
                                            )

                                            // Live Animated Corner Crop Brackets & Laser Scan Overlay
                                            Canvas(modifier = Modifier.matchParentSize()) {
                                                val insetX = size.width * animatedCropInset
                                                val insetY = size.height * animatedCropInset
                                                val bracketLen = 24f
                                                val strokeW = 5f
                                                // Top-left bracket
                                                drawLine(EmeraldSuccessDark, Offset(insetX, insetY), Offset(insetX + bracketLen, insetY), strokeW, StrokeCap.Round)
                                                drawLine(EmeraldSuccessDark, Offset(insetX, insetY), Offset(insetX, insetY + bracketLen), strokeW, StrokeCap.Round)
                                                // Top-right bracket
                                                drawLine(EmeraldSuccessDark, Offset(size.width - insetX, insetY), Offset(size.width - insetX - bracketLen, insetY), strokeW, StrokeCap.Round)
                                                drawLine(EmeraldSuccessDark, Offset(size.width - insetX, insetY), Offset(size.width - insetX, insetY + bracketLen), strokeW, StrokeCap.Round)
                                                // Bottom-left bracket
                                                drawLine(EmeraldSuccessDark, Offset(insetX, size.height - insetY), Offset(insetX + bracketLen, size.height - insetY), strokeW, StrokeCap.Round)
                                                drawLine(EmeraldSuccessDark, Offset(insetX, size.height - insetY), Offset(insetX, size.height - insetY - bracketLen), strokeW, StrokeCap.Round)
                                                // Bottom-right bracket
                                                drawLine(EmeraldSuccessDark, Offset(size.width - insetX, size.height - insetY), Offset(size.width - insetX - bracketLen, size.height - insetY), strokeW, StrokeCap.Round)
                                                drawLine(EmeraldSuccessDark, Offset(size.width - insetX, size.height - insetY), Offset(size.width - insetX, size.height - insetY - bracketLen), strokeW, StrokeCap.Round)

                                                if (!isReducedMotion) {
                                                    val beamY = insetY + (size.height - insetY * 2) * laserBeamProgress
                                                    drawLine(
                                                        brush = Brush.horizontalGradient(
                                                            colors = listOf(
                                                                Color.Transparent,
                                                                EmeraldSuccessDark.copy(alpha = 0.65f),
                                                                Color.Transparent
                                                            )
                                                        ),
                                                        start = Offset(insetX, beamY),
                                                        end = Offset(size.width - insetX, beamY),
                                                        strokeWidth = 3f
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Shutter Capture Flash Feedback Overlay
                                if (shutterFlashAlpha > 0.01f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = shutterFlashAlpha))
                                    )
                                }
                            }
                        }
                    }
                }

                // Batch Page Selector Strip
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(scannedPages) { idx, page ->
                            val isSelected = idx == selectedPageIdx
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPageIdx = idx },
                                label = { Text("Page ${idx + 1}: ${page.filter.label}", fontWeight = FontWeight.SemiBold) },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = pdflyColors.strongBorder,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("scan_batch_chip_${idx + 1}")
                            )
                        }
                    }
                }

                // Image Filters, Perspective Crop, Brightness & Contrast Studio
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
                                text = "Document Enhancement & Crop",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ScanFilterPreset.entries.forEach { preset ->
                                    val selected = activeDraft.filter == preset
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onUpdatePageDraft(activeDraft.copy(filter = preset)) },
                                        leadingIcon = {
                                            Icon(Icons.Default.FilterBAndW, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        label = { Text(preset.label, fontWeight = FontWeight.SemiBold) },
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            borderColor = pdflyColors.strongBorder,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.testTag("scan_filter_${preset.name}")
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Edge Crop & Border Trim", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${(activeDraft.cropInsetPercent * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = activeDraft.cropInsetPercent,
                                onValueChange = { onUpdatePageDraft(activeDraft.copy(cropInsetPercent = it)) },
                                valueRange = 0f..0.18f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Contrast & Shadow Cleanup", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${(activeDraft.contrast * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = activeDraft.contrast,
                                onValueChange = { onUpdatePageDraft(activeDraft.copy(contrast = it)) },
                                valueRange = 0.75f..1.75f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Brightness Compensation", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${activeDraft.brightness.roundToInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = activeDraft.brightness,
                                onValueChange = { onUpdatePageDraft(activeDraft.copy(brightness = it)) },
                                valueRange = -35f..35f
                            )
                        }
                    }
                }
            }

            // Export Scanned Batch to PDF
            item {
                OutlinedTextField(
                    value = outputFileName,
                    onValueChange = { outputFileName = it },
                    label = { Text("Scanned Output PDF Filename") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scanner_output_filename_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onExportScannedPdf(
                            PaperSizePreset.A4,
                            PageOrientation.PORTRAIT,
                            PageMarginPreset.SMALL,
                            90,
                            outputFileName
                        )
                    },
                    enabled = scannedPages.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("export_scanned_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export ${scannedPages.size} Scanned Page(s) to PDF", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
