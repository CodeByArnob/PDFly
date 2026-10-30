package com.example.feature.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.filemanager.LocalFileManager
import com.example.data.local.PdfDocumentEntity
import com.example.domain.model.CompressionPreset
import com.example.domain.model.PageMarginPreset
import com.example.domain.model.PageOrientation
import com.example.domain.model.PaperSizePreset
import com.example.domain.model.SplitMode
import com.example.ui.motion.LocalReducedMotion
import com.example.ui.motion.PdflyMotion
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.pdflyColors
import kotlin.math.roundToInt

/**
 * 1. MERGE PDF FUNCTIONAL MOTION VISUALIZER:
 * Animates the user's selected PDF documents on the left flowing along Bezier paths
 * into a combined multi-page master document on the right, reflecting real page counts.
 */
@Composable
fun MergeConvergenceVisualizer(
    mergeQueue: List<PdfDocumentEntity>,
    modifier: Modifier = Modifier
) {
    val reducedMotion = LocalReducedMotion.current
    val pdflyColors = MaterialTheme.pdflyColors
    val totalPages = mergeQueue.sumOf { it.pageCount }
    val totalBytes = mergeQueue.sumOf { it.sizeBytes }

    val pulsePhase = if (reducedMotion) {
        0.5f
    } else {
        val infinite = rememberInfiniteTransition(label = "merge_flow")
        val anim by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "flow_phase"
        )
        anim
    }

    val sheetColors = listOf(
        pdflyColors.crimsonIcon,
        pdflyColors.indigoIcon,
        pdflyColors.emeraldIcon,
        pdflyColors.amberIcon,
        pdflyColors.skyIcon
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("merge_functional_visualizer"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, pdflyColors.cardBorder),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.MergeType,
                        contentDescription = null,
                        tint = pdflyColors.indigoIcon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Merge Pipeline Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = pdflyColors.indigoContainer,
                    border = BorderStroke(1.dp, pdflyColors.indigoIcon.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${mergeQueue.size} Docs → $totalPages Pages",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = pdflyColors.indigoIcon,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(pdflyColors.subtleSurface)
                    .border(1.dp, pdflyColors.cardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left Column: Non-overlapping Source Document Queue Badges
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.width(82.dp)
                ) {
                    mergeQueue.take(3).forEachIndexed { idx, doc ->
                        val badgeColor = sheetColors[idx % sheetColors.size]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.65f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                                Text(
                                    text = "${doc.pageCount}p",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (mergeQueue.size > 3) {
                        Text(
                            text = "+${mergeQueue.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Center Canvas: Dedicated Bezier Convergence Stream
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(92.dp)
                ) {
                    val count = mergeQueue.size.coerceIn(1, 5)
                    val startX = 12f
                    val targetX = (size.width - 12f).coerceAtLeast(24f)
                    val targetY = size.height * 0.5f

                    for (i in 0 until count) {
                        val color = sheetColors[i % sheetColors.size]
                        val fraction = if (count == 1) 0.5f else i.toFloat() / (count - 1).toFloat()
                        val startY = size.height * (0.16f + fraction * 0.68f)

                        val path = Path().apply {
                            moveTo(startX, startY)
                            cubicTo(
                                size.width * 0.42f,
                                startY,
                                size.width * 0.58f,
                                targetY,
                                targetX,
                                targetY
                            )
                        }
                        drawPath(
                            path = path,
                            color = color.copy(alpha = 0.65f),
                            style = Stroke(
                                width = 4f,
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), -pulsePhase * 36f)
                            )
                        )

                        val t = ((pulsePhase + i * 0.22f) % 1f)
                        val dotX = startX + (targetX - startX) * t
                        val dotY = startY + (targetY - startY) * (t * t * (3f - 2f * t))
                        drawCircle(
                            color = color,
                            radius = 5.5f,
                            center = Offset(dotX, dotY)
                        )
                    }
                }

                // Right Column: High-Contrast Combined Output Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = pdflyColors.indigoContainer,
                    border = BorderStroke(1.5.dp, pdflyColors.indigoIcon),
                    modifier = Modifier.width(92.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MERGED",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = pdflyColors.indigoIcon
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalPages Pages",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LocalFileManager.formatFileSize(totalBytes),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. SPLIT PDF FUNCTIONAL MOTION VISUALIZER:
 * Visually displays each page of the target PDF and animates selected pages separating
 * upward and glowing when included in the extraction range.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SplitSeparationVisualizer(
    pageCount: Int,
    selectedPagesZeroBased: Set<Int>,
    splitMode: SplitMode,
    onTogglePageZeroBased: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleCount = pageCount.coerceIn(1, 12)
    val pdflyColors = MaterialTheme.pdflyColors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("split_functional_visualizer"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, pdflyColors.cardBorder),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.CallSplit,
                        contentDescription = null,
                        tint = pdflyColors.skyIcon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interactive Page Separation Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = pdflyColors.skyContainer,
                    border = BorderStroke(1.dp, pdflyColors.skyIcon.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${selectedPagesZeroBased.size} of $pageCount selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = pdflyColors.skyIcon,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (splitMode == SplitMode.EVERY_PAGE) {
                    "All $pageCount pages will separate into $pageCount individual PDF files."
                } else {
                    "Tap any page sheet below to toggle extraction. Selected pages lift out of the deck:"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                for (pageIdx in 0 until visibleCount) {
                    val isSelected = splitMode == SplitMode.EVERY_PAGE || selectedPagesZeroBased.contains(pageIdx)
                    val liftOffset by animateDpAsState(
                        targetValue = if (isSelected) (-6).dp else 0.dp,
                        animationSpec = PdflyMotion.tactileSpring(),
                        label = "split_page_lift"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 0.96f,
                        animationSpec = PdflyMotion.snappySpring(),
                        label = "split_page_scale"
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) pdflyColors.skyContainer
                        else pdflyColors.subtleSurface,
                        animationSpec = tween(PdflyMotion.FAST_MS),
                        label = "split_page_bg"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) pdflyColors.skyIcon else pdflyColors.strongBorder,
                        animationSpec = tween(PdflyMotion.FAST_MS),
                        label = "split_page_border"
                    )

                    Box(
                        modifier = Modifier
                            .offset(y = liftOffset)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .width(54.dp)
                            .height(70.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onTogglePageZeroBased(pageIdx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "P.${pageIdx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) pdflyColors.skyIcon else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(pdflyColors.skyIcon),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
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
 * 3. COMPRESS PDF FUNCTIONAL MOTION VISUALIZER:
 * Animates target DPI & JPEG stream density bars dynamically when switching between
 * LOW, MEDIUM, and HIGH compression presets.
 */
@Composable
fun CompressionDensityVisualizer(
    inputSizeBytes: Long,
    preset: CompressionPreset,
    modifier: Modifier = Modifier
) {
    val pdflyColors = MaterialTheme.pdflyColors
    val targetDensityRatio = when (preset) {
        CompressionPreset.LOW -> 0.82f
        CompressionPreset.MEDIUM -> 0.58f
        CompressionPreset.HIGH -> 0.38f
    }
    val animatedRatio by animateFloatAsState(
        targetValue = targetDensityRatio,
        animationSpec = PdflyMotion.tactileSpring(),
        label = "compression_density_ratio"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compress_functional_visualizer"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, pdflyColors.cardBorder),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Compress,
                        contentDescription = null,
                        tint = pdflyColors.emeraldIcon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Raster Stream Optimization Profile",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                AnimatedContent(targetState = preset, label = "preset_badge") { p ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = pdflyColors.successContainer,
                        border = BorderStroke(1.dp, pdflyColors.successBorder)
                    ) {
                        Text(
                            text = "${p.dpi} DPI • ${p.jpegQuality}% Q",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = pdflyColors.successText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Original vs Optimized Stream Bars
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Source Document Stream (100%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        LocalFileManager.formatFileSize(inputSizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Target Raster Stream Density",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = pdflyColors.successText
                    )
                    Text(
                        "${(animatedRatio * 100).roundToInt()}% Bitrate Factor",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = pdflyColors.successText
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, pdflyColors.cardBorder, RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedRatio.coerceIn(0.15f, 1f))
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(pdflyColors.emeraldIcon)
                    )
                }
            }
        }
    }
}

/**
 * 4. IMAGE TO PDF FUNCTIONAL MOTION VISUALIZER:
 * Animates a live paper sheet morphing its aspect ratio (A4, Letter, Legal, Fit),
 * rotating between Portrait and Landscape, and adjusting margin padding dynamically.
 */
@Composable
fun ImageToPageMorphVisualizer(
    paperSize: PaperSizePreset,
    orientation: PageOrientation,
    margin: PageMarginPreset,
    imageCount: Int,
    modifier: Modifier = Modifier
) {
    val pdflyColors = MaterialTheme.pdflyColors
    val rawW = if (orientation == PageOrientation.PORTRAIT) paperSize.widthPt else paperSize.heightPt
    val rawH = if (orientation == PageOrientation.PORTRAIT) paperSize.heightPt else paperSize.widthPt
    val aspect = rawW.toFloat() / rawH.toFloat().coerceAtLeast(1f)

    val targetHeightDp = 96.dp
    val targetWidthDp = (96f * aspect).coerceIn(64f, 130f).dp

    val animatedWidth by animateDpAsState(
        targetValue = targetWidthDp,
        animationSpec = PdflyMotion.tactileSpring(),
        label = "sheet_width"
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeightDp,
        animationSpec = PdflyMotion.tactileSpring(),
        label = "sheet_height"
    )
    val animatedMarginPad by animateDpAsState(
        targetValue = (margin.marginPt / 4.5f).dp,
        animationSpec = PdflyMotion.snappySpring(),
        label = "sheet_margin"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("img2pdf_functional_visualizer"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, pdflyColors.cardBorder),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Live Page Geometry Preview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${paperSize.label} • ${orientation.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = pdflyColors.violetIcon,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Dimensions: ${rawW}pt × ${rawH}pt\nMargin Inset: ${margin.marginPt}pt • $imageCount page(s)",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Animated Paper Sheet Box
            Box(
                modifier = Modifier
                    .width(142.dp)
                    .height(114.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(pdflyColors.subtleSurface)
                    .border(1.dp, pdflyColors.cardBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(animatedWidth)
                        .height(animatedHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, pdflyColors.violetIcon, RoundedCornerShape(8.dp))
                        .padding(animatedMarginPad),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(pdflyColors.violetContainer)
                            .border(1.dp, pdflyColors.violetIcon.copy(alpha = 0.6f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = pdflyColors.violetIcon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
