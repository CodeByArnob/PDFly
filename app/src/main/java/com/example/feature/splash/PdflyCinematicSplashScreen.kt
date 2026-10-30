package com.example.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.motion.LocalReducedMotion
import com.example.ui.motion.PdflyMotion
import com.example.ui.theme.AmberTertiaryDark
import com.example.ui.theme.AmberTertiaryLight
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.CrimsonPrimaryLight
import com.example.ui.theme.pdflyColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-screen choreographed cinematic launch experience for PDFly.
 *
 * Sequence (~1.85s cold start, or ~0.22s when reduced motion is enabled):
 * 1. Brand Atmosphere: High-contrast Light (#F1F5F9 / #FFFFFF) or Dark (#0B1120 / #131C31) studio
 *    backdrop with subtle radial lighting, precision grid accents, and floating luminous particles.
 * 2. Document Assembly: Three multi-layered PDF page silhouettes (Reader, Scanner, Security/Sign)
 *    smoothly glide, un-tilt, and dock into a single unified PDFly identity.
 * 3. PDFly Logo & Specular Light Sweep: Spring-overshoot entrance of the original PDFly folded-document
 *    emblem with an animated glowing light sweep and shockwave halo.
 * 4. Brand Name & Tagline: Smooth opacity + letter-spacing convergence for "PDFly" followed by a
 *    staggered reveal of "Your Complete PDF Studio".
 * 5. Seamless Portal Exit: Hardware-accelerated scale and fade transition revealing the live Home Dashboard.
 */
@Composable
fun PdflyCinematicSplashScreen(
    isInitializationComplete: Boolean,
    isPausedForInspection: Boolean = false,
    onSkipOrComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReducedMotion = LocalReducedMotion.current
    val pdflyColors = MaterialTheme.pdflyColors
    val isDark = pdflyColors.isDark

    // Master choreographed timeline [0f .. 1f]
    // When paused for deterministic UI inspection in tests, hold at 0.76f where all brand elements are fully visible.
    val masterProgress = remember {
        Animatable(if (isPausedForInspection) 0.76f else 0f)
    }

    LaunchedEffect(isReducedMotion, isPausedForInspection) {
        if (isPausedForInspection) {
            masterProgress.snapTo(0.76f)
            return@LaunchedEffect
        }
        val durationMs = if (isReducedMotion) 220 else 1820
        masterProgress.animateTo(
            targetValue = 0.86f,
            animationSpec = tween(
                durationMillis = (durationMs * 0.86f).toInt(),
                easing = LinearEasing
            )
        )
    }

    // Complete final portal transition [0.86f .. 1.0f] once essential initialization is ready
    LaunchedEffect(masterProgress.value >= 0.85f, isInitializationComplete, isPausedForInspection) {
        if (!isPausedForInspection && masterProgress.value >= 0.85f && isInitializationComplete) {
            val exitDurationMs = if (isReducedMotion) 90 else 260
            masterProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = exitDurationMs,
                    easing = FastOutSlowInEasing
                )
            )
            onSkipOrComplete()
        }
    }

    val t = if (isPausedForInspection) 0.76f else masterProgress.value

    // Phase progress helpers
    val logoEntranceRaw = segmentProgress(t, start = 0.0f, end = 0.44f)
    val logoSpringScale = if (isReducedMotion) {
        1f
    } else {
        springOvershootCurve(logoEntranceRaw)
    }
    val logoAlpha = if (isReducedMotion) 1f else (0.15f + 0.85f * segmentProgress(t, start = 0.0f, end = 0.22f))

    // Document pages assembling into the central PDFly emblem [0.06 .. 0.62]
    val docAssemblyProgress = if (isReducedMotion) {
        1f
    } else {
        PdflyMotion.EmphasizedDecelerate.transform(segmentProgress(t, start = 0.06f, end = 0.62f))
    }

    // Specular light sweep across the logo [0.24 .. 0.78]
    val lightSweepProgress = if (isReducedMotion) {
        0.5f
    } else {
        segmentProgress(t, start = 0.24f, end = 0.78f)
    }

    // Brand name "PDFly" opacity + letter-spacing transition [0.24 .. 0.60]
    val brandTitleProgress = if (isReducedMotion) {
        1f
    } else {
        PdflyMotion.EmphasizedDecelerate.transform(segmentProgress(t, start = 0.24f, end = 0.60f))
    }

    // Staggered tagline "Your Complete PDF Studio" [0.36 .. 0.70]
    val taglineProgress = if (isReducedMotion) {
        1f
    } else {
        PdflyMotion.EmphasizedDecelerate.transform(segmentProgress(t, start = 0.36f, end = 0.70f))
    }

    // Staggered bottom studio pill [0.44 .. 0.74]
    val badgeProgress = if (isReducedMotion) {
        1f
    } else {
        PdflyMotion.EmphasizedDecelerate.transform(segmentProgress(t, start = 0.44f, end = 0.74f))
    }

    // Final portal reveal [0.86 .. 1.00]
    val exitProgress = if (isPausedForInspection) {
        0f
    } else {
        segmentProgress(t, start = 0.86f, end = 1.00f)
    }
    val containerAlpha = (1f - exitProgress * 0.98f).coerceIn(0.02f, 1f)
    val containerScale = if (isReducedMotion) 1f else (1f + exitProgress * 0.055f)

    val baseBackground = if (isDark) BackgroundDark else BackgroundLight
    val primaryGlowColor = if (isDark) {
        CrimsonPrimaryDark.copy(alpha = 0.22f)
    } else {
        CrimsonPrimaryLight.copy(alpha = 0.11f)
    }
    val secondaryGlowColor = if (isDark) {
        AmberTertiaryDark.copy(alpha = 0.14f)
    } else {
        AmberTertiaryLight.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = containerAlpha
                scaleX = containerScale
                scaleY = containerScale
            }
            .background(baseBackground)
            .testTag("cinematic_splash_root")
    ) {
        // 1. Ambient Studio Lighting, Precision Grid & Floating Particles Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("splash_ambient_backdrop_canvas")
        ) {
            drawStudioBackdropAndParticles(
                isDark = isDark,
                isReducedMotion = isReducedMotion,
                timeline = t,
                primaryGlow = primaryGlowColor,
                secondaryGlow = secondaryGlowColor
            )
        }

        // 2. Skip Intro Button (top-right, accessible & unobtrusive)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.78f else 0.92f),
                border = BorderStroke(1.dp, pdflyColors.cardBorder),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .clickable(onClick = onSkipOrComplete)
                    .testTag("splash_skip_button")
            ) {
                Text(
                    text = "Skip Intro",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            // 3. Central Choreographed Identity Sequence (Logo + Assembling Documents + Typography)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Visual stage: Assembling PDF Document Sheets + Original PDFly Emblem + Light Sweep
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .graphicsLayer {
                            scaleX = logoSpringScale
                            scaleY = logoSpringScale
                            alpha = logoAlpha
                        }
                        .testTag("splash_logo_emblem"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("splash_document_assembly_canvas")
                    ) {
                        drawAssemblingPdfDocumentsAndEmblem(
                            isDark = isDark,
                            assemblyProgress = docAssemblyProgress,
                            lightSweepProgress = lightSweepProgress,
                            timeline = t,
                            isReducedMotion = isReducedMotion
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Brand Name: "PDFly" with smooth opacity, vertical settle, and letter-spacing convergence
                val animatedLetterSpacingSp = ((1f - brandTitleProgress) * 7.5f - 0.5f)
                Text(
                    text = "PDFly",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = animatedLetterSpacingSp.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = brandTitleProgress.coerceIn(0f, 1f)
                            translationY = (1f - brandTitleProgress) * 24f
                        }
                        .testTag("splash_brand_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Staggered Tagline: "Your Complete PDF Studio"
                Text(
                    text = "Your Complete PDF Studio",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    ),
                    color = if (isDark) CrimsonPrimaryDark else CrimsonPrimaryLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = taglineProgress.coerceIn(0f, 1f)
                            translationY = (1f - taglineProgress) * 20f
                        }
                        .testTag("splash_brand_tagline")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Read • Merge • Split • Scan • Sign • Convert • Protect",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = taglineProgress.coerceIn(0f, 1f)
                            translationY = (1f - taglineProgress) * 16f
                        }
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Animated Document Studio Status Pill + Progress Bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = badgeProgress.coerceIn(0f, 1f)
                            translationY = (1f - badgeProgress) * 16f
                        }
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, pdflyColors.cardBorder),
                        shadowElevation = if (isDark) 0.dp else 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = pdflyColors.emeraldIcon,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% On-Device • Offline-First • Free Forever",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sleek choreographed studio readiness bar
                    val barProgress = (t / 0.86f).coerceIn(0.08f, 1f)
                    Box(
                        modifier = Modifier
                            .width(148.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(pdflyColors.cardBorder.copy(alpha = 0.55f))
                            .testTag("splash_progress_indicator")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = barProgress)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            CrimsonPrimaryLight,
                                            CrimsonPrimaryDark,
                                            if (isDark) AmberTertiaryDark else AmberTertiaryLight
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun segmentProgress(value: Float, start: Float, end: Float): Float {
    if (end <= start) return 1f
    return ((value - start) / (end - start)).coerceIn(0f, 1f)
}

/**
 * Custom damped spring-overshoot curve mapping [0f..1f] -> smooth scale entrance with subtle 6% overshoot.
 */
private fun springOvershootCurve(x: Float): Float {
    val clamped = x.coerceIn(0f, 1f)
    if (clamped == 0f) return 0.42f
    if (clamped == 1f) return 1.0f
    // Damped harmonic spring response starting at 0.42f and settling at 1.0f
    val decay = kotlin.math.exp(-5.2f * clamped)
    val oscillation = cos(2.15f * PI.toFloat() * clamped)
    return 1.0f - (1.0f - 0.42f) * decay * oscillation
}

private fun DrawScope.drawStudioBackdropAndParticles(
    isDark: Boolean,
    isReducedMotion: Boolean,
    timeline: Float,
    primaryGlow: Color,
    secondaryGlow: Color
) {
    val w = size.width
    val h = size.height
    val center = Offset(w * 0.5f, h * 0.43f)
    val maxDim = maxOf(w, h)

    // Radial studio spotlight behind the emblem
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(primaryGlow, Color.Transparent),
            center = center,
            radius = maxDim * 0.48f
        ),
        radius = maxDim * 0.48f,
        center = center
    )

    // Secondary warm amber rim light
    val secondaryCenter = Offset(w * 0.65f, h * 0.34f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(secondaryGlow, Color.Transparent),
            center = secondaryCenter,
            radius = maxDim * 0.34f
        ),
        radius = maxDim * 0.34f,
        center = secondaryCenter
    )

    // Subtle architectural document alignment rings around center
    val ringColor = if (isDark) {
        Color(0xFF334155).copy(alpha = 0.28f)
    } else {
        Color(0xFFCBD5E1).copy(alpha = 0.45f)
    }
    val baseRadius = minOf(w, h) * 0.25f
    drawCircle(
        color = ringColor,
        radius = baseRadius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
    drawCircle(
        color = ringColor.copy(alpha = ringColor.alpha * 0.55f),
        radius = baseRadius * 1.32f,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // Subtle luminous floating studio particles (when full motion is active)
    if (!isReducedMotion) {
        val particleCount = 12
        for (i in 0 until particleCount) {
            val angle = (i * (2.0 * PI / particleCount)).toFloat() + timeline * 0.55f * (if (i % 2 == 0) 1f else -1f)
            val orbitFactor = 0.72f + (i % 4) * 0.16f
            val radius = baseRadius * orbitFactor
            val px = center.x + cos(angle) * radius
            val py = center.y + sin(angle) * radius - (timeline * 14f * ((i % 3) + 1))
            val dotRadius = (if (i % 3 == 0) 3.2.dp else 2.2.dp).toPx()
            val dotColor = when (i % 3) {
                0 -> if (isDark) Color(0xFFFB7185) else Color(0xFFBE123C)
                1 -> if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                else -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0369A1)
            }
            val dotAlpha = (0.18f + 0.35f * sin(timeline * PI.toFloat())).coerceIn(0f, 0.55f)
            drawCircle(
                color = dotColor.copy(alpha = dotAlpha),
                radius = dotRadius,
                center = Offset(px, py)
            )
        }
    }
}

