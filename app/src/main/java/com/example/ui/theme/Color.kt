package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// LIGHT MODE PALETTE (WCAG 2.2 AA+ High-Contrast Editorial Studio)
// ============================================================================
val CrimsonPrimaryLight = Color(0xFFBE123C)          // Deep Rose-Crimson (6.4:1 on white)
val OnCrimsonPrimaryLight = Color(0xFFFFFFFF)
val CrimsonContainerLight = Color(0xFFFFE4E6)
val OnCrimsonContainerLight = Color(0xFF4C0519)

val SlateSecondaryLight = Color(0xFF1E293B)          // Deep Architectural Slate (12.6:1 on white)
val OnSlateSecondaryLight = Color(0xFFFFFFFF)
val SlateContainerLight = Color(0xFFE2E8F0)
val OnSlateContainerLight = Color(0xFF0F172A)

val AmberTertiaryLight = Color(0xFFB45309)           // Deep Rich Amber (5.1:1 on white)
val OnAmberTertiaryLight = Color(0xFFFFFFFF)
val AmberContainerLight = Color(0xFFFEF3C7)
val OnAmberContainerLight = Color(0xFF451A03)

val ErrorLight = Color(0xFFB91C1C)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF450A0A)

// Neutral Surfaces & Text (Light Mode)
val BackgroundLight = Color(0xFFF1F5F9)              // Crisp cool off-white canvas so #FFFFFF cards pop
val SurfaceLight = Color(0xFFFFFFFF)                 // Pure white card & bar surface
val SurfaceVariantLight = Color(0xFFE2E8F0)          // Distinct secondary container surface
val OnBackgroundLight = Color(0xFF0F172A)            // Deep ink primary text (16.8:1)
val OnSurfaceLight = Color(0xFF111827)               // Crisp charcoal primary text (16.0:1)
val OnSurfaceVariantLight = Color(0xFF334155)        // High-contrast secondary text (8.5:1)
val MutedTextLight = Color(0xFF475569)               // Accessible tertiary text (5.9:1)
val OutlineLight = Color(0xFF94A3B8)                 // Clearly visible control & input borders
val OutlineVariantLight = Color(0xFFCBD5E1)          // Crisp 1dp card & divider border

// ============================================================================
// DARK MODE PALETTE (Balanced High-Contrast Obsidian Studio)
// ============================================================================
val CrimsonPrimaryDark = Color(0xFFFB7185)
val OnCrimsonPrimaryDark = Color(0xFF4C0519)
val CrimsonContainerDark = Color(0xFF881337)
val OnCrimsonContainerDark = Color(0xFFFFE4E6)

val SlateSecondaryDark = Color(0xFFCBD5E1)
val OnSlateSecondaryDark = Color(0xFF0F172A)
val SlateContainerDark = Color(0xFF1E293B)
val OnSlateContainerDark = Color(0xFFF1F5F9)

val AmberTertiaryDark = Color(0xFFFBBF24)
val OnAmberTertiaryDark = Color(0xFF451A03)
val AmberContainerDark = Color(0xFF78350F)
val OnAmberContainerDark = Color(0xFFFEF3C7)

val ErrorDark = Color(0xFFF87171)
val OnErrorDark = Color(0xFF450A0A)
val ErrorContainerDark = Color(0xFF7F1D1D)
val OnErrorContainerDark = Color(0xFFFEE2E2)

// Neutral Surfaces & Text (Dark Mode)
val BackgroundDark = Color(0xFF0B1120)               // Deep obsidian navy canvas
val SurfaceDark = Color(0xFF131C31)                  // Elevated dark slate card surface
val SurfaceVariantDark = Color(0xFF1E293B)           // Secondary container surface
val OnBackgroundDark = Color(0xFFF8FAFC)
val OnSurfaceDark = Color(0xFFF8FAFC)                // Pure crisp white primary text
val OnSurfaceVariantDark = Color(0xFFCBD5E1)         // High-contrast silver secondary text (10.4:1)
val MutedTextDark = Color(0xFF94A3B8)                // Accessible tertiary text (5.6:1)
val OutlineDark = Color(0xFF475569)                  // Visible control & input border
val OutlineVariantDark = Color(0xFF334155)           // Visible 1dp card & divider border

// ============================================================================
// SHARED BRAND & TOOL ACCENTS
// ============================================================================
val EmeraldSuccess = Color(0xFF059669)
val EmeraldSuccessDark = Color(0xFF10B981)
val EmeraldContainerLight = Color(0xFFD1FAE5)
val EmeraldContainerDark = Color(0xFF064E3B)

val SkyBlueAccent = Color(0xFF0369A1)
val VioletAccent = Color(0xFF6D28D9)
val IndigoAccent = Color(0xFF4338CA)
val UsdtGreen = Color(0xFF0D9488)
val UsdcBlue = Color(0xFF1D4ED8)
val BscGold = Color(0xFFD97706)

