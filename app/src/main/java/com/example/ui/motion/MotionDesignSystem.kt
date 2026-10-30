package com.example.ui.motion

import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.feature.TopLevelDestination
import kotlinx.coroutines.delay

/**
 * CompositionLocal indicating whether animations should be simplified or shortened
 * due to Android accessibility settings or user preference.
 */
val LocalReducedMotion = compositionLocalOf { false }

@Composable
fun rememberEffectiveReducedMotion(userReducedMotionPref: Boolean): Boolean {
    val context = LocalContext.current
    val systemScaleZero = remember(context) {
        runCatching {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale == 0f
        }.getOrDefault(false)
    }
    return userReducedMotionPref || systemScaleZero
}

/**
 * Centralized PDFly Motion Design System tokens, easing curves, spring physics,
 * and choreographed screen transitions.
 */
object PdflyMotion {
    const val FAST_MS = 150
    const val STANDARD_MS = 250
    const val CINEMATIC_MS = 340
    const val STAGGER_STEP_MS = 28L

    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val StandardEasing = FastOutSlowInEasing

    fun <T> tactileSpring() = spring<T>(
        dampingRatio = 0.72f,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> snappySpring() = spring<T>(
        dampingRatio = 0.80f,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> gentleSpring() = spring<T>(
        dampingRatio = 0.86f,
        stiffness = Spring.StiffnessLow
    )

    private val topLevelOrder = listOf(
        TopLevelDestination.DASHBOARD,
        TopLevelDestination.DOCUMENTS,
        TopLevelDestination.HISTORY,
        TopLevelDestination.SUPPORT,
        TopLevelDestination.SETTINGS
    )

    fun screenTransition(
        scope: AnimatedContentTransitionScope<TopLevelDestination>,
        reducedMotion: Boolean
    ): ContentTransform = with(scope) {
        if (reducedMotion) {
            return fadeIn(tween(90)) togetherWith fadeOut(tween(90))
        }

        val initialIdx = topLevelOrder.indexOf(initialState)
        val targetIdx = topLevelOrder.indexOf(targetState)

        when {
            // Navigating between top-level navigation tabs: ordered horizontal spatial glide
            initialIdx != -1 && targetIdx != -1 -> {
                val forward = targetIdx > initialIdx
                val slideDir = if (forward) 1 else -1
                (slideInHorizontally(
                    animationSpec = tween(STANDARD_MS, easing = EmphasizedDecelerate),
                    initialOffsetX = { fullWidth -> (fullWidth * 0.12f * slideDir).toInt() }
                ) + fadeIn(tween(FAST_MS))) togetherWith (slideOutHorizontally(
                    animationSpec = tween(FAST_MS, easing = EmphasizedAccelerate),
                    targetOffsetX = { fullWidth -> -(fullWidth * 0.08f * slideDir).toInt() }
                ) + fadeOut(tween(FAST_MS)))
            }

            // Returning from a Tool/Detail screen back to a Top-Level tab
            targetIdx != -1 && initialIdx == -1 -> {
                (fadeIn(tween(FAST_MS, easing = EmphasizedDecelerate)) +
                    scaleIn(initialScale = 0.97f, animationSpec = tween(STANDARD_MS, easing = EmphasizedDecelerate))) togetherWith
                    (slideOutVertically(
                        animationSpec = tween(FAST_MS, easing = EmphasizedAccelerate),
                        targetOffsetY = { fullHeight -> (fullHeight * 0.10f).toInt() }
                    ) + fadeOut(tween(FAST_MS)))
            }

            // Drilling into a PDF Tool Studio or Sub-screen: cinematic upward elevation
            else -> {
                (slideInVertically(
                    animationSpec = tween(CINEMATIC_MS, easing = EmphasizedDecelerate),
                    initialOffsetY = { fullHeight -> (fullHeight * 0.12f).toInt() }
                ) + scaleIn(
                    initialScale = 0.97f,
                    animationSpec = tween(CINEMATIC_MS, easing = EmphasizedDecelerate)
                ) + fadeIn(tween(FAST_MS))) togetherWith
                    fadeOut(tween(FAST_MS))
            }
        }
    }
}

/**
 * Tactile micro-interaction modifier that scales down smoothly on press with spring physics,
 * triggers subtle haptic feedback, and displays a Material 3 ripple.
 */
fun Modifier.pdflyPressable(
    enabled: Boolean = true,
    pressedScale: Float = 0.965f,
    enableHaptics: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val reducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !reducedMotion) pressedScale else 1f,
        animationSpec = PdflyMotion.snappySpring(),
        label = "press_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .let { base ->
            if (onClick != null) {
                base.clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    enabled = enabled,
                    onClick = {
                        if (enableHaptics) {
                            runCatching { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                        }
                        onClick()
                    }
                )
            } else {
                base
            }
        }
}

/**
 * Choreographed staggered entrance container for dashboard & tool sections.
 * CRITICAL CONTRAST RULE: Never reduces text/surface opacity below 1f so content is always
 * 100% full-contrast and readable on every frame, using vertical spring motion only.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reducedMotion = LocalReducedMotion.current
    if (reducedMotion) {
        Box(modifier = modifier) { content() }
        return
    }

    val offsetYAnim = remember { Animatable(16f) }

    LaunchedEffect(Unit) {
        val delayMs = (index.coerceIn(0, 6) * PdflyMotion.STAGGER_STEP_MS)
        if (delayMs > 0) delay(delayMs)
        offsetYAnim.animateTo(
            targetValue = 0f,
            animationSpec = PdflyMotion.tactileSpring()
        )
    }

    Box(
        modifier = modifier.graphicsLayer {
            translationY = offsetYAnim.value
        }
    ) {
        content()
    }
}

/**
 * Clean, high-contrast architectural background container that preserves crisp contrast
 * between the app background (#F1F5F9 in Light / #0B1120 in Dark) and pure surface cards (#FFFFFF / #131C31).
 */
@Composable
fun CinematicAmbientContainer(
    accentColor: Color = MaterialTheme.colorScheme.primary,
    secondaryAccent: Color = MaterialTheme.colorScheme.tertiary,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}