/**
 * Draws the choreographed document assembly visual and the central PDFly emblem with its
 * diagonal specular light sweep.
 */
private fun DrawScope.drawAssemblingPdfDocumentsAndEmblem(
    isDark: Boolean,
    assemblyProgress: Float,
    lightSweepProgress: Float,
    timeline: Float,
    isReducedMotion: Boolean
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val center = Offset(cx, cy)

    // 1. Shockwave convergence ring when satellite PDF pages lock into the central emblem
    if (!isReducedMotion && assemblyProgress > 0.35f) {
        val ringT = ((assemblyProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
        val ringRadius = (46.dp.toPx()) + ringT * (56.dp.toPx())
        val ringAlpha = (1f - ringT) * (if (isDark) 0.45f else 0.32f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    (if (isDark) Color(0xFFFB7185) else Color(0xFFBE123C)).copy(alpha = ringAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = ringRadius
            ),
            radius = ringRadius,
            center = center
        )
    }

    // 2. Three satellite PDF pages smoothly converging and aligning into the central PDFly studio badge
    val spreadFactor = (1f - assemblyProgress).coerceIn(0f, 1f)
    val sheetW = 68.dp.toPx()
    val sheetH = 88.dp.toPx()

    // Left Satellite Page: Document Reader & Text Layout (Indigo/Sky accent)
    val leftOffsetX = -56.dp.toPx() * spreadFactor - 12.dp.toPx() * assemblyProgress
    val leftOffsetY = 14.dp.toPx() * spreadFactor + 4.dp.toPx() * assemblyProgress
    val leftRotation = -24f * spreadFactor - 8f * assemblyProgress
    drawSatellitePdfPage(
        center = Offset(cx + leftOffsetX, cy + leftOffsetY),
        width = sheetW,
        height = sheetH,
        rotationDeg = leftRotation,
        surfaceColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        borderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
        accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0369A1),
        alpha = (0.45f + 0.45f * (1f - assemblyProgress * 0.35f)).coerceIn(0f, 1f),
        variant = 0
    )

    // Right Satellite Page: Optical Scanner & Page Grid (Emerald/Amber accent)
    val rightOffsetX = 56.dp.toPx() * spreadFactor + 12.dp.toPx() * assemblyProgress
    val rightOffsetY = 12.dp.toPx() * spreadFactor + 4.dp.toPx() * assemblyProgress
    val rightRotation = 24f * spreadFactor + 8f * assemblyProgress
    drawSatellitePdfPage(
        center = Offset(cx + rightOffsetX, cy + rightOffsetY),
        width = sheetW,
        height = sheetH,
        rotationDeg = rightRotation,
        surfaceColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        borderColor = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
        accentColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
        alpha = (0.45f + 0.45f * (1f - assemblyProgress * 0.35f)).coerceIn(0f, 1f),
        variant = 1
    )

    // Top Satellite Page: Security & Digital Signature Layer (Violet/Rose accent)
    val topOffsetY = -48.dp.toPx() * spreadFactor - 8.dp.toPx() * assemblyProgress
    val topRotation = 12f * spreadFactor
    drawSatellitePdfPage(
        center = Offset(cx, cy + topOffsetY),
        width = sheetW * 0.94f,
        height = sheetH * 0.94f,
        rotationDeg = topRotation,
        surfaceColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        borderColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
        accentColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF6D28D9),
        alpha = (0.40f + 0.45f * (1f - assemblyProgress * 0.45f)).coerceIn(0f, 1f),
        variant = 2
    )

    // 3. Central PDFly Brand Squircle Badge & Folded Document Emblem
    val badgeSize = 106.dp.toPx()
    val badgeLeft = cx - badgeSize / 2f
    val badgeTop = cy - badgeSize / 2f
    val badgeCorner = 28.dp.toPx()

    // Outer ambient glow behind the central badge
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE11D48).copy(alpha = if (isDark) 0.48f else 0.26f),
                Color.Transparent
            ),
            center = center,
            radius = badgeSize * 0.85f
        ),
        topLeft = Offset(cx - badgeSize * 0.8f, cy - badgeSize * 0.8f),
        size = Size(badgeSize * 1.6f, badgeSize * 1.6f),
        cornerRadius = CornerRadius(badgeCorner * 1.5f, badgeCorner * 1.5f)
    )

    // Central squircle container (#0F172A obsidian slate matching ic_launcher_background + crimson rim)
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = if (isDark) {
                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
            } else {
                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
            },
            start = Offset(badgeLeft, badgeTop),
            end = Offset(badgeLeft + badgeSize, badgeTop + badgeSize)
        ),
        topLeft = Offset(badgeLeft, badgeTop),
        size = Size(badgeSize, badgeSize),
        cornerRadius = CornerRadius(badgeCorner, badgeCorner)
    )

    // Crisp luminous border around the central squircle
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFB7185),
                Color(0xFFFBBF24),
                Color(0xFFBE123C)
            ),
            start = Offset(badgeLeft, badgeTop),
            end = Offset(badgeLeft + badgeSize, badgeTop + badgeSize)
        ),
        topLeft = Offset(badgeLeft, badgeTop),
        size = Size(badgeSize, badgeSize),
        cornerRadius = CornerRadius(badgeCorner, badgeCorner),
        style = Stroke(width = 2.2.dp.toPx())
    )

    // Folded Crimson PDF Document inside the central emblem
    val docW = 54.dp.toPx()
    val docH = 66.dp.toPx()
    val foldSize = 16.dp.toPx()
    val docLeft = cx - docW / 2f
    val docTop = cy - docH / 2f
    val docRight = docLeft + docW
    val docBottom = docTop + docH

    val docBodyPath = Path().apply {
        moveTo(docLeft + 8.dp.toPx(), docTop)
        lineTo(docRight - foldSize, docTop)
        lineTo(docRight, docTop + foldSize)
        lineTo(docRight, docBottom - 8.dp.toPx())
        quadraticTo(docRight, docBottom, docRight - 8.dp.toPx(), docBottom)
        lineTo(docLeft + 8.dp.toPx(), docBottom)
        quadraticTo(docLeft, docBottom, docLeft, docBottom - 8.dp.toPx())
        lineTo(docLeft, docTop + 8.dp.toPx())
        quadraticTo(docLeft, docTop, docLeft + 8.dp.toPx(), docTop)
        close()
    }

    drawPath(
        path = docBodyPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFF43F5E),
                Color(0xFFE11D48),
                Color(0xFF9F1239)
            ),
            start = Offset(docLeft, docTop),
            end = Offset(docRight, docBottom)
        )
    )

    // Folded Top-Right Corner Flap in Warm Amber Gold
    val foldPath = Path().apply {
        moveTo(docRight - foldSize, docTop)
        lineTo(docRight - foldSize, docTop + foldSize - 3.dp.toPx())
        quadraticTo(
            docRight - foldSize,
            docTop + foldSize,
            docRight - foldSize + 3.dp.toPx(),
            docTop + foldSize
        )
        lineTo(docRight, docTop + foldSize)
        close()
    }
    drawPath(
        path = foldPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFDE68A), Color(0xFFF59E0B)),
            start = Offset(docRight - foldSize, docTop),
            end = Offset(docRight, docTop + foldSize)
        )
    )

    // Crisp white editorial document lines & PDF vector glyph inside the folded sheet
    val lineStartX = docLeft + 11.dp.toPx()
    val lineEndXFull = docRight - 11.dp.toPx()
    val lineEndXShort = docLeft + docW * 0.62f
    val strokeW = 3.2.dp.toPx()

    // Top "PDF" pill bar inside the document
    drawRoundRect(
        color = Color.White.copy(alpha = 0.95f),
        topLeft = Offset(lineStartX, docTop + 14.dp.toPx()),
        size = Size(20.dp.toPx(), 8.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )

    // Three synchronized horizontal document lines
    val line1Y = docTop + 31.dp.toPx()
    val line2Y = docTop + 41.dp.toPx()
    val line3Y = docTop + 51.dp.toPx()

    drawLine(
        color = Color.White.copy(alpha = 0.95f),
        start = Offset(lineStartX, line1Y),
        end = Offset(lineEndXFull, line1Y),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White.copy(alpha = 0.88f),
        start = Offset(lineStartX, line2Y),
        end = Offset(lineEndXFull, line2Y),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFFDE68A),
        start = Offset(lineStartX, line3Y),
        end = Offset(lineEndXShort, line3Y),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )

    // 4. Diagonal Specular Glowing Light Sweep across the central emblem
    if (lightSweepProgress in 0.02f..0.98f) {
        val sweepOffset = (lightSweepProgress * 2.2f - 0.6f) * badgeSize
        val sweepStart = Offset(badgeLeft + sweepOffset - 28.dp.toPx(), badgeTop - 16.dp.toPx())
        val sweepEnd = Offset(badgeLeft + sweepOffset + 28.dp.toPx(), badgeTop + badgeSize + 16.dp.toPx())

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.34f * sin(lightSweepProgress * PI.toFloat())),
                    Color(0xFFFDE68A).copy(alpha = 0.22f * sin(lightSweepProgress * PI.toFloat())),
                    Color.Transparent
                ),
                start = sweepStart,
                end = sweepEnd
            ),
            topLeft = Offset(badgeLeft, badgeTop),
            size = Size(badgeSize, badgeSize),
            cornerRadius = CornerRadius(badgeCorner, badgeCorner)
        )
    }
}