/**
 * Semantic theme-aware color tokens for badges, tool icons, borders, and status indicators
 * ensuring >= 4.5:1 text contrast in both Light Mode and Dark Mode.
 */
@Immutable
data class PdflySemanticColors(
    val isDark: Boolean,
    val cardBorder: Color,
    val strongBorder: Color,
    val subtleSurface: Color,
    val mutedText: Color,
    // Success / Free & Offline Badge
    val successText: Color,
    val successContainer: Color,
    val successBorder: Color,
    // Warning / Amber / BSC Network Badge
    val warningText: Color,
    val warningContainer: Color,
    val warningBorder: Color,
    // Tool Accents (Icon Tint + Container + Border)
    val crimsonIcon: Color,
    val crimsonContainer: Color,
    val indigoIcon: Color,
    val indigoContainer: Color,
    val skyIcon: Color,
    val skyContainer: Color,
    val emeraldIcon: Color,
    val emeraldContainer: Color,
    val violetIcon: Color,
    val violetContainer: Color,
    val amberIcon: Color,
    val amberContainer: Color,
    val tealIcon: Color,
    val tealContainer: Color,
    val roseIcon: Color,
    val roseContainer: Color,
    // Crypto Token Brand Colors
    val usdtBrand: Color,
    val usdtContainer: Color,
    val usdcBrand: Color,
    val usdcContainer: Color
)

val LightPdflySemanticColors = PdflySemanticColors(
    isDark = false,
    cardBorder = Color(0xFFCBD5E1),
    strongBorder = Color(0xFF94A3B8),
    subtleSurface = Color(0xFFF8FAFC),
    mutedText = MutedTextLight,
    successText = Color(0xFF065F46),
    successContainer = Color(0xFFD1FAE5),
    successBorder = Color(0xFF34D399),
    warningText = Color(0xFF92400E),
    warningContainer = Color(0xFFFEF3C7),
    warningBorder = Color(0xFFF59E0B),
    crimsonIcon = Color(0xFFBE123C),
    crimsonContainer = Color(0xFFFFE4E6),
    indigoIcon = Color(0xFF3730A3),
    indigoContainer = Color(0xFFE0E7FF),
    skyIcon = Color(0xFF0369A1),
    skyContainer = Color(0xFFE0F2FE),
    emeraldIcon = Color(0xFF047857),
    emeraldContainer = Color(0xFFD1FAE5),
    violetIcon = Color(0xFF5B21B6),
    violetContainer = Color(0xFFEDE9FE),
    amberIcon = Color(0xFFB45309),
    amberContainer = Color(0xFFFEF3C7),
    tealIcon = Color(0xFF0F766E),
    tealContainer = Color(0xFFCCFBF1),
    roseIcon = Color(0xFF9D174D),
    roseContainer = Color(0xFFFCE7F3),
    usdtBrand = Color(0xFF0F766E),
    usdtContainer = Color(0xFFCCFBF1),
    usdcBrand = Color(0xFF1D4ED8),
    usdcContainer = Color(0xFFDBEAFE)
)

val DarkPdflySemanticColors = PdflySemanticColors(
    isDark = true,
    cardBorder = Color(0xFF334155),
    strongBorder = Color(0xFF475569),
    subtleSurface = Color(0xFF1E293B),
    mutedText = MutedTextDark,
    successText = Color(0xFF6EE7B7),
    successContainer = Color(0xFF064E3B),
    successBorder = Color(0xFF059669),
    warningText = Color(0xFFFDE68A),
    warningContainer = Color(0xFF451A03),
    warningBorder = Color(0xFFD97706),
    crimsonIcon = Color(0xFFFB7185),
    crimsonContainer = Color(0xFF4C0519),
    indigoIcon = Color(0xFF818CF8),
    indigoContainer = Color(0xFF1E1B4B),
    skyIcon = Color(0xFF38BDF8),
    skyContainer = Color(0xFF0C4A6E),
    emeraldIcon = Color(0xFF34D399),
    emeraldContainer = Color(0xFF064E3B),
    violetIcon = Color(0xFFA78BFA),
    violetContainer = Color(0xFF2E1065),
    amberIcon = Color(0xFFFBBF24),
    amberContainer = Color(0xFF451A03),
    tealIcon = Color(0xFF2DD4BF),
    tealContainer = Color(0xFF134E4A),
    roseIcon = Color(0xFFF472B6),
    roseContainer = Color(0xFF500724),
    usdtBrand = Color(0xFF26A17B),
    usdtContainer = Color(0xFF064E3B),
    usdcBrand = Color(0xFF3B82F6),
    usdcContainer = Color(0xFF1E3A8A)
)

val LocalPdflySemanticColors = staticCompositionLocalOf { LightPdflySemanticColors }