private fun DrawScope.drawSatellitePdfPage(
    center: Offset,
    width: Float,
    height: Float,
    rotationDeg: Float,
    surfaceColor: Color,
    borderColor: Color,
    accentColor: Color,
    alpha: Float,
    variant: Int
) {
    withTransform({
        rotate(degrees = rotationDeg, pivot = center)
    }) {
        val left = center.x - width / 2f
        val top = center.y - height / 2f
        val corner = 12.dp.toPx()

        drawRoundRect(
            color = surfaceColor.copy(alpha = alpha),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(corner, corner)
        )
        drawRoundRect(
            color = borderColor.copy(alpha = alpha * 0.85f),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 1.5.dp.toPx())
        )

        val pad = 10.dp.toPx()
        when (variant) {
            0 -> {
                // Reader / Text document lines
                for (i in 0..3) {
                    val y = top + pad + 8.dp.toPx() + i * 11.dp.toPx()
                    val lineW = if (i == 3) width * 0.45f else width - pad * 2
                    drawLine(
                        color = accentColor.copy(alpha = alpha * (if (i == 0) 0.85f else 0.45f)),
                        start = Offset(left + pad, y),
                        end = Offset(left + pad + lineW, y),
                        strokeWidth = 2.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            1 -> {
                // Scanner / Page grid blocks
                val boxSize = 14.dp.toPx()
                drawRoundRect(
                    color = accentColor.copy(alpha = alpha * 0.75f),
                    topLeft = Offset(left + pad, top + pad + 4.dp.toPx()),
                    size = Size(boxSize, boxSize),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
                drawRoundRect(
                    color = borderColor.copy(alpha = alpha * 0.65f),
                    topLeft = Offset(left + pad + boxSize + 6.dp.toPx(), top + pad + 4.dp.toPx()),
                    size = Size(boxSize, boxSize),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
                val lineY = top + pad + boxSize + 14.dp.toPx()
                drawLine(
                    color = borderColor.copy(alpha = alpha * 0.55f),
                    start = Offset(left + pad, lineY),
                    end = Offset(left + width - pad, lineY),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            else -> {
                // Signature curve / Security seal
                val sigPath = Path().apply {
                    val sy = top + height * 0.55f
                    moveTo(left + pad, sy)
                    cubicTo(
                        left + width * 0.35f, sy - 10.dp.toPx(),
                        left + width * 0.55f, sy + 10.dp.toPx(),
                        left + width - pad, sy - 4.dp.toPx()
                    )
                }
                drawPath(
                    path = sigPath,
                    color = accentColor.copy(alpha = alpha * 0.85f),
                    style = Stroke(
                        width = 2.2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
